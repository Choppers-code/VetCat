# PRO · Propietarios y mascotas

Superficies: `WEB-CLI` y `APP`. Actores: `clinica_admin`, `recepcionista`, `veterinario`.

Regla general: un propietario tiene muchas mascotas; una mascota pertenece a un solo propietario y **no se transfiere**. Nada se borra: se da de baja lógica.

### PRO-001 · Registrar un propietario
- **EARS:** Cuando un `clinica_admin`, una `recepcionista` o un `veterinario` registre un propietario con nombre y teléfono, y opcionalmente correo, el sistema deberá guardarlo en la clínica activa.
- **Actor:** `clinica_admin`, `recepcionista`, `veterinario` · **Superficie:** `WEB-CLI`, `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Nombre y teléfono son obligatorios; el correo es opcional salvo para invitarlo a la app (IDA-007).
  - Si ya existe un propietario con el mismo teléfono, el sistema avisa y pide confirmar antes de crear otro (puede haber familias con un solo teléfono).

### PRO-002 · Editar y consultar un propietario
- **EARS:** Cuando el personal edite o abra la ficha de un propietario, el sistema deberá guardar los cambios o mostrar sus datos y sus mascotas activas.
- **Actor:** `clinica_admin`, `recepcionista`, `veterinario` · **Superficie:** `WEB-CLI`, `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - La ficha no muestra datos clínicos a la `recepcionista`.

### PRO-003 · Dirección del propietario
- **EARS:** Donde el plan de la clínica incluya `visitas_domicilio`, el sistema deberá permitir registrar y editar la dirección del propietario.
- **Actor:** `clinica_admin`, `recepcionista`, `veterinario` · **Superficie:** `WEB-CLI`, `APP` · **Prioridad:** Podría
- **Criterios de aceptación:**
  - Sin la función, el campo no aparece ni la API lo acepta (PLA-004).
  - La dirección se captura como texto libre en esta versión; no se geolocaliza.

### PRO-004 · Dar de baja un propietario
- **EARS:** Cuando el `clinica_admin` dé de baja a un propietario, el sistema deberá marcarlo como inactivo, conservar su historial y dejar de ofrecerlo en búsquedas y en citas nuevas.
- **Actor:** `clinica_admin` · **Superficie:** `WEB-CLI` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Si tiene citas futuras activas, el sistema avisa y pide cancelarlas antes.
  - Sus mascotas pasan a inactivas junto con él.
  - Se puede reactivar.

### PRO-005 · Registrar una mascota
- **EARS:** Cuando el personal registre una mascota con nombre, especie y propietario, y opcionalmente raza, sexo, fecha de nacimiento, peso y notas, el sistema deberá guardarla ligada a ese propietario.
- **Actor:** `clinica_admin`, `recepcionista`, `veterinario` · **Superficie:** `WEB-CLI`, `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Un propietario puede tener cualquier número de mascotas, dentro del límite del plan (PLA-003).
  - Nombre, especie y propietario son obligatorios.
  - La fecha de nacimiento no puede ser futura.

### PRO-006 · Editar y consultar una mascota
- **EARS:** Cuando el personal edite o abra la ficha de una mascota, el sistema deberá guardar los cambios o mostrar sus datos generales.
- **Actor:** `clinica_admin`, `recepcionista`, `veterinario` · **Superficie:** `WEB-CLI`, `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - El peso se actualiza también al registrar una consulta (EXP-002); la ficha muestra el último.
  - La `recepcionista` ve los datos generales, no el historial clínico ni las vacunas.

### PRO-007 · Dar de baja una mascota
- **EARS:** Cuando el `clinica_admin` dé de baja una mascota, el sistema deberá marcarla como inactiva, conservar su expediente y dejar de ofrecerla para citas nuevas.
- **Actor:** `clinica_admin` · **Superficie:** `WEB-CLI` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Las citas futuras activas se listan para cancelarlas antes de dar la baja.
  - Una mascota inactiva deja de contar para el límite del plan y de generar alertas de vacunas.
  - Se puede reactivar.

### PRO-008 · No transferir mascotas entre propietarios
- **EARS:** Si alguien intenta cambiar el propietario de una mascota existente, entonces el sistema deberá rechazar la operación.
- **Actor:** Sistema · **Superficie:** Todas · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Ninguna pantalla ofrece "cambiar propietario".
  - Si una mascota se registró con el propietario equivocado, se da de baja y se registra de nuevo; el expediente anterior se conserva como histórico de la mascota dada de baja.

### PRO-009 · Buscar propietarios y mascotas
- **EARS:** Cuando el personal escriba un texto en el buscador, el sistema deberá devolver los propietarios activos cuyo nombre, teléfono o correo coincidan, y las mascotas activas cuyo nombre coincida.
- **Actor:** `clinica_admin`, `recepcionista`, `veterinario` · **Superficie:** `WEB-CLI`, `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - La búsqueda ignora mayúsculas y acentos y acepta coincidencias parciales de al menos 3 caracteres.
  - El resultado de una mascota muestra también su propietario.
  - Solo busca dentro de la clínica activa.
  - Existe la opción de incluir inactivos, solo para el `clinica_admin`.
