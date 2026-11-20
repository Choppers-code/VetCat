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

- `catalogo.usuario_clinica (keycloak_id, clinica_id, rol, estado)` es la **única** fuente de verdad de la pertenencia. No vive dentro del esquema de la clínica, porque un usuario puede pertenecer a varias.
- Un usuario puede trabajar en más de una clínica. Al iniciar sesión elige la clínica activa y recibe un token acotado a ella.
- Ese token lo emite el backend, no Keycloak: `POST /api/v1/sesion/clinicas/{id}` valida la pertenencia en `usuario_clinica` y devuelve un token de tenant firmado por la API, de 15 minutos, con `sub`, `tenant_id` y el rol efectivo de **esa** clínica. El detalle, los dos validadores y las alternativas descartadas están en el [ADR-0003](adr/0003-token-por-clinica.md).
- El token de identidad de Keycloak solo vale en `/api/v1/sesion/**` y `/api/v1/plataforma/**`. Ningún endpoint de negocio de una clínica lo acepta: sin `tenant_id` no hay esquema. `administrador_plataforma` es el único rol que se toma del realm, porque no pertenece a ninguna clínica.
- Lo que no se negocia: el `tenant_id` llega en un token firmado y **el rol efectivo sale de `usuario_clinica` para esa clínica**, no del rol de realm. Un usuario que es administrador en la clínica A y recepcionista en la B no puede llevar el rol de A al token de B.
- Keycloak: un solo realm `vetcat` con los roles como catálogo de valores, y la asignación por clínica en base de datos.

## 4. Roles y permisos

Roles: `administrador_plataforma`, `administrador_clinica`, `veterinario`, `recepcionista`, `dueno`.

- La autorización se aplica con `@PreAuthorize` en el backend.
- `administrador_plataforma` aprovisiona clínicas y da soporte; **no** accede a datos clínicos.
- `recepcionista` gestiona agenda, propietarios y mascotas; **no** accede a datos clínicos ni registra vacunas.
- `dueno` solo lectura de lo suyo, con campos explícitamente permitidos, y el backend verifica la propiedad de la mascota en **cada** consulta.

La matriz completa de acciones por rol vive en [`docs/Matriz_roles.md`](Matriz_roles.md) y se implementa tal cual: lo que no figura en ella no se escribe con `@PreAuthorize`.

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

## 6. Expediente clínico: entidades separadas y con versiones

- `consulta` es el encuentro; `diagnostico`, `tratamiento`, `receta` y `nota` son entidades propias, no columnas de texto. La consulta puede o no estar ligada a una cita. La receta forma parte de la consulta y se versiona con ella.
- Solo el veterinario edita el expediente. Cada edición crea una versión nueva; las anteriores se conservan con autor y fecha y nunca se sobrescriben ni se borran.
- Toda consulta registra qué veterinario la realizó y las fechas y horas.
- El `cliente` ve solo el resumen permitido (fecha, motivo, diagnóstico, tratamiento y receta) de la última versión de las consultas cerradas.
- Los cambios importantes y la **lectura** del expediente se registran en auditoría.
- El PDF de la receta y el comprobante de la cita se generan en el momento, sin almacenarse, con los datos de la clínica y su logo opcional: una imagen PNG o JPG de hasta 500 KB guardada en la base.

## 7. Vacunas

- `vacuna_catalogo` es **por clínica**, en el esquema de la clínica. No hay catálogo global ni sembrado por migración, ni tabla de override. Cada vacuna tiene un solo intervalo; los esquemas de varias dosis con intervalos distintos quedan fuera.
- `vacuna_aplicada` guarda mascota, vacuna, fecha de aplicación, `proxima_dosis`, `lote` y `veterinario_id`.
- La próxima dosis se calcula con el intervalo del catálogo y puede ajustarse por criterio veterinario; el valor calculado original se conserva.
- Las alertas son dentro de la aplicación: vencidas y próximas (**30 días**, configurable) para `veterinario` y `clinica_admin`. La `recepcionista` consulta el catálogo pero no ve aplicaciones ni próximas dosis.

## 8. Ventas, caja y tickets

- Todo es opcional por plan: `ventas`, y `turno_caja` e `inventario`, que requieren `ventas`.
- Catálogo de conceptos con `tipo` (`SERVICIO` / `PRODUCTO`) y precio por clínica. El producto puede ser **controlado**.
- Dinero con `NUMERIC(12,2)` o enteros en centavos. **Nunca** coma flotante. Solo efectivo.
- Folio consecutivo por clínica **sin huecos**, con un contador por clínica actualizado dentro de la misma transacción (`UPDATE ... RETURNING`) y restricción única. Una secuencia de PostgreSQL no sirve para esto: no es transaccional y un rollback deja hueco.
- Estados del ticket: `BORRADOR`, `EMITIDO`, `PAGADO`, `CANCELADO`. El ticket es un comprobante interno, no una factura. Un ticket emitido no se edita ni se borra.
- Solo `clinica_admin` puede cancelar un ticket, con motivo y auditoría y conservando el folio, salvo que pertenezca a un turno de caja ya cerrado.
- Turno de caja con apertura, cierre y arqueo; la diferencia se registra, no se corrige.
- **Inventario simple.** Las entradas de stock llevan su propia fecha de caducidad (obligatoria si el producto tiene caducidad). El stock se descuenta al emitir el ticket, empezando por lo que caduca antes; nunca queda negativo y se devuelve al cancelar el ticket. Hay alertas de stock bajo y de caducidad a 30 días. Si el plan no incluye `inventario`, los productos son solo conceptos con precio.
- **Reportes:** opcionales por plan, solo lectura, con conteos y totales por rango de fechas.
- La facturación fiscal queda como fase aparte.

## 9. App móvil

- **Una sola app Android** para `veterinario` y `cliente`, con una vista por rol según la clínica activa. Los dos conjuntos de pantallas viven en carpetas separadas (`features/veterinario` y `features/cliente`) para poder partir la app más adelante. Si el usuario tiene ambos roles en la clínica, puede cambiar de vista sin cerrar sesión. `clinica_admin` y `recepcionista` usan la consola web.
- **Android primero.** iOS queda fuera del primer alcance: requiere Mac, Xcode y cuenta de Apple Developer.
- Cliente público de Keycloak con Authorization Code + PKCE y redirect a esquema propio (`vetcat://callback`), abriendo el navegador del sistema. Nunca un WebView embebido.
- Tráfico sin cifrar permitido solo en configuración de debug; en release, HTTPS obligatorio.
- Tokens en el almacenamiento seguro del sistema, con refresh.
- Los contratos publicados no se rompen: la app instalada no se actualiza al instante. Se necesita versionado de API y un endpoint de versión mínima para forzar actualización.
- Sin modo sin conexión en esta entrega: solo caché de lectura y reintento.
- Notificaciones push con Firebase Cloud Messaging ([ADR-0008](adr/0008-notificaciones-bandeja-y-push.md)), con permiso del usuario.

### Notificaciones

- Toda notificación se guarda en una **bandeja** por usuario y clínica (90 días) y se envía además por push. La bandeja es la fuente de verdad.
- El texto del push no incluye datos clínicos.
- Los tokens de dispositivo se guardan por usuario y se borran al cerrar sesión.
- Los trabajos programados (recordatorios, vencimiento de planes) reciben el tenant de forma explícita, como exige §2.
- El envío va detrás de un adaptador. Si el servicio de push falla, la notificación sigue en la bandeja.

## 10. Despliegue y operación

- **Primer entorno: la máquina del desarrollador.** Destino previsto: nube, con el mismo stack y otra configuración.
- Configuración exclusivamente por variables de entorno; sin rutas del host ni `localhost` incrustados.
- Migraciones en un contenedor one-shot antes de arrancar la API, nunca con `spring.flyway` en cada réplica.
- Logs a `stdout`; estado solo en volúmenes.
- El compose de desarrollo incluye un buzón de correo de pruebas para la recuperación de contraseña y las invitaciones. Las credenciales de Firebase son un secreto fuera del repositorio.
- Detalle en `docs/Despliegue_y_operacion.md`.

## 11. Consecuencias operativas

- Migraciones con Flyway, aplicadas por esquema; sin secretos ni `.env` en el repositorio.
- Respaldos por esquema **más** catálogo y usuarios de Keycloak: restaurar una clínica requiere las tres piezas. Ya no hay objetos de almacenamiento que respaldar: el logo vive en la base y los PDF se generan al vuelo.
- Auditoría por esquema de clínica.
- CI/CD con firma de APK y keystore como secreto.

## Pendiente de confirmar

1. Facturación fiscal (CFDI o equivalente) y su proveedor.
