# Sprints 10 a 19 · Fases 4 a 7

Parte de [`Plan_de_sprints.md`](../Plan_de_sprints.md). Entrega el núcleo clínico de la versión 1.0: propietarios y mascotas, agenda, expediente y vacunas. Termina en el hito **H3**.

**Cómo leer las tablas.** *Req.* es el módulo de `docs/requerimientos/`. *Depende de* son filas o issues que deben estar fusionados antes. Un `#N` ya existe como issue; el resto se crea al empezar el sprint. Donde dice "(aprueba X)", escribe una persona y aprueba el dueño del módulo, según [`Equipo_responsabilidades.md`](../Equipo_responsabilidades.md).

## Sprint 10 · 3 – 9 ene 2027 · Fase 4

| Issue | Responsable | Req. | Depende de |
|---|---|---|---|
| Propietarios: registro, edición, consulta, búsqueda y desactivación | `Raul-Rolu` | PRO | Aislamiento, contrato de PRO |
| Mascotas: registro, edición, consulta y desactivación; un solo propietario, sin transferencia | `Raul-Rolu` | PRO | Propietarios |
| Pantalla piloto de mascotas, que sirve de patrón a las demás | `Nacho-Nacho3` | PRO | Navegación base, #5 |
| Auditoría de la plataforma: registro de las acciones de `super_admin` | `Anton-code-sp` | CLI | Catálogo |
| Pruebas de aislamiento sobre propietarios y mascotas | `Carlosss1819` | RNF | Propietarios |
| Contrato OpenAPI de vacunas | `ToNyyF1` | VAC | Convención |

## Sprint 11 · 10 – 16 ene 2027 · Fase 4 (cierre)

| Issue | Responsable | Req. | Depende de |
|---|---|---|---|
| Dirección del propietario solo si el plan incluye `visitas_domicilio` | `Raul-Rolu` | PRO, PLA | Verificación de funciones |
| Propiedad verificada en cada consulta del `cliente` | `Raul-Rolu` | PRO, POR | Mascotas |
| Límite de mascotas del plan al registrar una mascota | `Anton-code-sp` | PLA | Verificación de funciones |
| Pantallas de propietarios: lista, formulario y ficha | `Nacho-Nacho3` | PRO | Pantalla piloto |
| Pruebas de módulo y autorización de propietarios y mascotas | `Carlosss1819` | RNF | Propietarios, mascotas |
| Especificar el expediente con Spec Kit | `Raul-Rolu` | EXP | — |

## Sprint 12 · 17 – 23 ene 2027 · Fase 5

| Issue | Responsable | Req. | Depende de |
|---|---|---|---|
| Consultorios y horarios: alta, edición, baja lógica y excepciones por fecha | `ToNyyF1` | CON, AGE | Contrato de agenda |
| Bloqueos de agenda por veterinario o consultorio | `ToNyyF1` | CON, AGE | Consultorios |
| Migración de la agenda con restricción `EXCLUDE USING gist` por veterinario y por consultorio | `ToNyyF1` | AGE | Aislamiento |
| Decisión de la librería de PDF (ADR) | `Anton-code-sp` | EXP, AGE | — |
| Calendario de la agenda: vistas de día y semana | `Nacho-Nacho3` | AGE | Contrato de agenda |
| Esqueleto de la prueba de reservas concurrentes con Testcontainers | `Carlosss1819` | AGE | Migración de la agenda |

## Sprint 13 · 24 – 30 ene 2027 · Fase 5

| Issue | Responsable | Req. | Depende de |
|---|---|---|---|
| Crear cita, validar horario y bloqueos, y responder 409 ante un empalme (`23P01`) | `ToNyyF1` | AGE | Migración, horarios |
| Estados de la cita y transiciones válidas, reprogramación y `NO_ASISTIO` | `ToNyyF1` | AGE | Crear cita |
| Citas `DOMICILIO` solo si el plan incluye `visitas_domicilio` | `Anton-code-sp` | AGE, PLA | Verificación de funciones |
| Prueba de reservas concurrentes: solo una gana | `Carlosss1819` | AGE | Crear cita |
| Calendario: citas, estados y colores por veterinario | `Nacho-Nacho3` | AGE | Calendario de la agenda |

## Sprint 14 · 31 ene – 6 feb 2027 · Fase 5 (cierre)

| Issue | Responsable | Req. | Depende de |
|---|---|---|---|
| Solicitud de cita del `cliente` (máximo 3 pendientes) y confirmación por el veterinario | `ToNyyF1` | AGE | Estados de la cita |
| Solicitud de cancelación (con 5 horas de anticipación) y su aprobación o rechazo | `ToNyyF1` | AGE | Estados de la cita |
| Un bloqueo sobre citas activas las cancela con su motivo | `ToNyyF1` | AGE | Bloqueos |
| Formulario de cita y bandeja de solicitudes (aprueba `ToNyyF1`) | `Nacho-Nacho3` | AGE | Solicitudes |
| Pruebas de aislamiento y autorización de la agenda | `Carlosss1819` | AGE | Solicitudes |

## Sprint 15 · 7 – 13 feb 2027 · Fase 6

| Issue | Responsable | Req. | Depende de |
|---|---|---|---|
| Consulta con diagnóstico, tratamiento y notas, ligada o no a una cita | `Raul-Rolu` | EXP | Especificación del expediente |
| Versiones: cada edición crea una versión nueva y conserva autor y fecha | `Raul-Rolu` | EXP | Consulta |
| Comprobante de cita en PDF (generado al vuelo, sin almacenarse) | `ToNyyF1` | AGE | Decisión de PDF |
| Auditoría de cambios y lecturas del expediente | `Anton-code-sp` | EXP, RNF | Consulta |
| Pantallas del expediente: lista de consultas y ficha (aprueba `Raul-Rolu`) | `Nacho-Nacho3` | EXP | Consulta |
| Pruebas de integridad del expediente: nada se sobrescribe ni se borra | `Carlosss1819` | EXP | Versiones |

## Sprint 16 · 14 – 20 feb 2027 · Fase 6

| Issue | Responsable | Req. | Depende de |
|---|---|---|---|
| Receta dentro de la consulta y su PDF al vuelo, con el logo de la clínica | `Raul-Rolu` | EXP | Decisión de PDF |
| Cerrar la consulta marca la cita `ATENDIDA` (aprueba `ToNyyF1`) | `Raul-Rolu` | EXP, AGE | Estados de la cita |
| Permisos del expediente: `clinica_admin` lee sin editar; `recepcionista` y `super_admin` no acceden | `Anton-code-sp` | EXP | Auditoría |
| Pantalla de consulta y receta (aprueba `Raul-Rolu`) | `Nacho-Nacho3` | EXP | Receta |
| Pruebas de autorización del expediente por rol | `Carlosss1819` | EXP | Permisos |

## Sprint 17 · 21 – 27 feb 2027 · Fase 6 (cierre)

| Issue | Responsable | Req. | Depende de |
|---|---|---|---|
| Resumen autorizado para el `cliente`: última versión de consultas cerradas | `Raul-Rolu` | EXP, POR | Versiones |
| Historia clínica en la app del veterinario | `Nacho-Nacho3` | EXP | Pantallas del expediente |
| Prueba de integridad completa del expediente y revisión de auditoría | `Carlosss1819` | EXP | Auditoría |
| Catálogo de vacunas por clínica: alta, edición y consulta | `ToNyyF1` | VAC | Contrato de vacunas |

## Sprint 18 · 28 feb – 6 mar 2027 · Fase 7

| Issue | Responsable | Req. | Depende de |
|---|---|---|---|
| Consulta del catálogo para `recepcionista` sin aplicaciones ni próximas dosis | `ToNyyF1` | VAC | Catálogo |
| Registrar la aplicación de una vacuna y calcular la próxima dosis, con ajuste manual | `ToNyyF1` | VAC | Catálogo, mascotas |
| Prueba del cálculo de la próxima dosis | `Carlosss1819` | VAC | Aplicación de vacuna |
| Cartilla de vacunación en la app (aprueba `ToNyyF1`) | `Nacho-Nacho3` | VAC | Aplicación de vacuna |

## Sprint 19 · 7 – 13 mar 2027 · Fase 7 (cierre) · H3

| Issue | Responsable | Req. | Depende de |
|---|---|---|---|
| Alertas dentro de la app: vencidas y próximas a 30 días, configurable | `ToNyyF1` | VAC | Aplicación de vacuna |
| Pantalla de alertas de vacunas (aprueba `ToNyyF1`) | `Nacho-Nacho3` | VAC | Alertas |
| Contratos OpenAPI v1.0 consolidados en `docs/api/v1/` | `Anton-code-sp` | todos | Fases 4 a 7 |
| Corte `release/1.0.0` y pruebas de aceptación del núcleo clínico | `Carlosss1819` | todos | Todo lo anterior |

**H3 (v1.0):** agenda, propietarios, mascotas, expediente y vacunas funcionando en la app.
