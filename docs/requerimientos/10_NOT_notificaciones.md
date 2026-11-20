# NOT · Notificaciones

Superficie: `APP` (vistas de cliente y de veterinario).

Toda notificación se guarda en una bandeja dentro de la app y, además, se envía como notificación push al teléfono. WhatsApp y SMS están fuera de alcance. Un propietario sin cuenta vinculada no recibe notificaciones; la clínica lo contacta por su cuenta.

### NOT-001 · Avisar la cancelación de citas por un bloqueo
- **EARS:** Cuando un bloqueo cancele una cita (CON-008), el sistema deberá crear una notificación para el `cliente` vinculado al propietario con la fecha de la cita, la mascota y el motivo del bloqueo.
- **Actor:** Sistema · **Superficie:** `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Hay una notificación por cita cancelada.
  - Si el propietario no tiene cuenta vinculada, la cita aparece marcada en la lista de citas afectadas (CON-008) como "sin notificar" para que la clínica llame.

### NOT-002 · Avisar los cambios de una cita
- **EARS:** Cuando una cita se confirme, se reprograme o se cancele, el sistema deberá notificar al `cliente` vinculado con el estado nuevo y, si cambió, el horario nuevo.
- **Actor:** Sistema · **Superficie:** `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Crear una cita en estado `PENDIENTE` por parte del personal también notifica, con el texto "Cita registrada".
  - Un cambio hecho por el propio cliente (solicitud) no genera un aviso redundante.

### NOT-003 · Avisar al veterinario de una solicitud del cliente
- **EARS:** Cuando un `cliente` solicite una cita o la cancelación de una cita, el sistema deberá notificar al `veterinario` asignado.
- **Actor:** Sistema · **Superficie:** `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - La notificación lleva a la cita por confirmar (AGE-004) o a la solicitud de cancelación (AGE-009).

### NOT-004 · Avisar al cliente el resultado de su solicitud
- **EARS:** Cuando el `veterinario` apruebe o rechace una solicitud de cancelación, o confirme una cita solicitada por el cliente, el sistema deberá notificar el resultado al `cliente` que la hizo, con el motivo si lo hay.
- **Actor:** Sistema · **Superficie:** `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Se envía una sola notificación por solicitud.

### NOT-005 · Bandeja de notificaciones
- **EARS:** Cuando un usuario abra la bandeja de notificaciones, el sistema deberá mostrarle las de la clínica activa ordenadas de la más reciente a la más antigua, con las no leídas destacadas.
- **Actor:** `cliente`, `veterinario` · **Superficie:** `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Un ícono muestra cuántas hay sin leer.
  - Abrir una la marca como leída, y existe "marcar todas como leídas".
  - Se conservan 90 días.
  - Cada notificación lleva al elemento que menciona (cita, solicitud).

### NOT-006 · Recordatorio de cita
- **EARS:** Donde el plan de la clínica incluya `recordatorios`, cuando falten 24 horas para una cita `CONFIRMADA`, el sistema deberá notificar al `cliente` vinculado.
- **Actor:** Sistema · **Superficie:** `APP` · **Prioridad:** Debería
- **Criterios de aceptación:**
  - No se envía si la cita se confirmó, o se creó, con menos de 24 horas de anticipación.
  - No se envía para citas canceladas.
  - Se envía una sola vez por cita.

### NOT-007 · Recordatorio de vacuna
- **EARS:** Donde el plan de la clínica incluya `recordatorios`, cuando falten 7 días para la próxima dosis de una mascota, el sistema deberá notificar al `cliente` vinculado.
- **Actor:** Sistema · **Superficie:** `APP` · **Prioridad:** Debería
- **Criterios de aceptación:**
  - No se envía si la dosis ya se aplicó.
  - Se envía una vez por dosis.

### NOT-008 · Enviar notificaciones push
- **EARS:** Cuando se cree una notificación para un usuario con un dispositivo registrado, el sistema deberá enviarla también como notificación push a ese dispositivo.
- **Actor:** Sistema · **Superficie:** `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - El texto del push no incluye datos clínicos, solo el aviso general (por ejemplo, "Tienes una solicitud de cancelación por revisar").
  - Al tocar el push, la app abre el elemento que menciona.
  - Si el envío falla, la notificación sigue disponible en la bandeja.
  - El push de recordatorios solo se envía donde el plan incluye `recordatorios`.

### NOT-009 · Registrar el dispositivo del usuario
- **EARS:** Cuando un usuario inicie sesión en la app y autorice las notificaciones, el sistema deberá registrar su dispositivo para recibir push, y cuando cierre sesión deberá eliminar ese registro.
- **Actor:** `cliente`, `veterinario` · **Superficie:** `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Un usuario puede tener varios dispositivos registrados.
  - Tras cerrar sesión, el dispositivo deja de recibir avisos de esa cuenta.
  - Si el usuario no autoriza las notificaciones, la app sigue funcionando con la bandeja.
