# Tareas: Agenda

**Entrada**: [plan.md](plan.md) y [spec.md](spec.md). **Pruebas**: JUnit 6 + Testcontainers contra PostgreSQL real.

Regla: primero la base y la concurrencia; después la lógica. Cada tarea cabe en el PR de su issue.

## Fase 0 — Base (#189)

- [ ] **T001** `tenant/V2`: `consultorio`, `horario_consultorio`, `bloqueo_agenda`, `cita`, con los dos `EXCLUDE` y el `btree_gist` de `ext`.
- [ ] **T002** `[P]` Prueba de esquema: dos filas solapadas del mismo consultorio se rechazan; una cancelada no bloquea.
- [ ] **T003** `[P]` Prueba de esquema: el segundo `EXCLUDE` rechaza el solape del mismo veterinario en consultorios distintos.
- [ ] **T004** Entidades JPA en la unidad `tenant` y validación contra el esquema.
- [ ] **T005** Paths del contrato en `openapi.yaml`.

## Fase 1 — Consultorios y horarios (#191)

- [X] **T006** Alta, edición y baja lógica de consultorio.
- [X] **T007** Horarios por día y excepción por fecha, en la zona de la clínica.
- [X] **T008** `[P]` Prueba: dos horarios solapados se rechazan; una excepción manda sobre el semanal.
- [X] **T009** `@PreAuthorize` de configuración y lectura.

## Fase 2 — Bloqueos (#193)

- [ ] **T010** Alta y baja de bloqueo por veterinario o consultorio.
- [ ] **T011** `[P]` Prueba: un veterinario no bloquea la agenda de otro.
- [ ] **T012** `[P]` Prueba: una cita dentro de un bloqueo se rechaza con 422 y el motivo.

## Fase 3 — Crear cita (#195)

- [ ] **T013** `[P]` Prueba de concurrencia: N hilos en la misma franja y consultorio; exactamente una gana.
- [ ] **T014** Validación de mascota, veterinario y consultorio de la clínica activa.
- [ ] **T015** Validación de horario y bloqueo con la zona de la clínica.
- [ ] **T016** Inserción y traducción del `23P01` a 409 con el sobre.
- [ ] **T017** Filtro del veterinario a sus propias citas.
- [ ] **T018** `@PreAuthorize` y prueba de 403.

## Fase 4 — Aparcado tras el viernes

- [ ] **T019** Estados, transiciones, reprogramación y no asistencia (#188).
- [ ] **T020** Solicitud de cita y límite de 3 pendientes (#190).
- [ ] **T021** Solicitud de cancelación y resolución (#192).
- [ ] **T022** Un bloqueo cancela citas activas (#194).
- [ ] **T023** Calendario en la app (#202, #196).
- [ ] **T024** Citas a domicilio con plan (#197).

## Dependencias

- T001 antes que todo lo demás.
- T004 y T005 antes de T014.
- T013 se puede escribir antes que T016: es la prueba que define el comportamiento.
