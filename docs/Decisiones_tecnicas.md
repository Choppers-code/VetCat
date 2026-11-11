# Decisiones técnicas

Correcciones y definiciones que resuelven las contradicciones entre `Propuesta_VetCat.md` y `Plan_inicial.md`. Cada decisión es la que se implementa; las dos fuentes originales quedan como referencia histórica.

## 1. Multi-tenancy: catálogo global + esquema por clínica

- Esquema `catalogo` global y un esquema `clinica_xxx` por clínica, en la misma base `vetcat_db`.
- **Dos unidades de persistencia**: una sin multi-tenant para `catalogo`, otra con `CurrentTenantIdentifierResolver` y `MultiTenantConnectionProvider` para los esquemas de clínica. Mezclar entidades globales y de tenant en una sola unidad es la parte más frágil de esta fase.
- El nombre del esquema lo genera el servidor a partir de un identificador interno. Nunca lo envía el cliente.
- `catalogo.clinica` incluye `zona_horaria` en formato IANA. Los horarios de atención y el cálculo de disponibilidad se hacen en esa zona; todo instante se guarda como `timestamptz` y las fechas sin hora como `date`.
- Flyway con dos ubicaciones de migración: `catalogo` y `tenant`. El aprovisionamiento de una clínica es idempotente: registra en el catálogo, crea el esquema, aplica las migraciones y registra al administrador en Keycloak, con estados persistidos y un job de reconciliación si un paso falla.

## 2. El tenant sale solo del token

- `tenant_id` proviene exclusivamente del JWT firmado y se valida contra la relación usuario-clínica en base de datos. Nunca de un parámetro, cuerpo o cabecera.
- La resolución es por conexión y transacción. **Ningún bean singleton guarda el tenant actual**; un campo compartido cruza datos entre clínicas bajo concurrencia.
- Los servicios son sin estado, con inyección por constructor.
- El contexto de tenant se limpia en un `finally` al devolver la conexión, se prohíbe la caché de segundo nivel de Hibernate y los jobs (`@Async`, `@Scheduled`) reciben el tenant explícitamente, nunca lo heredan del hilo.

## 3. Pertenencia usuario-clínica: tabla global y multi-clínica

- `catalogo.usuario_clinica (keycloak_id, clinica_id, rol, estado)` es la **única** fuente de verdad de la pertenencia. No vive dentro del esquema de la clínica, porque un usuario puede pertenecer a varias.
- Un usuario puede trabajar en más de una clínica. Al iniciar sesión elige la clínica activa y recibe un token acotado a ella.
- **Cómo se emite ese token está pendiente de un ADR** (token exchange de Keycloak, mapper propio que consulte la base, u Organizations). Lo que no se negocia: el `tenant_id` llega en un token firmado y **el rol efectivo sale de `usuario_clinica` para esa clínica**, no del rol de realm. Un usuario que es administrador en la clínica A y recepcionista en la B no puede llevar el rol de A al token de B.
- Keycloak: un solo realm `vetcat` con los roles como catálogo de valores, y la asignación por clínica en base de datos.

## 4. Roles y permisos

Roles: `administrador_plataforma`, `administrador_clinica`, `veterinario`, `recepcionista`, `dueno`.

- La autorización se aplica con `@PreAuthorize` en el backend.
- `administrador_plataforma` aprovisiona clínicas y da soporte; **no** accede a datos clínicos.
- `recepcionista` gestiona agenda, propietarios y mascotas; **no** accede a datos clínicos ni registra vacunas.
- `dueno` solo lectura de lo suyo, con campos explícitamente permitidos, y el backend verifica la propiedad de la mascota en **cada** consulta.

La matriz completa de acciones por rol se aprueba antes de escribir el primer `@PreAuthorize` y vive en `docs/Matriz_roles.md`.

## 5. Agenda: el empalme lo impide la base de datos

- `cita` incluye `tipo` (`CONSULTORIO` / `DOMICILIO`), `consultorio_id` obligatorio solo para el tipo `CONSULTORIO`, `direccion` para el tipo `DOMICILIO`, `inicio`, `fin`, `veterinario_id`, `mascota_id`, `motivo`, `estado`, `cancelada_por`, `motivo_cancelacion` y `reprogramada_de`.
- Estados: `PENDIENTE`, `CONFIRMADA`, `ATENDIDA`, `NO_ASISTIO`, `CANCELADA`. Se modelan como `varchar` con `CHECK`, no como tipo PostgreSQL: agregar un valor a un `CREATE TYPE` obliga a un `ALTER TYPE` en cada esquema.
- La extensión se crea una sola vez desde la migración de catálogo, en un esquema propio, y todos los esquemas de clínica la incluyen en su `search_path`:

```sql
CREATE SCHEMA IF NOT EXISTS ext;
CREATE EXTENSION IF NOT EXISTS btree_gist SCHEMA ext;
```

- Restricciones de exclusión, sobre el veterinario y sobre el consultorio:

```sql
ALTER TABLE cita
ADD CONSTRAINT cita_sin_empalmes_veterinario
EXCLUDE USING gist (
    veterinario_id WITH =,
    tstzrange(inicio, fin, '[)') WITH &&
)
WHERE (estado <> 'CANCELADA');

ALTER TABLE cita
ADD CONSTRAINT cita_sin_empalmes_consultorio
EXCLUDE USING gist (
    consultorio_id WITH =,
    tstzrange(inicio, fin, '[)') WITH &&
)
WHERE (estado <> 'CANCELADA' AND tipo = 'CONSULTORIO');
```

- Las restricciones anteriores no bastan solas: en una exclusión los `NULL` no se consideran iguales, así que una cita con `consultorio_id` nulo o con `estado` nulo escapa al índice. Hacen falta las columnas `NOT NULL` y estos `CHECK`:

```sql
ALTER TABLE cita
  ALTER COLUMN estado SET NOT NULL,
  ALTER COLUMN tipo SET NOT NULL,
  ALTER COLUMN veterinario_id SET NOT NULL,
  ADD CONSTRAINT cita_rango_valido CHECK (fin > inicio),
  ADD CONSTRAINT cita_consultorio_requerido
    CHECK (tipo <> 'CONSULTORIO' OR consultorio_id IS NOT NULL),
  ADD CONSTRAINT cita_consultorio_no_aplica
    CHECK (tipo <> 'DOMICILIO' OR consultorio_id IS NULL);
```

- Un `SELECT` previo no basta: dos recepcionistas pueden reservar a la vez. El rechazo de la base se traduce en un `409` que la app muestra como conflicto de horario.
- Se agregan `consultorio`, `horario_atencion` (por veterinario y por consultorio, con excepciones por fecha), `bloqueo_agenda` y la duración por servicio en el catálogo de conceptos.
- El horario de atención se valida en el servicio; la restricción de exclusión solo impide empalmes, no citas fuera de horario.

## 6. Expediente clínico: entidades separadas e inmutables

- `consulta` es el encuentro; `diagnostico`, `tratamiento`, `receta`, `nota` y `adjunto` son entidades propias, no columnas de texto.
- Una consulta no se sobrescribe: se corrige con una consulta nueva.
- Toda consulta registra qué veterinario la realizó y las fechas y horas.
- Los adjuntos se guardan fuera de las tablas principales, con referencia, validación por contenido real del archivo, límite de tamaño y acceso mediante URL firmada de vida corta.
- Los cambios importantes y la **lectura** del expediente se registran en auditoría.

## 7. Vacunas

- `vacuna_catalogo` es **global**, en `catalogo`, sembrado por migración, con una tabla de override por clínica y una precedencia definida (el override gana, campo por campo).
- `vacuna_aplicada` guarda mascota, vacuna, fecha de aplicación, `proxima_dosis`, `lote` y `veterinario_id`.
- La próxima dosis se calcula con el intervalo del catálogo y puede ajustarse por criterio veterinario.
- Las alertas son dentro de la aplicación; no hay WhatsApp ni SMS.

## 8. Facturación y caja

- Catálogo de conceptos con `tipo` (`SERVICIO` / `PRODUCTO`), precio por clínica, unidad y lote opcional, para que el inventario se enganche después sin migrar datos.
- Dinero con `NUMERIC(12,2)` o enteros en centavos. **Nunca** coma flotante.
- Folio consecutivo por clínica **sin huecos**, con un contador por clínica actualizado dentro de la misma transacción (`UPDATE ... RETURNING`) y restricción única. Una secuencia de PostgreSQL no sirve para esto: no es transaccional y un rollback deja hueco.
- Turno de caja con apertura, cierre y arqueo; la diferencia se registra, no se corrige.
- Estados: borrador, emitida, pagada, cancelada. Una factura emitida no se edita ni se borra.
- Comprobante interno en v1.2; la facturación fiscal queda como fase aparte.

## 9. App móvil

- **Android primero.** iOS queda fuera del primer alcance: requiere Mac, Xcode y cuenta de Apple Developer.
- Cliente público de Keycloak con Authorization Code + PKCE y redirect a esquema propio (`vetcat://callback`), abriendo el navegador del sistema. Nunca un WebView embebido.
- Tráfico sin cifrar permitido solo en configuración de debug; en release, HTTPS obligatorio.
- Tokens en el almacenamiento seguro del sistema, con refresh.
- Los contratos publicados no se rompen: la app instalada no se actualiza al instante. Se necesita versionado de API y un endpoint de versión mínima para forzar actualización.
- Sin modo sin conexión en esta entrega: solo caché de lectura y reintento.

## 10. Despliegue y operación

- **Primer entorno: la máquina del desarrollador.** Destino previsto: nube, con el mismo stack y otra configuración.
- Configuración exclusivamente por variables de entorno; sin rutas del host ni `localhost` incrustados.
- Migraciones en un contenedor one-shot antes de arrancar la API, nunca con `spring.flyway` en cada réplica.
- Logs a `stdout`; estado solo en volúmenes.
- Detalle en `docs/Despliegue_y_operacion.md`.

## 11. Consecuencias operativas

- Migraciones con Flyway, aplicadas por esquema; sin secretos ni `.env` en el repositorio.
- Respaldos por esquema **más** catálogo, usuarios de Keycloak y objetos de almacenamiento: restaurar una clínica requiere las cuatro piezas.
- Auditoría por esquema de clínica.
- CI/CD con firma de APK y keystore como secreto.

## Pendiente de confirmar

1. Mecanismo de emisión del token por clínica (ADR).
2. Matriz de roles por acción (`docs/Matriz_roles.md`).
3. Facturación fiscal (CFDI o equivalente) y su proveedor.
4. Notificaciones push.
5. Uso sin conexión.
6. Fase de inventario.
