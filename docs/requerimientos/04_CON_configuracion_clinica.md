# CON · Configuración de la clínica

Superficie: `WEB-CLI`. Actor principal: `clinica_admin`.

### CON-001 · Editar los datos generales de la clínica
- **EARS:** Cuando el `clinica_admin` modifique los datos generales de su clínica (nombre comercial, dirección, teléfono, correo y horario de atención), el sistema deberá guardar los cambios.
- **Actor:** `clinica_admin` · **Superficie:** `WEB-CLI` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - La zona horaria la fija el `super_admin` (CLI-003); el `clinica_admin` la ve pero no la cambia.

### CON-002 · Crear personal de la clínica
- **EARS:** Cuando el `clinica_admin` registre a un miembro del personal con nombre, correo y uno o más roles (`clinica_admin`, `veterinario`, `recepcionista`), el sistema deberá crear su usuario en la clínica y enviarle un correo para que establezca su contraseña.
- **Actor:** `clinica_admin` · **Superficie:** `WEB-CLI` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Si el correo ya pertenece a un usuario de VetCat, se le agrega la clínica y el rol sin crear otra cuenta.
  - El `clinica_admin` no puede crear usuarios con rol `super_admin` ni `cliente`.
  - Si crear el usuario superaría el límite del plan, se rechaza (PLA-003).

### CON-003 · Editar roles y datos del personal
- **EARS:** Cuando el `clinica_admin` cambie los datos o los roles de un miembro del personal, el sistema deberá aplicar los cambios a partir de su siguiente token de clínica.
- **Actor:** `clinica_admin` · **Superficie:** `WEB-CLI` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - El `clinica_admin` no puede quitarse a sí mismo el rol `clinica_admin` si es el último de la clínica.
  - La asignación de roles se valida contra la matriz de funciones, no contra lo que envía el cliente.

### CON-004 · Desactivar y reactivar personal
- **EARS:** Cuando el `clinica_admin` desactive a un miembro del personal, el sistema deberá impedirle el acceso a la clínica y conservar su historial.
- **Actor:** `clinica_admin` · **Superficie:** `WEB-CLI` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Las consultas, citas y tickets que registró siguen mostrando su nombre.
  - Las citas futuras de un veterinario desactivado se listan para reasignarlas (AGE-006).
  - Un usuario nunca se borra; se puede reactivar.

### CON-005 · Mantener consultorios
- **EARS:** Cuando el `clinica_admin` cree, edite o desactive un consultorio, el sistema deberá guardar el cambio y dejar de ofrecer el consultorio desactivado para citas nuevas.
- **Actor:** `clinica_admin` · **Superficie:** `WEB-CLI` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Un consultorio con citas futuras no se desactiva sin antes listar esas citas.
  - Una clínica debe tener al menos un consultorio activo para agendar citas en sede.

### CON-006 · Definir el horario laboral de un veterinario
- **EARS:** Cuando el `clinica_admin` defina el horario semanal de un veterinario (días y franjas de atención), el sistema deberá ofrecer citas con ese veterinario únicamente dentro de esas franjas.
- **Actor:** `clinica_admin` · **Superficie:** `WEB-CLI` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Un veterinario sin horario definido no aparece como disponible.
  - Cambiar el horario no cancela citas ya agendadas; las que queden fuera del nuevo horario se listan para revisión.

### CON-007 · Bloquear horarios
- **EARS:** Cuando el `clinica_admin` o un `veterinario` bloquee un periodo de un veterinario o de un consultorio con un motivo, el sistema deberá impedir citas nuevas en ese periodo.
- **Actor:** `clinica_admin`, `veterinario` · **Superficie:** `WEB-CLI`, `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - El motivo es obligatorio (ausencia, mantenimiento, festivo, otro).
  - Un veterinario solo bloquea su propia agenda; el `clinica_admin` bloquea cualquiera y también consultorios.
  - Se puede bloquear el día completo o un rango de horas, y se puede repetir en varios días.

### CON-008 · Detectar citas afectadas por un bloqueo
- **EARS:** Cuando se cree un bloqueo que coincida con citas `PENDIENTE` o `CONFIRMADA`, el sistema deberá mostrar al autor del bloqueo la lista de citas afectadas antes de confirmarlo.
- **Actor:** `clinica_admin`, `veterinario` · **Superficie:** `WEB-CLI`, `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - La lista muestra fecha, hora, mascota y propietario.
  - El autor elige entre confirmar el bloqueo cancelando esas citas (con el motivo del bloqueo) o dejar el bloqueo sin guardar.
  - Al confirmar, cada cita afectada pasa a `CANCELADA` y se notifica a su propietario (NOT-001).
  - Las citas ya `ATENDIDA` o `NO_ASISTIO` no se tocan.
  - Después, el autor puede consultar la lista de citas canceladas por ese bloqueo.

### CON-009 · Consultar y quitar bloqueos
- **EARS:** Cuando el `clinica_admin` o el `veterinario` consulte los bloqueos, el sistema deberá listarlos con su periodo, su motivo y su autor y permitir eliminar los vigentes.
- **Actor:** `clinica_admin`, `veterinario` · **Superficie:** `WEB-CLI`, `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Eliminar un bloqueo libera el horario, pero no restaura las citas ya canceladas.
  - Un veterinario solo elimina los suyos.

### CON-010 · Configurar la identidad de la clínica en los documentos
- **EARS:** Cuando el `clinica_admin` cargue el logo de su clínica, el sistema deberá guardarlo y usarlo en los PDF de citas y recetas y en los tickets.
- **Actor:** `clinica_admin` · **Superficie:** `WEB-CLI` · **Prioridad:** Debería
- **Criterios de aceptación:**
  - El logo es opcional. Sin logo, los documentos llevan solo el nombre, la dirección y el teléfono de la clínica (CON-001).
  - Se aceptan imágenes PNG o JPG de hasta 500 KB.
  - El `clinica_admin` puede reemplazar o quitar el logo.
