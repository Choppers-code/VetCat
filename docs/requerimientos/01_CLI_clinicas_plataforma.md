# CLI · Clínicas y administración de plataforma

Superficie principal: `WEB-PLA`. Actor principal: `super_admin`.

### CLI-001 · Dar de alta una clínica
- **EARS:** Cuando el `super_admin` registre una clínica con nombre, contacto, zona horaria y plan, el sistema deberá crearla con un esquema de datos propio y aislado, y dejarla en estado `ACTIVA`.
- **Actor:** `super_admin` · **Superficie:** `WEB-PLA` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Tras el alta existe el esquema de la clínica con sus migraciones aplicadas.
  - Si el aprovisionamiento falla, no queda una clínica a medias: ni registro ni esquema.
  - Dos clínicas con el mismo nombre se aceptan; el identificador único es interno.
  - El plan elegido define desde el alta los límites y las funciones de la clínica (PLA-002).

### CLI-002 · Crear al administrador de la clínica
- **EARS:** Cuando el `super_admin` indique el nombre y el correo del administrador de una clínica, el sistema deberá crear su usuario con el rol `clinica_admin` en esa clínica y enviarle un correo para que establezca su contraseña.
- **Actor:** `super_admin` · **Superficie:** `WEB-PLA` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - El administrador recibe el correo y puede entrar tras definir su contraseña.
  - Si el correo ya existe como usuario, se agrega el rol a ese usuario en lugar de duplicarlo.
  - Una clínica puede tener más de un `clinica_admin`.

### CLI-003 · Editar los datos administrativos de una clínica
- **EARS:** Cuando el `super_admin` modifique el nombre, el contacto o la zona horaria de una clínica, el sistema deberá guardar los cambios y registrarlos en la auditoría de la plataforma.
- **Actor:** `super_admin` · **Superficie:** `WEB-PLA` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Los datos clínicos de la clínica nunca aparecen en esta pantalla.

### CLI-004 · Suspender una clínica
- **EARS:** Cuando el `super_admin` suspenda una clínica, o venza su plan (PLA-007), el sistema deberá impedir el acceso de todos sus usuarios a esa clínica sin borrar sus datos ni afectar a las demás clínicas.
- **Actor:** `super_admin` · **Superficie:** `WEB-PLA` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Un usuario de una clínica suspendida que intente seleccionarla recibe un mensaje de clínica suspendida.
  - Un usuario con otra clínica activa sigue entrando a esa.
  - Los datos permanecen intactos.

### CLI-005 · Reactivar una clínica
- **EARS:** Cuando el `super_admin` reactive una clínica suspendida, el sistema deberá restablecer el acceso de sus usuarios con todos sus datos.
- **Actor:** `super_admin` · **Superficie:** `WEB-PLA` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Tras reactivar, un usuario ve sus citas y expedientes tal como estaban.

### CLI-006 · Dar de baja una clínica
- **EARS:** Cuando el `super_admin` confirme la baja de una clínica, el sistema deberá marcarla como `BAJA`, impedir cualquier acceso y conservar sus datos hasta que una operación de plataforma los elimine.
- **Actor:** `super_admin` · **Superficie:** `WEB-PLA` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - La baja pide confirmación escribiendo el nombre de la clínica.
  - Una clínica en `BAJA` no se puede reactivar desde la pantalla; solo suspender es reversible.
  - Los datos de una clínica dada de baja se conservan; el sistema no ofrece eliminarlos.

### CLI-007 · Listar y consultar clínicas
- **EARS:** Cuando el `super_admin` consulte el listado de clínicas, el sistema deberá mostrar para cada una su nombre, estado, plan, vigencia del plan, fecha de alta, número de usuarios activos y número de mascotas.
- **Actor:** `super_admin` · **Superficie:** `WEB-PLA` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Se filtra por estado y por plan, y se busca por nombre.
  - Los números son conteos; nunca nombres de pacientes, propietarios ni datos clínicos.

### CLI-008 · Ver el estado operativo de una clínica
- **EARS:** Cuando el `super_admin` abra el detalle de una clínica, el sistema deberá mostrar su estado, su plan, el uso frente a los límites del plan y la fecha de la última actividad.
- **Actor:** `super_admin` · **Superficie:** `WEB-PLA` · **Prioridad:** Debería
- **Criterios de aceptación:**
  - El detalle no incluye ninguna pantalla que lleve a expedientes, citas, propietarios ni tickets.
