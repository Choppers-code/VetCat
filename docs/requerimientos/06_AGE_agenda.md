# AGE · Agenda de citas

Superficies: `WEB-CLI`, `APP`.

**Estados de la cita y transiciones válidas.** Cualquier otra transición se rechaza (AGE-005).

| De | A | Quién |
|---|---|---|
| (nueva) | `PENDIENTE` | Personal de la clínica o `cliente` |
| `PENDIENTE` | `CONFIRMADA` | `veterinario` |
| `PENDIENTE`, `CONFIRMADA` | `CANCELADA` | `clinica_admin`, `recepcionista`, `veterinario` |
| `CONFIRMADA` | `ATENDIDA` | `veterinario` (al cerrar la consulta, EXP-005) |
| `CONFIRMADA` | `NO_ASISTIO` | `veterinario` |
| `NO_ASISTIO` | `PENDIENTE` (reprogramada) | `veterinario`, `clinica_admin`, `recepcionista` |
| `NO_ASISTIO` | `CANCELADA` | `veterinario`, `clinica_admin`, `recepcionista` |
| `ATENDIDA`, `CANCELADA` | — | Estados finales |

### AGE-001 · Crear una cita
- **EARS:** Cuando un `clinica_admin`, una `recepcionista` o un `veterinario` cree una cita con mascota, veterinario, consultorio, fecha, hora de inicio, duración y motivo, el sistema deberá guardarla en estado `PENDIENTE`.
- **Actor:** `clinica_admin`, `recepcionista`, `veterinario` · **Superficie:** `WEB-CLI`, `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - El propietario se deduce de la mascota.
  - Todos los campos son obligatorios salvo el motivo, que puede ser breve.
  - La duración se toma de un valor por defecto de la clínica y se puede ajustar por cita.
  - Un `veterinario` solo crea citas en su propia agenda.
  - Solo se pueden elegir mascotas y propietarios activos.
  - No se puede crear una cita en el pasado.

### AGE-002 · Validar el horario de la cita
- **EARS:** Si una cita nueva o reprogramada queda fuera del horario laboral del veterinario o dentro de un bloqueo, entonces el sistema deberá rechazarla e informar el motivo.
- **Actor:** Sistema · **Superficie:** `WEB-CLI`, `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - El mensaje distingue "fuera de horario" de "horario bloqueado" y, en este segundo caso, muestra el motivo del bloqueo.
  - La cita debe terminar dentro de la franja, no solo empezar dentro.

### AGE-003 · Impedir empalmes
- **EARS:** Si una cita nueva o reprogramada coincide en horario con otra cita no cancelada del mismo veterinario o del mismo consultorio, entonces el sistema deberá rechazarla, informar el conflicto y dejar la agenda sin cambios.
- **Actor:** Sistema · **Superficie:** Todas · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Dos peticiones simultáneas para el mismo horario: una se guarda y la otra recibe el conflicto (la garantía vive en la base de datos, no solo en la aplicación).
  - Dos citas que se tocan en el límite (una termina a las 10:00 y otra empieza a las 10:00) no cuentan como empalme.
  - Las citas `CANCELADA` no bloquean horario.
  - El conflicto se responde con código 409.

### AGE-004 · Confirmar una cita
- **EARS:** Cuando el `veterinario` confirme una cita `PENDIENTE` de su agenda, el sistema deberá pasarla a `CONFIRMADA` y notificar al propietario.
- **Actor:** `veterinario` · **Superficie:** `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Solo el veterinario asignado a la cita puede confirmarla.
  - Incluye las citas solicitadas por el `cliente` (AGE-017).
  - Una cita no confirmada se muestra en la app del veterinario con la etiqueta "Pendiente de confirmar".

### AGE-005 · Respetar las transiciones de estado
- **EARS:** Si se intenta un cambio de estado de una cita que no está en la tabla de transiciones válidas, entonces el sistema deberá rechazarlo y conservar el estado actual.
- **Actor:** Sistema · **Superficie:** Todas · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Cada cambio de estado guarda quién lo hizo y cuándo.
  - Una cita `ATENDIDA` no puede cancelarse ni reprogramarse.

### AGE-006 · Reprogramar una cita
- **EARS:** Cuando el personal reprograme una cita `PENDIENTE`, `CONFIRMADA` o `NO_ASISTIO` con nueva fecha y hora, nuevo veterinario o nuevo consultorio, el sistema deberá aplicar las validaciones de AGE-002 y AGE-003 y dejarla en `PENDIENTE`.
- **Actor:** `clinica_admin`, `recepcionista`, `veterinario` · **Superficie:** `WEB-CLI`, `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Una cita reprogramada debe volver a confirmarse.
  - Se notifica al propietario con el horario anterior y el nuevo (NOT-002).
  - Un `veterinario` reprograma solo citas de su agenda.
  - Reasignar a otro veterinario es una reprogramación.

### AGE-007 · Cancelar una cita
- **EARS:** Cuando el personal cancele una cita `PENDIENTE`, `CONFIRMADA` o `NO_ASISTIO` e indique el motivo, el sistema deberá pasarla a `CANCELADA` y notificar al propietario.
- **Actor:** `clinica_admin`, `recepcionista`, `veterinario` · **Superficie:** `WEB-CLI`, `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - El motivo es obligatorio y queda visible para el propietario.
  - El horario liberado vuelve a estar disponible de inmediato.

### AGE-008 · Solicitar la cancelación de una cita
- **EARS:** Cuando un `cliente` solicite cancelar una cita `PENDIENTE` o `CONFIRMADA` de una de sus mascotas, el sistema deberá registrar la solicitud con su motivo, dejar la cita en su estado actual y avisar al veterinario.
- **Actor:** `cliente` · **Superficie:** `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Mientras la solicitud está pendiente, la cita sigue ocupando el horario.
  - Solo puede haber una solicitud pendiente por cita.
  - La app muestra "Cancelación solicitada".
  - No se puede solicitar sobre una cita que empieza en menos de 5 horas (valor configurable).

### AGE-009 · Resolver una solicitud de cancelación
- **EARS:** Cuando el `veterinario` apruebe o rechace una solicitud de cancelación, el sistema deberá, si la aprueba, pasar la cita a `CANCELADA`, y en ambos casos notificar al cliente el resultado.
- **Actor:** `veterinario` · **Superficie:** `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - El veterinario ve las solicitudes pendientes de su agenda en una lista.
  - Al rechazar puede escribir un motivo, que el cliente ve.
  - Si el `clinica_admin` o la `recepcionista` cancelan la cita mientras la solicitud está pendiente, la solicitud se cierra como aprobada.

### AGE-010 · Marcar que el paciente no asistió
- **EARS:** Cuando el `veterinario` marque como no asistida una cita `CONFIRMADA` cuya hora de inicio ya pasó, el sistema deberá pasarla a `NO_ASISTIO`.
- **Actor:** `veterinario` · **Superficie:** `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - No se puede marcar `NO_ASISTIO` antes de la hora de inicio.
  - Desde ese estado, el veterinario puede reprogramarla (AGE-006) o cancelarla (AGE-007).
  - Una cita `NO_ASISTIO` no bloquea horario, pues ya pasó.

### AGE-011 · Pasar a atendida una cita con consulta
- **EARS:** Cuando el `veterinario` cierre una consulta ligada a una cita `CONFIRMADA`, el sistema deberá pasar esa cita a `ATENDIDA`.
- **Actor:** `veterinario` · **Superficie:** `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - La cita guarda la referencia a su consulta.
  - Si el veterinario inicia una consulta sobre una cita `PENDIENTE`, el sistema la confirma antes de iniciarla.

### AGE-012 · Ver la agenda por día y semana
- **EARS:** Cuando un usuario del personal abra la agenda y elija un día o una semana, el sistema deberá mostrar las citas de ese periodo con su hora, mascota, motivo y estado.
- **Actor:** `clinica_admin`, `recepcionista`, `veterinario` · **Superficie:** `WEB-CLI`, `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - `clinica_admin` y `recepcionista` ven la agenda de toda la clínica y filtran por veterinario y por consultorio.
  - El `veterinario` ve por defecto su agenda y puede consultar la de la clínica en solo lectura.
  - Los bloqueos se muestran en la agenda con su motivo.
  - Las citas con solicitud de cancelación pendiente llevan una marca visible.
  - La `recepcionista` ve el motivo como lo escribió quien creó la cita, no datos del expediente.

### AGE-013 · Consultar la disponibilidad
- **EARS:** Cuando el personal elija veterinario, fecha y duración al crear una cita, el sistema deberá mostrar los horarios libres que cumplen el horario laboral, los bloqueos y las citas existentes.
- **Actor:** `clinica_admin`, `recepcionista`, `veterinario` · **Superficie:** `WEB-CLI`, `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Un horario mostrado como libre puede aun así ser rechazado si otra persona lo tomó antes (AGE-003).

### AGE-014 · Ver las citas propias
- **EARS:** Cuando un `cliente` abra la sección de citas, el sistema deberá mostrarle las citas próximas y pasadas de sus mascotas en la clínica activa, con su estado.
- **Actor:** `cliente` · **Superficie:** `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Una cita nueva creada por la clínica aparece en la app sin acción del cliente.
  - Solo ve citas de mascotas de las que es propietario.
  - Donde la clínica no tenga `portal_cliente`, esta función no está disponible.

### AGE-015 · Descargar el comprobante de la cita
- **EARS:** Cuando un `cliente` elija descargar una cita, el sistema deberá generar un PDF con la clínica, la mascota, el veterinario, el consultorio, la fecha, la hora y el estado.
- **Actor:** `cliente` · **Superficie:** `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - El PDF no incluye datos clínicos.
  - Una cita `CANCELADA` descarga el comprobante con la marca de cancelada.
  - Incluye el nombre, la dirección, el teléfono y el logo de la clínica, si lo tiene (CON-010).

### AGE-016 · Citas a domicilio
- **EARS:** Donde el plan de la clínica incluya `visitas_domicilio`, el sistema deberá permitir crear citas de tipo domicilio con la dirección del propietario.
- **Actor:** `clinica_admin`, `recepcionista`, `veterinario` · **Superficie:** `WEB-CLI`, `APP` · **Prioridad:** Podría
- **Criterios de aceptación:**
  - Una cita a domicilio no usa consultorio; sí ocupa el horario del veterinario.
  - Si el propietario no tiene dirección, no se puede crear y se pide registrarla (PRO-003).
  - La dirección se muestra al veterinario en su agenda.
  - Sin la función en el plan, el tipo de cita no existe (PLA-004).

### AGE-017 · Solicitar una cita
- **EARS:** Donde el plan de la clínica incluya `portal_cliente`, cuando un `cliente` elija una de sus mascotas, un veterinario, un horario libre y un motivo, el sistema deberá crear la cita en estado `PENDIENTE` y notificar al veterinario.
- **Actor:** `cliente` · **Superficie:** `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - El cliente solo ve los horarios libres según el horario laboral, los bloqueos y las citas existentes (AGE-013).
  - Se aplican las validaciones de horario y empalme (AGE-002, AGE-003).
  - La cita es siempre en consultorio; las citas a domicilio las crea el personal (AGE-016).
  - La cita queda pendiente hasta que el veterinario la confirme (AGE-004), igual que las creadas por la clínica.
  - El cliente solo solicita citas para mascotas activas de las que es propietario.

### AGE-018 · Limitar las citas pendientes del cliente
- **EARS:** Si un `cliente` ya tiene 3 citas en estado `PENDIENTE` en la clínica, entonces el sistema deberá rechazar una nueva solicitud e informarle el límite.
- **Actor:** Sistema · **Superficie:** `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Las citas `CONFIRMADA`, `ATENDIDA` y `CANCELADA` no cuentan para el límite.
  - El límite no aplica a las citas que crea el personal.
  - El valor de 3 es configurable.
