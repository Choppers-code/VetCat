# ADR-0010 · aprovisionamiento de clínicas en runtime con advisory lock

| | |
|---|---|
| **Estado** | Aceptada |
| **Fecha** | 2026-11-20 |
| **Área** | 1 · Arquitectura, con el área 5 |
| **Issue** | #15 |
| **Decide** | El job one-shot migra los esquemas existentes; la API aplica el historial de tenant solo al esquema nuevo de una clínica recién creada, bajo un advisory lock de PostgreSQL |

## Contexto

`Despliegue_y_operacion.md` §5 dice que un contenedor one-shot aplica Flyway antes de levantar la API y que la API nunca migra. Pero dar de alta una clínica exige un esquema `clinica_xxx` migrado **en ese momento**: el administrador de la plataforma no puede esperar al siguiente despliegue, y la API no puede lanzar un contenedor a demanda sin depender de la máquina ni del orquestador (regla de portabilidad a la nube).

El texto actual lo resuelve con un "runner que se puede lanzar a demanda", pero no dice quién lo lanza ni qué pasa si dos altas o un despliegue coinciden. Sin esa respuesta, la fase 2 tiene una regla que el aprovisionamiento no puede cumplir.

## Opciones consideradas

| Opción | Ventaja | Costo |
|---|---|---|
| Solo el runner a demanda | Cumple "la API nunca migra" al pie de la letra | El alta es asíncrona y la API tendría que lanzar un contenedor, lo que no es portable; estado intermedio visible al usuario |
| La API migra siempre | Simple | Rompe la separación con el despliegue, riesgo de migraciones concurrentes entre réplicas, y la API necesitaría permisos de DDL sobre todos los esquemas |
| **Híbrida: one-shot para lo existente, API para el esquema nuevo bajo advisory lock** | El alta termina en una sola operación; el despliegue sigue migrando todo lo existente; el lock impide dos migraciones del mismo esquema | La API necesita Flyway y permiso de DDL, acotado al alta; hay que probar la concurrencia |

## Decisión

Se adopta la opción híbrida.

1. **Esquemas existentes** (despliegue y actualizaciones): los migra el contenedor one-shot, en bucle, antes de arrancar la API, con la tabla `catalogo.migracion_tenant` para saber cuáles van al día. Sin cambios.
2. **Esquema nuevo** (alta de clínica): el aprovisionamiento crea el esquema vacío, lo registra como `pendiente` en `catalogo.migracion_tenant`, y **la API aplica el historial de `database/migrations/tenant/`** a ese solo esquema.
3. **Advisory lock.** Antes de migrar, la API toma `pg_advisory_lock` con una clave derivada del nombre del esquema, en la misma conexión que ejecuta Flyway. Dos altas o un alta y el runner sobre el mismo esquema se serializan; el segundo ve el estado `aplicada` y no hace nada.
4. **Idempotencia y estados.** El paso solo pasa a `aplicada` si Flyway termina; si falla queda `fallida`, con `ultimo_intento`, y el job de reconciliación lo reintenta. La API **nunca** migra un esquema que ya figura como `aplicada` ni toca el catálogo.
5. **Permisos.** El rol con el que la API ejecuta este paso solo puede crear objetos en esquemas `clinica_*`; el resto de la API usa un rol sin DDL.

## Consecuencias

### Se gana

- El alta de una clínica es una operación sincrónica y verificable, sin depender de lanzar un contenedor desde la API.
- La misma imagen y las mismas migraciones sirven en local y en la nube.
- Dos migraciones concurrentes del mismo esquema quedan impedidas por la base de datos, no por una revisión.

### Se paga

- La API incluye Flyway y un rol con DDL acotado.
- Hay que probar la concurrencia con Testcontainers: dos altas simultáneas y un alta contra el runner.

### Queda prohibido a partir de ahora

- Que la API migre el catálogo o un esquema ya `aplicada`.
- Que la API migre sin tomar el advisory lock.
- Aplicar migraciones de tenant desde un endpoint de negocio: solo el aprovisionamiento y la reconciliación las disparan.

## Cómo se hace cumplir

| Mecanismo | Qué garantiza | Estado |
|---|---|---|
| Prueba de aprovisionamiento concurrente | Solo una migración del mismo esquema avanza | PR 4 de la fase 2 |
| Rol de base de datos con DDL solo en `clinica_*` | La API no puede alterar el catálogo | PR 4 de la fase 2 |
| Revisión de PR | Que nadie migra desde un endpoint de negocio | Activo |

## Referencias

- `docs/Decisiones_tecnicas.md` §1.
- `docs/Despliegue_y_operacion.md` §5.
- `database/migrations/catalogo/V2__estado_migracion_tenant.sql`.
- Issue #3 (fase 2), PR 4.
