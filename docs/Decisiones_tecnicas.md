# Decisiones técnicas

Correcciones y definiciones que resuelven las contradicciones entre `historico/Propuesta_VetCat.md` y `historico/Plan_inicial.md`. Cada decisión es la que se implementa; las dos fuentes originales quedan como referencia histórica.

## 1. Multi-tenancy: catálogo global + esquema por clínica

- Esquema `catalogo` global y un esquema `clinica_xxx` por clínica, en la misma base `vetcat_db`.
- **Dos unidades de persistencia**: una sin multi-tenant para `catalogo`, otra con `CurrentTenantIdentifierResolver` y `MultiTenantConnectionProvider` para los esquemas de clínica. Mezclar entidades globales y de tenant en una sola unidad es la parte más frágil de esta fase.
- El nombre del esquema lo genera el servidor a partir de un identificador interno. Nunca lo envía el cliente.
- `catalogo.clinica` incluye `zona_horaria` en formato IANA. Los horarios de atención y el cálculo de disponibilidad se hacen en esa zona; todo instante se guarda como `timestamptz` y las fechas sin hora como `date`.
- Flyway con dos ubicaciones de migración: `catalogo` y `tenant`. El aprovisionamiento de una clínica es idempotente: registra en el catálogo, crea el esquema **vacío**, lo deja en la tabla de estado como pendiente de migrar y registra al administrador en Keycloak, con estados persistidos y un job de reconciliación si un paso falla. Quien aplica Flyway es siempre el runner one-shot, nunca la API: ver `Despliegue_y_operacion.md` §5.

## 2. El tenant sale solo del token

- `tenant_id` proviene exclusivamente del JWT firmado y se valida contra la relación usuario-clínica en base de datos. Nunca de un parámetro, cuerpo o cabecera.
- **Si no hay tenant, no hay respuesta.** Un token sin `tenant_id`, o con una clínica que no figura activa en `usuario_clinica`, se rechaza con `401` o `403`. Nunca se resuelve un esquema por defecto: aquí el silencio no es un valor neutro, significa leer datos de otra clínica.
- La resolución es por conexión y transacción. **Ningún bean singleton guarda el tenant actual**; un campo compartido cruza datos entre clínicas bajo concurrencia.
- Los servicios son sin estado, con inyección por constructor.
- El contexto de tenant se limpia en un `finally` al devolver la conexión, se prohíbe la caché de segundo nivel de Hibernate y los jobs (`@Async`, `@Scheduled`) reciben el tenant explícitamente, nunca lo heredan del hilo.

## 3. Pertenencia usuario-clínica: tabla global y multi-clínica

- `catalogo.usuario_clinica` tiene una fila por `(keycloak_id, clinica_id, rol)`, con `estado`, y es la **única** fuente de verdad de la pertenencia. No vive dentro del esquema de la clínica, porque un usuario puede pertenecer a varias.
- Un usuario puede tener **varios roles en una misma clínica** (por ejemplo `veterinario` y `clinica_admin`). Sus permisos son la unión de los roles que tiene en **esa** clínica. Ver el [ADR-0006](adr/0006-varios-roles-por-clinica.md).
- Un usuario puede trabajar en más de una clínica. Al iniciar sesión elige la clínica activa y recibe un token acotado a ella.
- Ese token lo emite el backend, no Keycloak: `POST /api/v1/sesion/clinicas/{id}` valida la pertenencia en `usuario_clinica` y devuelve un token de tenant firmado por la API, de 15 minutos, con `sub`, `tenant_id` y la lista `roles` efectivos de **esa** clínica. El detalle, los dos validadores y las alternativas descartadas están en el [ADR-0003](adr/0003-token-por-clinica.md).
- El token de identidad de Keycloak solo vale en `/api/v1/sesion/**` y `/api/v1/plataforma/**`. Ningún endpoint de negocio de una clínica lo acepta: sin `tenant_id` no hay esquema. `super_admin` es el único rol que se toma del realm, porque no pertenece a ninguna clínica.
- Lo que no se negocia: el `tenant_id` llega en un token firmado y **los roles efectivos salen de `usuario_clinica` para esa clínica**, no del rol de realm. Un usuario que es `clinica_admin` en la clínica A y `recepcionista` en la B no puede llevar el rol de A al token de B.
- El `cliente` es un miembro de la clínica con el rol `cliente`. Su ficha de propietario se enlaza mediante un código de vinculación de un solo uso que vence a los **2 días**; la pertenencia sigue siendo la única fuente de verdad.
- Keycloak: un solo realm `vetcat` con los roles como catálogo de valores, y la asignación por clínica en base de datos.

## 4. Roles y permisos

Roles: `super_admin`, `clinica_admin`, `veterinario`, `recepcionista`, `cliente`.

- La autorización se aplica con `@PreAuthorize` en el backend.
- `super_admin` aprovisiona clínicas y da soporte; **no** accede a datos clínicos, de pacientes ni de propietarios.
- `clinica_admin` administra su clínica; **lee** el expediente y la auditoría de su clínica, sin editarlo.
- `veterinario` confirma las citas de su agenda y resuelve las solicitudes de cancelación.
- `recepcionista` gestiona agenda, propietarios, mascotas y caja; **ve el catálogo de vacunas**, pero no registra vacunas ni ve cartillas ni datos clínicos.
- `cliente` solo lectura de lo suyo, más las solicitudes de cita y de cancelación, con campos explícitamente permitidos; el backend verifica la propiedad de la mascota en **cada** consulta.

La matriz completa de acciones por rol vive en [`docs/Matriz_roles.md`](Matriz_roles.md) y se implementa tal cual: lo que no figura en ella no se escribe con `@PreAuthorize`.

## 5. Agenda: el empalme lo impide la base de datos

- `cita` incluye `tipo` (`CONSULTORIO` / `DOMICILIO`), `consultorio_id` obligatorio solo para el tipo `CONSULTORIO`, `direccion` para el tipo `DOMICILIO`, `inicio`, `fin`, `veterinario_id`, `mascota_id`, `motivo`, `estado`, `cancelada_por`, `motivo_cancelacion` y `reprogramada_de`.
- Estados: `PENDIENTE`, `CONFIRMADA`, `ATENDIDA`, `NO_ASISTIO`, `CANCELADA`. La cita nace `PENDIENTE` y la confirma el veterinario. Las transiciones válidas están en la tabla de [`requerimientos/06_AGE_agenda.md`](requerimientos/06_AGE_agenda.md), incluida `NO_ASISTIO → PENDIENTE` al reprogramar. Se modelan como `varchar` con `CHECK`, no como tipo PostgreSQL: agregar un valor a un `CREATE TYPE` obliga a un `ALTER TYPE` en cada esquema.
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
  ALTER COLUMN inicio SET NOT NULL,
  ALTER COLUMN fin SET NOT NULL,
  ADD CONSTRAINT cita_rango_valido CHECK (fin > inicio),
  ADD CONSTRAINT cita_consultorio_requerido
    CHECK (tipo <> 'CONSULTORIO' OR consultorio_id IS NOT NULL),
  ADD CONSTRAINT cita_consultorio_no_aplica
    CHECK (tipo <> 'DOMICILIO' OR consultorio_id IS NULL);
```

- `inicio` y `fin` van `NOT NULL` por la misma razón que las otras tres: `fin > inicio` no detecta un nulo —evalúa a `NULL` y la restricción pasa— y una cita con `inicio` nulo deja el rango abierto por abajo, con lo que bloquea la agenda entera de ese veterinario. No es una fuga de datos, es la agenda de una clínica caída por una fila.

- Un `SELECT` previo no basta: dos recepcionistas pueden reservar a la vez. El rechazo de la base se traduce en un `409` que la app muestra como conflicto de horario.
- Se agregan `consultorio`, `horario_atencion` (por veterinario y por consultorio, con excepciones por fecha), `bloqueo_agenda` y la duración por servicio en el catálogo de conceptos.
- El horario de atención se valida en el servicio; la restricción de exclusión solo impide empalmes, no citas fuera de horario.
- La cita registra su **origen** (personal de la clínica o `cliente`). La **solicitud de cancelación** es una entidad propia, con estado (pendiente, aprobada, rechazada) y motivo.
- El `cliente` puede tener como máximo **3 citas `PENDIENTE`** y solo puede solicitar cancelar con **5 horas** de anticipación. Ambos valores son configuración, no constantes.
- Las citas `DOMICILIO` solo existen si el plan de la clínica incluye `visitas_domicilio`.
- La cita que crea el `cliente` respeta el horario laboral y los bloqueos igual que la del personal.
- El bloqueo que cae sobre citas activas las cancela con el motivo del bloqueo y dispara las notificaciones.

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

## 12. Planes y funciones

- Hay un **único modelo de datos**: el plan activa funciones y fija límites, pero no cambia el esquema. En `catalogo` viven `plan`, `plan_funcion`, `plan_limite` y `pago_plan`; la clínica tiene un plan y una `vigente_hasta`.
- Las funciones son `portal_cliente`, `recordatorios`, `ventas`, `turno_caja`, `inventario`, `visitas_domicilio` y `reportes`. `turno_caja` e `inventario` requieren `ventas`.
- `plan_limite` fija el máximo de usuarios, de veterinarios y de mascotas. Un límite vacío significa sin límite.
- **Las funciones y los límites se verifican en el servidor en cada petición.** No vienen en el token ni los informa la app.
- Estados de la clínica: `ACTIVA`, `SUSPENDIDA` y `BAJA`. La suspensión por vencimiento es automática tras **3 días de gracia** sin pago registrado y no toca los datos. La baja conserva los datos y no se puede revertir desde la consola.
- El cobro de los planes es manual: no hay pagos en línea ni datos bancarios. Ver el [ADR-0007](adr/0007-planes-y-funciones.md).

## Pendiente de confirmar

1. Facturación fiscal (CFDI o equivalente) y su proveedor.
2. Notificaciones push.
3. Fase de inventario.
