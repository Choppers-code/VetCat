# POR · Portal del cliente

Superficie: `APP`. Actor: `cliente`. Solo lectura, salvo la solicitud de cancelación (AGE-008).

Todo el módulo se ofrece solo donde el plan de la clínica incluye `portal_cliente` (PLA-004). El registro, la invitación y la vinculación están en IDA-006, IDA-007 e IDA-008; las citas del cliente en AGE-014 y AGE-015.

### POR-001 · Ver mis clínicas y mis mascotas
- **EARS:** Cuando un `cliente` elija una clínica, el sistema deberá mostrarle las mascotas activas que esa clínica tiene registradas a nombre de los propietarios vinculados a su cuenta.
- **Actor:** `cliente` · **Superficie:** `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Un cliente sin propietario vinculado en la clínica ve el mensaje "Pídele a tu clínica un código de vinculación".
  - Nunca ve mascotas de otro propietario aunque conozca su identificador (la propiedad se verifica en cada consulta; RNF-SEG-003).
  - Los datos mostrados son nombre, especie, raza, fecha de nacimiento y peso.

### POR-002 · Ver mis próximas citas
- **EARS:** Cuando un `cliente` abra su inicio, el sistema deberá mostrar sus citas futuras activas ordenadas por fecha.
- **Actor:** `cliente` · **Superficie:** `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Es el acceso rápido a AGE-014; no duplica reglas.

### POR-003 · Ver el carnet de vacunación de mis mascotas
- **EARS:** Cuando un `cliente` abra una de sus mascotas, el sistema deberá mostrar su carnet de vacunación con las aplicaciones y la próxima dosis de cada vacuna.
- **Actor:** `cliente` · **Superficie:** `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Muestra vacuna, fecha de aplicación y próxima dosis; no muestra el lote ni quién la aplicó.
  - Una próxima dosis vencida se destaca.

### POR-004 · Ver el resumen de las consultas de mis mascotas
- **EARS:** Cuando un `cliente` abra el historial de una de sus mascotas, el sistema deberá mostrar de cada consulta cerrada la fecha, el motivo, el diagnóstico, el tratamiento y la receta.
- **Actor:** `cliente` · **Superficie:** `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - No se muestran anamnesis, exploración, notas internas ni versiones anteriores.
  - Se ve la última versión de cada consulta.
  - Una consulta abierta o en borrador no se muestra.
  - Cada lectura queda en la auditoría (RNF-AUD-002).

### POR-005 · Navegar entre clínicas
- **EARS:** Cuando un `cliente` esté vinculado a propietarios de más de una clínica, el sistema deberá permitirle cambiar de clínica con IDA-003 y mostrar solo los datos de la clínica activa.
- **Actor:** `cliente` · **Superficie:** `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - No hay una vista que mezcle datos de varias clínicas.
