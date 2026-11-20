# EXP · Expediente clínico

Superficie: `APP`. Actor principal: `veterinario`.

Solo el `veterinario` registra y edita. El `clinica_admin` lee. La `recepcionista` y el `super_admin` no acceden a nada de este módulo. El `cliente` ve solo un resumen (POR-004).

**Retirado:** adjuntar fotografías o estudios (antes RF-022). El análisis es presencial y el equipo decidió no incluirlo; no hay almacenamiento de archivos en esta versión. Si el equipo lo retoma, será un módulo nuevo.

### EXP-001 · Iniciar una consulta con cita
- **EARS:** Cuando el `veterinario` inicie una consulta desde una cita `CONFIRMADA` de su agenda, el sistema deberá abrir una consulta ligada a esa cita, a su mascota y a él.
- **Actor:** `veterinario` · **Superficie:** `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - La cita puede tener como máximo una consulta.
  - Los datos de mascota y motivo de la cita aparecen precargados.

### EXP-002 · Registrar los datos de la consulta
- **EARS:** Cuando el `veterinario` capture los datos de una consulta abierta, el sistema deberá guardar motivo, peso, temperatura, anamnesis, exploración, diagnóstico, tratamiento y notas.
- **Actor:** `veterinario` · **Superficie:** `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Diagnóstico y tratamiento son obligatorios para cerrar la consulta; los demás campos son opcionales.
  - El peso capturado actualiza el peso de la mascota (PRO-006).
  - Una consulta abierta se puede guardar como borrador y retomar.

### EXP-003 · Registrar una consulta sin cita
- **EARS:** Cuando el `veterinario` inicie una consulta sin cita para una mascota activa, el sistema deberá abrirla sin ligarla a ninguna cita.
- **Actor:** `veterinario` · **Superficie:** `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - El flujo, los campos y los permisos son los mismos que con cita.
  - Cerrarla no modifica ninguna cita.
  - Aparece en el historial de la mascota igual que las demás.

### EXP-004 · Registrar la receta
- **EARS:** Cuando el `veterinario` agregue medicamentos a una consulta, el sistema deberá guardar para cada uno su nombre, dosis, frecuencia, duración e indicaciones.
- **Actor:** `veterinario` · **Superficie:** `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Una consulta puede tener cero o más medicamentos.
  - La receta forma parte de la consulta: se edita y se versiona con ella.

### EXP-005 · Cerrar la consulta
- **EARS:** Cuando el `veterinario` cierre una consulta, el sistema deberá marcarla como cerrada, guardar la fecha y hora de cierre y, si estaba ligada a una cita, ejecutar AGE-011.
- **Actor:** `veterinario` · **Superficie:** `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Una consulta con campos obligatorios vacíos no se cierra.
  - Cerrar una consulta no genera un cobro; el cobro lo hace la recepción (FAC-002).

### EXP-006 · Corregir una consulta con historial de versiones
- **EARS:** Cuando el `veterinario` edite una consulta cerrada, el sistema deberá guardar la edición como una versión nueva y conservar las anteriores con autor y fecha.
- **Actor:** `veterinario` · **Superficie:** `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Se puede ver la lista de versiones y qué cambió en cada una.
  - Ninguna versión se borra ni se sobrescribe.
  - Cualquier `veterinario` de la clínica con acceso al expediente puede corregir; la versión guarda quién fue.

### EXP-007 · Ver el historial clínico de una mascota
- **EARS:** Cuando el `veterinario` o el `clinica_admin` abra el historial de una mascota, el sistema deberá listar sus consultas en orden cronológico inverso, con fecha, veterinario, motivo y diagnóstico.
- **Actor:** `veterinario`, `clinica_admin` · **Superficie:** `APP`, `WEB-CLI` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - El historial incluye las consultas con y sin cita.
  - Al abrir una consulta se ve su última versión y se puede ir a las anteriores.
  - Cada lectura queda en la auditoría (RNF-AUD-002).

### EXP-008 · Restringir el acceso al expediente
- **EARS:** Si un usuario sin rol `veterinario` ni `clinica_admin` pide datos clínicos de una mascota, entonces el sistema deberá rechazar la petición.
- **Actor:** Sistema · **Superficie:** Todas · **Prioridad:** Debe
- **Criterios de aceptación:**
  - La `recepcionista` recibe un rechazo en cada endpoint clínico, aunque conozca el identificador.
  - El `clinica_admin` puede leer, nunca crear ni editar consultas.
  - Para un usuario con varios roles rige la unión de permisos (IDA-009).

### EXP-009 · Descargar la receta en PDF
- **EARS:** Cuando un `veterinario` o un `cliente` elijan descargar la receta de una consulta cerrada, el sistema deberá generar un PDF con la clínica, la mascota, la fecha, el veterinario y los medicamentos.
- **Actor:** `veterinario`, `cliente` · **Superficie:** `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - El `cliente` descarga solo la receta de consultas de sus mascotas, no el resto de la consulta.
  - El PDF usa la última versión de la receta.
  - No es una receta con validez legal; el PDF lo indica con una leyenda.
  - Incluye el nombre, la dirección, el teléfono y el logo de la clínica, si lo tiene (CON-010).
