# Tareas: Planes y funciones habilitadas

**Entrada**: [plan.md](plan.md) y [spec.md](spec.md). **Pruebas**: JUnit 6 + Testcontainers contra PostgreSQL real.

Regla: primero lo que protege el aislamiento y la autorización; después la lógica. Cada tarea cabe en el PR de su issue (#156, #158, #157).

## Fase 0 — Base (hecha)

- [x] **T001** Migración `catalogo/V5`: `plan`, `plan_funcion`, `plan_limite`, `pago_plan` y `clinica.plan_id`, con CHECK de funciones, recursos y periodo. (#163)
- [x] **T002** Pruebas de esquema: funciones y límites válidos, nombre repetido, función desconocida, límite no positivo, plan en uso, periodo invertido.

## Fase 1 — Verificación de funciones (#156)

- [ ] **T003** `[P]` Prueba: una operación de una función no incluida se rechaza aunque el rol la permita, y falla si se quita la guarda.
- [ ] **T004** `[P]` Prueba: apagar `ventas` apaga `turno_caja` e `inventario`.
- [ ] **T005** `FuncionesDePlan`: enum cerrado y mapa de requisitos; prueba de la lista completa.
- [ ] **T006** `VerificadorDePlan`: lee funciones de la clínica activa por petición, sin caché que sobreviva al cambio.
- [ ] **T007** Guarda o interceptor que devuelve el error de función no disponible con el sobre.
- [ ] **T008** `VerificadorDeLimites`: conteo real de `usuarios`, `veterinarios` y `mascotas` por la unidad `tenant`.
- [ ] **T009** Prueba de límite: alta N+1 rechazada con el límite y el plan en el mensaje.
- [ ] **T010** Prueba de que las bajas lógicas no cuentan.
- [ ] **T011** Endpoints de catálogo y asignación (`super_admin`) con `@PreAuthorize` y auditoría.

## Fase 2 — Vigencia y pagos (#158)

- [ ] **T012** `[P]` Prueba: un pago extiende la vigencia; antes del vencimiento se suma.
- [ ] **T013** `[P]` Prueba: la suspensión corre a los 3 días de gracia y es idempotente (reloj controlado).
- [ ] **T014** `VigenciaDePlan`: `max(periodo_hasta)` por clínica, con índice.
- [ ] **T015** `PagoPlanService`: registra pago, recalcula vigencia y reactiva si estaba suspendida.
- [ ] **T016** Proceso programado de suspensión, con el tenant explícito y auditoría del cambio de estado.
- [ ] **T017** `GET /api/v1/clinica/plan`: plan, funciones, vigencia y uso frente a cada límite.
- [ ] **T018** Aviso de plan vencido en la gracia (dato para la consola, sin pantalla).

## Fase 3 — Pruebas de conjunto (#157)

- [ ] **T019** `[P]` Prueba de función no incluida sobre un endpoint real de negocio (no de prueba).
- [ ] **T020** `[P]` Prueba de rol ajeno y de token sin clínica en el módulo de plan.
- [ ] **T021** Dos clínicas con planes distintos: una función activa en A y no en B, sin cruce.
- [ ] **T022** Revisar que las pruebas fallan al quitar la guarda o el `@PreAuthorize`.

## Dependencias

- T004 y T005 antes que T006.
- T008 y T009 antes de conectar la verificación al módulo que crea mascotas.
- T012–T016 antes de T017 y de la prueba de suspensión de #157.
