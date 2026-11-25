# Especificación de funcionalidad: Agenda

**Rama de la funcionalidad**: `feature/203-spec-agenda`

**Creada**: 2026-11-24

**Estado**: Borrador

**Entrada**: Issue #203, plan de #65 y épica #6. Sustituye la spec que el plan de sprints asignaba al Sprint 4.

**Requerimientos que cubre**: AGE-001 a AGE-018 (`docs/requerimientos/06_AGE_agenda.md`).

## Escenarios de usuario y pruebas *(obligatorio)*

Regla general: toda cita vive en la clínica activa del token y apunta a una mascota de esa clínica. El empalme lo impide la base de datos, no un `SELECT` previo. Los horarios, los bloqueos y los cálculos usan la **zona horaria de la clínica**.

### Historia de usuario 1 - Crear una cita (Prioridad: P1)

Como `recepcionista`, quiero agendar a una mascota con su veterinario y consultorio para que la clínica tenga su día ordenado.

**Por qué esta prioridad**: sin citas no hay agenda; es la base de la operación diaria.

**Prueba independiente**: se prueba creando una cita válida y comprobando el 409 de empalme.

**Escenarios de aceptación**:

1. **Dado** un consultorio con horario ese día, **Cuando** se crea una cita dentro de la franja, **Entonces** queda `PENDIENTE` y aparece en la agenda.
2. **Dado** una cita existente en una franja, **Cuando** otra petición agenda el mismo consultorio o el mismo veterinario en esa franja, **Entonces** la segunda recibe 409.
3. **Dado** una franja fuera del horario, **Cuando** se intenta, **Entonces** 422 con el motivo.
4. **Dado** una cita de la clínica A, **Cuando** alguien de la B pide su id, **Entonces** no la encuentra.

---

### Historia de usuario 2 - Confirmar y cambiar de estado (Prioridad: P1)

Como `veterinario`, quiero confirmar mis citas y marcarlas como atendidas o no asistidas para que la agenda refleje la realidad.

**Prueba independiente**: se prueba cada transición válida y una prohibida.

**Escenarios de aceptación**:

1. **Dado** una cita `PENDIENTE`, **Cuando** el veterinario la confirma, **Entonces** pasa a `CONFIRMADA`.
2. **Dado** una transición no listada (por ejemplo `CANCELADA → ATENDIDA`), **Cuando** se intenta, **Entonces** 409.
3. **Dado** una cita con consulta asociada, **Cuando** se marca atendida, **Entonces** pasa a `ATENDIDA`; sin consulta, se rechaza.

---

### Historia de usuario 3 - Reprogramar y no asistencia (Prioridad: P2)

Como `recepcionista`, quiero mover una cita y marcar que el paciente no vino para no perder la franja.

**Escenarios de aceptación**:

1. **Dado** una cita `NO_ASISTIO`, **Cuando** se reprograma, **Entonces** nace otra `PENDIENTE` ligada a la original (`reprogramada_de`) y la anterior queda como estaba.
2. **Dado** una cita reprogramada, **Cuando** se consulta la nueva, **Entonces** muestra de qué cita viene.

---

### Historia de usuario 4 - Cancelar una cita (Prioridad: P1)

Como `clinica_admin` o `recepcionista`, quiero cancelar con motivo para liberar la franja y dejar constancia.

**Escenarios de aceptación**:

1. **Dado** una cita activa, **Cuando** se cancela con motivo, **Entonces** pasa a `CANCELADA` con `cancelada_por` y deja de bloquear la franja.
2. **Dado** una cita ya cancelada, **Cuando** se cancela otra vez, **Entonces** 409.

---

### Historia de usuario 5 - Solicitud de cita del cliente (Prioridad: P2)

Como `cliente`, quiero solicitar una cita para mi mascota para que la clínica la confirme.

**Escenarios de aceptación**:

1. **Dado** una mascota propia, **Cuando** el cliente solicita una cita, **Entonces** nace `PENDIENTE`.
2. **Dado** 3 solicitudes pendientes, **Cuando** pide la cuarta, **Entonces** se rechaza nombrando el límite.
3. **Dado** una mascota de otro, **Cuando** lo intenta, **Entonces** 404.
4. **Dado** una clínica sin `portal_cliente`, **Cuando** solicita, **Entonces** se rechaza en el servidor.

---

### Historia de usuario 6 - Agenda por día y semana (Prioridad: P1)

Como `veterinario`, quiero ver mi día y mi semana para organizar la consulta.

**Escenarios de aceptación**:

1. **Dado** un rango de un día, **Cuando** se consulta, **Entonces** devuelve las citas de la clínica con su estado.
2. **Dado** un `veterinario`, **Cuando** consulta, **Entonces** solo ve las suyas; la recepción ve todos los consultorios.
3. **Dado** un cambio de hora del dispositivo, **Cuando** se pinta, **Entonces** las horas son las de la clínica.

---

### Historia de usuario 7 - Bloqueos de agenda (Prioridad: P1)

Como `veterinario`, quiero bloquear una franja para que no se agenden citas en ella.

**Escenarios de aceptación**:

1. **Dado** un bloqueo de 10 a 12, **Cuando** se intenta una cita a las 11, **Entonces** 422 con el motivo del bloqueo.
2. **Dado** un bloqueo nuevo sobre citas activas, **Cuando** se crea, **Entonces** esas citas se cancelan con el motivo (historia aparte, #194).
3. **Dado** un veterinario, **Cuando** bloquea, **Entonces** no puede bloquear la agenda de otro.

---

### Casos límite

- Cita que termina exactamente cuando empieza otra: no hay empalme (`[inicio, fin)`).
- Cita cancelada dentro de una franja bloqueada después.
- Cambio de horario del consultorio con citas ya agendadas: no se tocan.
- Cita de un veterinario que deja de pertenecer a la clínica.
- Cruce de medianoche en la zona de la clínica.

## Requisitos *(obligatorio)*

### Requisitos funcionales

- **FR-001**: crear cita con mascota, veterinario, consultorio y franja (AGE-001).
- **FR-002**: validar horario, bloqueo y pertenencia a la clínica (AGE-002).
- **FR-003**: el empalme lo rechaza la base con `EXCLUDE`, y la API responde 409 (AGE-003).
- **FR-004**: confirmar, atender, no asistir y cancelar con transiciones cerradas (AGE-004, AGE-005, AGE-007, AGE-010, AGE-011).
- **FR-005**: reprogramar crea una cita nueva ligada a la anterior (AGE-006).
- **FR-006**: el cliente solicita cita, máximo 3 pendientes, y el veterinario confirma (AGE-017, AGE-018).
- **FR-007**: el cliente solicita cancelación con 5 horas y el veterinario la resuelve (AGE-008, AGE-009).
- **FR-008**: agenda por día y semana en la zona de la clínica (AGE-012).
- **FR-009**: disponibilidad según horario y bloqueos (AGE-013).
- **FR-010**: el veterinario solo ve sus citas (AGE-014).
- **FR-011**: citas a domicilio solo con `visitas_domicilio` del plan (AGE-016).

### Entidades clave

- `consultorio`, `horario_consultorio` (con excepción por fecha), `bloqueo_agenda`, `cita`.
- `cita`: `tipo` (CONSULTORIO/DOMICILIO), `consultorio_id` obligatorio solo en consultorio, `direccion` en domicilio, estados con `varchar` y CHECK, `reprogramada_de`.

## Criterios de éxito *(obligatorio)*

- Dos peticiones simultáneas de la misma franja: exactamente una gana.
- Una cita cancelada no bloquea la franja.
- Ninguna consulta devuelve citas de otra clínica.
- Las horas se muestran siempre en la zona de la clínica.

## Supuestos

- Los recordatorios y el comprobante en PDF son de otras fases (NOT, AGE-015).
- El calendario de la app es una pantalla; esta spec cubre la API y su contrato.

## Fuera de alcance

- Notificaciones y recordatorios.
- Comprobante en PDF.
- Multi-sede y disponibilidad entre clínicas.
