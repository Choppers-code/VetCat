# ADR-0006 · varios roles por usuario en una clínica

| | |
|---|---|
| **Estado** | Aceptada |
| **Fecha** | 2026-11-20 |
| **Área** | 1 · Arquitectura |
| **Issue** | #76 |
| **Decide** | Un usuario puede tener varios roles en una misma clínica; el token de tenant lleva la lista `roles` y el permiso es la unión de los roles de esa clínica |

## Contexto

El [ADR-0003](0003-token-por-clinica.md) supone un solo rol por usuario y clínica: `catalogo.usuario_clinica` tiene una fila por `(keycloak_id, clinica_id)` y el token de tenant lleva un `rol`. Los requerimientos (`docs/requerimientos/03_IDA_identidad_acceso.md`) piden algo distinto: en una clínica pequeña, la misma persona suele ser `veterinario` y `clinica_admin` a la vez, y un `cliente` puede ser además personal de la clínica.

Además, los requerimientos renombran los roles. Los ADR ya aceptados no se reescriben, así que este deja la equivalencia.

## Opciones consideradas

| Opción | Ventaja | Costo |
|---|---|---|
| **Una fila por rol** en `usuario_clinica` | Cambio mínimo al modelo; la pertenencia sigue siendo una sola fuente de verdad | El token lleva una lista y la autorización la interpreta |
| Un rol "compuesto" por cada combinación | Un solo valor en el token | Crece sin control: cada combinación es un rol nuevo en la matriz |
| Un usuario distinto por rol | Sin cambios de modelo | Obliga a la persona a tener dos cuentas y rompe la auditoría |

## Decisión

1. `catalogo.usuario_clinica` tiene una fila por **`(keycloak_id, clinica_id, rol)`**, con `estado`.
2. El token de tenant lleva **`roles`** (lista) en lugar de un solo `rol`. El resto del ADR-0003 no cambia: lo emite el backend, dura 15 minutos y lleva `tenant_id`.
3. El permiso es la **unión** de los permisos de los roles que el usuario tiene en **esa** clínica. Un rol de otra clínica no cuenta.
4. `super_admin` sigue siendo el único rol que viene del realm.
5. Un `clinica_admin` no puede crear usuarios `super_admin` ni `cliente`, ni quitarse el último rol `clinica_admin`, ni asignarse permisos que no tiene.
6. **Equivalencia de nombres.** Los ADR anteriores conservan los nombres con los que se escribieron:

| Nombre anterior | Nombre actual |
|---|---|
| `administrador_plataforma` | `super_admin` |
| `administrador_clinica` | `clinica_admin` |
| `dueno` | `cliente` |
| `veterinario` | `veterinario` |
| `recepcionista` | `recepcionista` |

## Consecuencias

### Se gana

- Una persona con dos funciones en la clínica tiene una sola cuenta y una sola sesión.
- La pertenencia sigue en un solo lugar, y el cambio no toca a Keycloak.

### Se paga

- El token y la autorización manejan una lista; cada `@PreAuthorize` debe evaluar contra la unión.
- Quitar un rol tarda como mucho 15 minutos en aplicarse, igual que una baja (ADR-0003).

### Queda prohibido a partir de ahora

- Copiar los roles de una clínica a otra.
- Que un `clinica_admin` se asigne permisos que no tiene.
- Tomar los roles del realm para el token de tenant: salen de `usuario_clinica`.

## Cómo se hace cumplir

| Mecanismo | Qué garantiza | Estado |
|---|---|---|
| Prueba de usuario con dos roles | Que el permiso es la unión y no más | Fase 3 |
| Prueba de rol ajeno | Que los roles de la clínica A no valen en la B | Fase 3 |
| `Matriz_roles.md` | Que cada permiso sale de una fila escrita | Activo |

## Referencias

- [ADR-0003](0003-token-por-clinica.md), que este ADR enmienda.
- `docs/Decisiones_tecnicas.md` §3 y §4.
- `docs/Matriz_roles.md`.
- `docs/requerimientos/03_IDA_identidad_acceso.md`.
