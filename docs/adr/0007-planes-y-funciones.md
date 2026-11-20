# ADR-0007 · planes y funciones por plan

| | |
|---|---|
| **Estado** | Aceptada |
| **Fecha** | 2026-11-20 |
| **Área** | 1 · Arquitectura |
| **Issue** | #76 |
| **Decide** | Un único modelo de datos; cada clínica tiene un plan con vigencia que activa funciones y fija límites, y el servidor lo verifica en cada petición |

## Contexto

VetCat es un SaaS multi-clínica con funciones opcionales: portal del cliente, recordatorios, ventas, turno de caja, inventario, visitas a domicilio y reportes (`docs/requerimientos/02_PLA_planes.md`). No todas las clínicas pagan por todo, y el equipo necesita poder activar o apagar funciones sin separar los datos.

## Opciones consideradas

| Opción | Ventaja | Costo |
|---|---|---|
| **Un solo modelo de datos y plan con funciones y límites** | Un esquema, un código, una migración; el plan es dato, no estructura | Cada petición debe verificar el plan en el servidor |
| Un esquema o una base distinta por plan | Aislamiento fuerte por plan | Multiplica migraciones y pruebas por cada plan; cambiar de plan obliga a mover datos |
| Pagos en línea de los planes | Cobro automático | Amplía el alcance y trae datos bancarios y riesgo de seguridad |

## Decisión

1. **Un solo modelo de datos.** En `catalogo` viven `plan`, `plan_funcion`, `plan_limite` y `pago_plan`; la clínica tiene un plan y una `vigente_hasta`.
2. Las funciones son `portal_cliente`, `recordatorios`, `ventas`, `turno_caja`, `inventario`, `visitas_domicilio` y `reportes`. **`turno_caja` e `inventario` requieren `ventas`.**
3. `plan_limite` fija el máximo de usuarios, de veterinarios y de mascotas. Un límite vacío significa sin límite.
4. **La verificación es del servidor, en cada petición.** Las funciones y los límites no viajan en el token ni los informa la app. Una función que el plan no incluye responde con error aunque el rol la permita.
5. Estados de la clínica: `ACTIVA`, `SUSPENDIDA` y `BAJA`. La suspensión por vencimiento es automática tras **3 días de gracia** sin pago registrado y no toca los datos. La baja conserva los datos y no se puede revertir desde la consola.
6. **El cobro es manual:** `super_admin` registra el pago y la vigencia. No hay pagos en línea ni datos bancarios.

## Consecuencias

### Se gana

- Cambiar de plan es cambiar filas, no mover datos.
- Una sola base de código y un solo conjunto de migraciones para todas las clínicas.

### Se paga

- Cada módulo opcional debe consultar el plan, y esa consulta es el punto de mayor riesgo de olvido.
- La suspensión automática es un trabajo programado que debe recibir el tenant de forma explícita (`Decisiones_tecnicas.md` §2) y probarse.

### Queda prohibido a partir de ahora

- Un esquema o una base por plan.
- Leer las funciones del plan desde el token o desde un valor enviado por la app.
- Guardar datos bancarios o procesar pagos de los planes.

## Cómo se hace cumplir

| Mecanismo | Qué garantiza | Estado |
|---|---|---|
| Prueba por función: rol permitido, plan sin la función | Que responde con error aunque el rol lo permita | Fase 3 |
| Prueba de límites | Que no se registra más allá de `plan_limite` | Fase 3 |
| Prueba de suspensión automática | Que vence a los 3 días de gracia y no toca los datos | Fase 3 |

## Referencias

- `docs/Decisiones_tecnicas.md` §12.
- `docs/requerimientos/02_PLA_planes.md`.
- `docs/Matriz_roles.md`, regla 6.
