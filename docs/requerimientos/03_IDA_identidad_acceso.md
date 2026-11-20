# IDA · Identidad y acceso

Todas las superficies. Autenticación con Keycloak (OIDC, Authorization Code + PKCE); el token de clínica lo emite el backend (ADR-0003).

### IDA-001 · Iniciar sesión
- **EARS:** Cuando un usuario introduzca sus credenciales válidas en el flujo de inicio de sesión, el sistema deberá autenticarlo y llevarlo a la selección de clínica o, si tiene una sola, a su pantalla de inicio.
- **Actor:** Todos · **Superficie:** Todas · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Las contraseñas solo se capturan en la página de Keycloak, nunca en pantallas propias.
  - Con credenciales incorrectas el mensaje no revela si el correo existe.
  - Un `super_admin` entra directo a `WEB-PLA`, sin selección de clínica.

### IDA-002 · Seleccionar la clínica activa
- **EARS:** Cuando un usuario con acceso a más de una clínica termine de iniciar sesión, el sistema deberá mostrarle sus clínicas como tarjetas con nombre y rol en cada una, y abrir la clínica que elija.
- **Actor:** `clinica_admin`, `veterinario`, `recepcionista`, `cliente` · **Superficie:** `APP`, `WEB-CLI` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Si el usuario tiene una sola clínica, se entra directo.
  - Las clínicas suspendidas se muestran deshabilitadas con la etiqueta "Suspendida".
  - Si el usuario tiene varios roles en la clínica elegida, entra con todos ellos (IDA-009).
  - La app abre la vista que corresponde al rol del usuario en la clínica elegida: la del veterinario o la del cliente.
  - La app recuerda la última clínica elegida como sugerencia, sin saltarse la selección cuando hay más de una.

### IDA-003 · Cambiar de clínica
- **EARS:** Cuando un usuario con más de una clínica elija "Cambiar de clínica", el sistema deberá cerrar el contexto de la clínica actual y mostrar la selección de clínica sin pedir de nuevo sus credenciales.
- **Actor:** Todos con más de una clínica · **Superficie:** `APP`, `WEB-CLI` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Tras cambiar, ninguna pantalla muestra datos de la clínica anterior.
  - La opción solo aparece si el usuario tiene más de una clínica.

### IDA-004 · Cerrar sesión
- **EARS:** Cuando el usuario elija "Cerrar sesión", el sistema deberá terminar su sesión en la aplicación y en Keycloak, y borrar los tokens del dispositivo.
- **Actor:** Todos · **Superficie:** Todas · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Tras cerrar sesión, el botón "atrás" no muestra pantallas con datos.
  - Un token ya emitido deja de aceptarse al expirar; no se promete revocación inmediata (ver RNF-SEG-005).

### IDA-005 · Recuperar la contraseña
- **EARS:** Cuando un usuario pida recuperar su contraseña desde el inicio de sesión, el sistema deberá enviar a su correo un enlace de un solo uso y de vigencia limitada para que establezca una contraseña nueva.
- **Actor:** Todos · **Superficie:** Todas · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Se usa el flujo de recuperación propio de Keycloak; VetCat no implementa uno paralelo.
  - La respuesta es la misma exista o no el correo.
  - El enlace caduca (por defecto 15 minutos) y funciona una sola vez.
  - En desarrollo los correos llegan a un buzón local de pruebas; en producción hace falta un servidor SMTP configurado (lista de despliegue).
  - Quien no recibe el correo puede pedir otro desde la misma pantalla.

### IDA-006 · Registro del cliente por su cuenta
- **EARS:** Cuando una persona se registre desde la app del cliente con nombre, correo y contraseña, el sistema deberá crear su cuenta con el rol `cliente` y pedirle que confirme su correo.
- **Actor:** Persona sin cuenta · **Superficie:** `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Una cuenta sin correo confirmado no entra.
  - El registro por cuenta propia solo crea el rol `cliente`; nunca otro rol.
  - Una cuenta recién creada no ve ninguna mascota hasta vincularse a un propietario (IDA-008).

### IDA-007 · Invitar a un cliente
- **EARS:** Cuando el `clinica_admin` o la `recepcionista` invite a un propietario a la app, el sistema deberá enviarle por correo un enlace de invitación vinculado a su ficha de propietario.
- **Actor:** `clinica_admin`, `recepcionista` · **Superficie:** `WEB-CLI` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - La invitación requiere que el propietario tenga correo registrado.
  - Al aceptarla, el cliente queda vinculado a esa ficha sin pasos extra; si no tenía cuenta, se le ayuda a crearla.
  - La invitación caduca a los 2 días y se puede reenviar.
  - Solo se ofrece donde el plan incluye `portal_cliente`.

### IDA-008 · Vincular una cuenta de cliente con un propietario
- **EARS:** Cuando un `cliente` introduzca el código de vinculación que le entregó una clínica, el sistema deberá vincular su cuenta con la ficha de propietario a la que corresponde el código.
- **Actor:** `cliente` · **Superficie:** `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - La clínica genera el código desde la ficha del propietario; el código es de un solo uso y caduca a los 2 días.
  - Un código vencido, usado o inexistente se rechaza con el mismo mensaje.
  - Una cuenta puede vincularse a propietarios de varias clínicas, y un propietario a una sola cuenta.
  - Si el propietario ya está vinculado a otra cuenta, el sistema rechaza el código.
  - Solo se ofrece donde el plan incluye `portal_cliente`.

### IDA-009 · Varios roles por usuario
- **EARS:** Cuando un usuario tenga más de un rol en la clínica activa, el sistema deberá concederle la unión de los permisos de sus roles.
- **Actor:** Sistema · **Superficie:** Todas · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Un veterinario que también es `clinica_admin` ve ambas funciones.
  - Un permiso se concede solo si alguno de los roles activos lo tiene en la matriz.
  - La combinación `cliente` con un rol de personal en la misma clínica se permite; las funciones de cada rol no se mezclan, el usuario cambia de vista (IDA-010).

### IDA-010 · Interfaz ajustada al rol
- **EARS:** Mientras haya una sesión activa en una clínica, el sistema deberá mostrar solo las opciones de menú, botones y acciones que los roles del usuario y el plan de la clínica permiten.
- **Actor:** Todos · **Superficie:** Todas · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Una `recepcionista` no ve opciones clínicas ni de configuración; un `veterinario` no ve la caja.
  - Un usuario con varios roles ve el menú combinado, agrupado por rol.
  - Si el usuario es veterinario y cliente en la misma clínica, la app ofrece "Cambiar de vista" sin cerrar sesión.
  - Ocultar es comodidad, no seguridad: el backend rechaza igual cualquier acción no permitida (RNF-SEG-002).
  - Una opción de una función no incluida en el plan no aparece (PLA-004).
  - Si una acción ya mostrada es rechazada por permisos, la app muestra "No tienes permiso" y no un error técnico.
