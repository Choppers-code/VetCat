# Especificación de funcionalidad: Propietarios y mascotas

**Rama de la funcionalidad**: `feature/5-propietarios-mascotas`

**Creada**: 2026-11-20

**Estado**: Borrador

**Entrada**: Issue #5 (Fase 4 — Propietarios y mascotas) y issue #115: "Registro y consulta de propietarios y mascotas".

**Requerimientos que cubre**: PRO-001, PRO-002, PRO-003, PRO-004, PRO-005, PRO-006, PRO-007, PRO-008, PRO-009 (`docs/requerimientos/05_PRO_propietarios_mascotas.md`).

## Escenarios de usuario y pruebas *(obligatorio)*

Regla general: un propietario tiene muchas mascotas; una mascota pertenece a un solo propietario y no se transfiere. Nada se borra: se da de baja lógica. Todo ocurre dentro de la clínica activa del token; ninguna historia admite un identificador de clínica en la petición.

### Historia de usuario 1 - Registrar y consultar propietarios (Prioridad: P1)

Como `recepcionista`, quiero registrar un propietario con su nombre y teléfono para tener su ficha lista antes de la primera cita.

**Por qué esta prioridad**: sin propietarios no hay mascotas, citas ni expediente; es la base de las fases 5 a 9.

**Prueba independiente**: se prueba completa registrando, editando y consultando un propietario con dos clínicas, y entrega una ficha utilizable por sí sola.

**Escenarios de aceptación**:

1. **Dado** una clínica activa, **Cuando** la `recepcionista` registra un propietario con nombre y teléfono, **Entonces** queda guardado en esa clínica y su ficha lo muestra con cero mascotas.
2. **Dado** un propietario con el mismo teléfono, **Cuando** se registra otro con ese teléfono sin confirmar, **Entonces** el sistema avisa con un conflicto y no crea nada; **Cuando** se repite confirmando, **Entonces** se crea el segundo propietario.
3. **Dado** una `recepcionista`, **Cuando** abre la ficha de un propietario, **Entonces** ve sus datos y sus mascotas activas, sin datos clínicos.
4. **Dado** un propietario de la clínica A, **Cuando** alguien de la clínica B pide su identificador, **Entonces** recibe "no encontrado".

---

### Historia de usuario 2 - Registrar y consultar mascotas (Prioridad: P1)

Como `veterinario`, quiero registrar una mascota ligada a su propietario para atenderla con sus datos generales a la vista.

**Por qué esta prioridad**: la mascota es el paciente; la agenda, el expediente y las vacunas cuelgan de ella.

**Prueba independiente**: se prueba registrando mascotas de un propietario y consultando su ficha; entrega el registro de pacientes.

**Escenarios de aceptación**:

1. **Dado** un propietario activo, **Cuando** el personal registra una mascota con nombre, especie y propietario, **Entonces** queda ligada a él y aparece entre sus mascotas activas.
2. **Dado** una mascota existente, **Cuando** alguien intenta cambiar su propietario, **Entonces** el sistema rechaza la operación (PRO-008) y la mascota conserva el suyo.
3. **Dado** una fecha de nacimiento futura, **Cuando** se registra o edita la mascota, **Entonces** el sistema la rechaza.
4. **Dado** una `recepcionista`, **Cuando** abre la ficha de una mascota, **Entonces** ve los datos generales y ningún dato clínico ni de vacunas.

---

### Historia de usuario 3 - Buscar propietarios y mascotas (Prioridad: P2)

Como `recepcionista`, quiero escribir parte de un nombre, teléfono o correo para encontrar a la persona o a la mascota sin recorrer listas.

**Por qué esta prioridad**: no bloquea el registro, pero es el acceso diario de recepción a todo lo demás.

**Prueba independiente**: se prueba con propietarios y mascotas de dos clínicas y términos con acentos y mayúsculas.

**Escenarios de aceptación**:

1. **Dado** un propietario llamado "María Pérez", **Cuando** se busca "maria", **Entonces** aparece, porque la búsqueda ignora mayúsculas y acentos.
2. **Dado** un texto de menos de 3 caracteres, **Cuando** se busca, **Entonces** el sistema rechaza la consulta.
3. **Dado** una mascota que coincide, **Cuando** aparece en el resultado, **Entonces** se muestra también su propietario.
4. **Dado** propietarios o mascotas inactivos, **Cuando** busca una `recepcionista`, **Entonces** no aparecen; **Cuando** busca el `clinica_admin` con "incluir inactivos", **Entonces** sí aparecen.

---

### Historia de usuario 4 - Dar de baja y reactivar (Prioridad: P2)

Como `clinica_admin`, quiero dar de baja a un propietario o a una mascota sin perder su historial para dejar de ofrecerlos en citas nuevas.

**Por qué esta prioridad**: mantiene limpios los listados sin borrar nada, pero el sistema funciona sin ella el primer día.

**Prueba independiente**: se prueba dando de baja y reactivando un propietario con mascotas.

**Escenarios de aceptación**:

1. **Dado** un propietario con dos mascotas activas, **Cuando** el `clinica_admin` lo da de baja, **Entonces** el propietario y sus mascotas quedan inactivos y se conservan sus datos.
2. **Dado** una mascota dada de baja, **Cuando** el `clinica_admin` la reactiva, **Entonces** vuelve a las búsquedas y a las citas nuevas.
3. **Dado** una `recepcionista` o un `veterinario`, **Cuando** intentan dar de baja, **Entonces** el sistema responde 403.
4. **Dado** que existe una verificación de citas futuras, **Cuando** el propietario o la mascota tienen citas futuras activas, **Entonces** el sistema lista las citas y no da de baja hasta cancelarlas.

---

### Historia de usuario 5 - Dirección del propietario (Prioridad: P3)

Como `recepcionista`, quiero registrar la dirección de un propietario cuando mi clínica hace visitas a domicilio para programar esas citas.

**Por qué esta prioridad**: solo aplica a clínicas con la función `visitas_domicilio` del plan; el resto del módulo no depende de ella.

**Prueba independiente**: se prueba con una clínica con la función y otra sin ella.

**Escenarios de aceptación**:

1. **Dado** una clínica cuyo plan incluye `visitas_domicilio`, **Cuando** se guarda una dirección en texto libre, **Entonces** se conserva en la ficha.
2. **Dado** una clínica sin la función, **Cuando** la petición trae una dirección, **Entonces** el sistema la rechaza y la pantalla no muestra el campo.

---

### Casos límite

- Un usuario de la clínica A consulta, edita o da de baja un identificador de la clínica B: "no encontrado", nunca un 403 que confirme su existencia.
- Un `cliente` llama a estos endpoints: 403. Su consulta de lo suyo pertenece al portal (módulo POR) y se entrega aparte.
- Dos solicitudes simultáneas registran el mismo teléfono: ambas pueden crear su propietario solo si confirman; sin confirmación no se crea ninguno por una carrera entre la comprobación y la inserción.
- Se da de baja un propietario mientras otro usuario registra una mascota para él: la mascota nueva no puede quedar activa bajo un propietario inactivo.
- Teléfono con espacios, guiones o prefijo: se compara normalizado.
- Texto de búsqueda con comodines (`%`, `_`): se trata como texto literal.
- Peso negativo o cero: se rechaza.
- Una mascota dada de baja por haberse registrado con el propietario equivocado: se registra de nuevo; el expediente anterior queda como histórico de la mascota inactiva.

## Requisitos *(obligatorio)*

### Requisitos funcionales

- **FR-001**: El sistema DEBE permitir a `clinica_admin`, `recepcionista` y `veterinario` registrar un propietario con nombre y teléfono obligatorios y correo opcional, en la clínica activa (PRO-001).
- **FR-002**: El sistema DEBE avisar con un conflicto cuando ya exista un propietario con el mismo teléfono (normalizado) y crear el nuevo solo si la petición confirma explícitamente (PRO-001).
- **FR-003**: El sistema DEBE permitir editar un propietario y mostrar su ficha con sus mascotas activas, sin datos clínicos para la `recepcionista` (PRO-002).
- **FR-004**: El sistema DEBE aceptar y mostrar la dirección del propietario, en texto libre, solo si el plan de la clínica incluye `visitas_domicilio`; sin la función la rechaza (PRO-003).
- **FR-005**: El sistema DEBE permitir al `clinica_admin` dar de baja y reactivar un propietario, conservar su historial, dejarlo fuera de búsquedas y de citas nuevas, y pasar sus mascotas a inactivas en la misma operación (PRO-004).
- **FR-006**: El sistema DEBE permitir registrar una mascota con nombre, especie y propietario obligatorios, y raza, sexo, fecha de nacimiento, peso y notas opcionales; la especie es un valor de una lista cerrada que incluye `OTRA` (PRO-005).
- **FR-007**: El sistema DEBE rechazar una fecha de nacimiento futura y un peso que no sea mayor que cero (PRO-005).
- **FR-008**: El sistema DEBE permitir editar una mascota y mostrar su ficha con datos generales, sin historial clínico ni vacunas para la `recepcionista` (PRO-006).
- **FR-009**: El sistema DEBE permitir al `clinica_admin` dar de baja y reactivar una mascota, conservar su expediente, y excluirla de citas nuevas, del límite del plan y de las alertas de vacunas (PRO-007).
- **FR-010**: El sistema DEBE rechazar cualquier cambio de propietario de una mascota existente, y la base de datos DEBE impedirlo aunque la aplicación falle (PRO-008).
- **FR-011**: El sistema DEBE buscar propietarios activos por nombre, teléfono o correo, y mascotas activas por nombre, ignorando mayúsculas y acentos, con coincidencia parcial de al menos 3 caracteres, mostrando el propietario de cada mascota y solo dentro de la clínica activa (PRO-009).
- **FR-012**: El sistema DEBE ofrecer al `clinica_admin` la opción de incluir inactivos en la búsqueda y negarla a los demás roles (PRO-009).
- **FR-013**: Antes de dar de baja un propietario o una mascota, el sistema DEBE consultar un punto de extensión de citas futuras y, si devuelve citas, listarlas y no continuar. Mientras no exista la agenda, el punto de extensión no devuelve ninguna (PRO-004, PRO-007).
- **FR-014**: El sistema DEBE tomar la clínica solo del token, rechazar con 401 sin token y con 403 un rol que no tiene permiso, y responder "no encontrado" a un identificador de otra clínica.
- **FR-015**: El sistema DEBE responder en el sobre uniforme `{success, message, data}`, en éxito y en error, sin filtrar trazas ni SQL.

### Entidades clave

- **Propietario**: persona responsable de una o varias mascotas. Nombre, teléfono, correo opcional, dirección opcional (solo con la función del plan), estado activo o inactivo. Vive en el esquema de la clínica.
- **Mascota**: paciente de la clínica. Nombre, especie (lista cerrada), raza, sexo, fecha de nacimiento, peso, notas, estado activo o inactivo y un propietario que no cambia. Vive en el esquema de la clínica.

## Criterios de éxito *(obligatorio)*

### Resultados medibles

- **SC-001**: Con dos clínicas, ninguna operación de propietarios o mascotas alcanza datos de la otra: 0 lecturas y 0 escrituras cruzadas en la prueba de aislamiento.
- **SC-002**: Los cinco roles responden como dice `Matriz_roles.md`: 401 sin token y 403 con el rol equivocado en todos los endpoints del módulo.
- **SC-003**: Una `recepcionista` registra un propietario con una mascota en menos de 2 minutos.
- **SC-004**: La búsqueda de un nombre con acentos y mayúsculas devuelve el resultado correcto al primer intento, sobre una clínica con 5 000 propietarios, en menos de 1 segundo.
- **SC-005**: Un intento de cambiar el propietario de una mascota falla el 100 % de las veces, también si se hace directamente en la base de datos.

## Supuestos

- Requiere el aislamiento por clínica de la fase 2 (hecho) y la autenticación, los roles y `@PreAuthorize` de la fase 3 (#4). Sin ella los endpoints no tienen JWT que validar.
- La comprobación de la función `visitas_domicilio` (PRO-003) usa la verificación de funciones del plan (PLA-004). Hasta que exista, la historia 5 queda bloqueada y las demás no dependen de ella.
- El límite de mascotas del plan (PLA-003) se aplica en un issue aparte del sprint 11; aquí solo se deja el registro de mascota listo para recibirlo.
- La especie sale de una lista cerrada con `OTRA`; la raza es texto libre. El sexo es opcional con los valores `MACHO`, `HEMBRA` y `DESCONOCIDO`. El peso se guarda en kilogramos.
- Reactivar un propietario **no** reactiva sus mascotas: se reactivan una por una. Decisión confirmada por Raúl el 2026-11-23, porque PRO-004 no lo dice.
- La búsqueda sin acentos usa la extensión `unaccent` de PostgreSQL, instalada una vez en el esquema `ext` desde una migración de catálogo (igual que `btree_gist`).
- El `cliente` no usa estos endpoints. Su consulta de lo suyo, con verificación de propiedad en cada petición, se especifica con el portal (módulo POR).
- La pantalla piloto y las pantallas Flutter de propietarios y mascotas son issues de `Nacho-Nacho3`; esta especificación cubre el backend y su contrato.
