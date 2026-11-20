# Requerimientos no funcionales

Reglas que cruzan todos los módulos. No son una función que el usuario invoque: son condiciones que toda función debe cumplir. Cada módulo las hereda; ningún RF las repite.

## Aislamiento entre clínicas (AIS)

- **RNF-AIS-001.** El sistema deberá resolver la clínica de cada petición únicamente desde el token firmado, validado contra la relación usuario↔clínica, y nunca desde un parámetro, cabecera o cuerpo de la petición. *(antes RF-004)*
- **RNF-AIS-002.** Si no es posible determinar la clínica de una petición, entonces el sistema deberá rechazarla sin acceder a ningún dato.
- **RNF-AIS-003.** El sistema deberá mantener los datos de cada clínica en un esquema propio, de modo que una consulta de una clínica no pueda alcanzar datos de otra.
- **RNF-AIS-004.** El sistema deberá emitir el token de clínica con vigencia de 15 minutos, y aceptar el token de Keycloak solo en las rutas de sesión y de plataforma (ADR-0003).

## Seguridad y privacidad (SEG)

- **RNF-SEG-001.** El sistema deberá exigir autenticación en toda ruta salvo las de inicio de sesión y recuperación.
- **RNF-SEG-002.** El sistema deberá validar el permiso de cada operación en el backend contra la matriz de funciones por rol; ocultar una opción en la interfaz no es autorización. *(antes RF-008)*
- **RNF-SEG-003.** El sistema deberá verificar la propiedad de cada mascota, cita y consulta que pide un `cliente` en cada petición, no solo en la primera. *(antes RF-033)*
- **RNF-SEG-004.** El sistema deberá impedir que el `super_admin` acceda a datos clínicos, de pacientes, de propietarios, de citas o de caja de las clínicas; solo ve datos administrativos y conteos (CLI-007, CLI-008). *(Nuevo)*
- **RNF-SEG-005.** El sistema deberá rechazar los tokens vencidos, y limitar la ventana de un acceso retirado a la vigencia del token (15 minutos).
- **RNF-SEG-006.** El sistema deberá guardar las contraseñas únicamente en Keycloak y los tokens móviles en almacenamiento seguro del dispositivo.
- **RNF-SEG-008.** El sistema no deberá capturar ni almacenar datos bancarios ni de tarjetas; el registro de pagos de planes es manual y el pago de tickets es en efectivo.
- **RNF-SEG-007.** El sistema no deberá incluir contraseñas, claves ni archivos `.env` en el repositorio.

## Auditoría (AUD)

- **RNF-AUD-001.** El sistema deberá registrar quién, cuándo y qué en las operaciones sensibles: altas y bajas de usuarios, cambios de rol, cambios de plan, cancelación de tickets, ajustes de stock, bajas de clínica. *(antes RF-044)*
- **RNF-AUD-002.** El sistema deberá registrar cada lectura de un expediente clínico, incluida la del propio cliente.
- **RNF-AUD-003.** El `clinica_admin` deberá poder consultar la auditoría de su clínica, y el `super_admin` la de la plataforma; ninguno puede modificarla ni borrarla.

## Datos (DAT)

- **RNF-DAT-001.** El sistema deberá responder toda petición de la API con el envoltorio `{success, message, data}`, también en errores. *(antes RF-043)*
- **RNF-DAT-002.** El sistema no deberá borrar físicamente usuarios, propietarios, mascotas, consultas ni tickets; los desactiva.
- **RNF-DAT-003.** El sistema deberá manejar los importes con `NUMERIC(12,2)` y nunca con punto flotante. *(antes RF-040)*
- **RNF-DAT-004.** El sistema deberá garantizar en la base de datos las reglas de integridad críticas: empalme de citas, folio sin huecos y stock no negativo.
- **RNF-DAT-006.** El sistema deberá conservar los datos de una clínica dada de baja; no ofrece eliminarlos.
- **RNF-DAT-005.** El sistema deberá guardar fechas y horas con zona horaria y mostrarlas en la zona de la clínica.

## Respaldo y operación (OPE)

- **RNF-OPE-001.** El sistema deberá permitir respaldar y restaurar los datos de una clínica de forma independiente de las demás. *(antes RF-045)*
- **RNF-OPE-002.** El sistema deberá poder levantarse completo (backend, base de datos, Keycloak y un buzón de correo de pruebas) con Docker Compose en la máquina de desarrollo.
- **RNF-OPE-004.** El sistema deberá enviar las notificaciones push mediante un servicio externo (Firebase Cloud Messaging), cuyas credenciales se guardan fuera del repositorio; si el servicio no está disponible, las notificaciones siguen disponibles en la bandeja de la app.
- **RNF-OPE-003.** El sistema deberá aplicar las migraciones de cada esquema mediante el ejecutor único, no desde la aplicación.

## Usabilidad y plataforma (USA)

- **RNF-USA-001.** El sistema deberá ofrecer una sola app móvil para Android, con una vista para el cliente y otra para el veterinario según el rol del usuario en la clínica activa; iOS queda fuera de esta versión.
- **RNF-USA-002.** El sistema deberá mostrar mensajes de error en español, comprensibles, sin trazas técnicas.
- **RNF-USA-003.** El sistema deberá requerir conexión; el modo sin conexión queda fuera.
- **RNF-USA-004.** La consola web deberá usar un único origen para la API (ADR-0004).

## Valores por defecto configurables

Los números de los RF (5 horas, 2 días, 3 días de gracia, 30 días, 90 días, 15 minutos, 3 citas pendientes) son valores por defecto. Se guardan como configuración y no como constantes en el código, para ajustarlos sin cambiar la lógica.
