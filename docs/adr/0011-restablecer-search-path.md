# ADR-0011 · restablecer el search_path al devolver la conexión

| | |
|---|---|
| **Estado** | Aceptada |
| **Fecha** | 2026-11-20 |
| **Área** | 1 · Arquitectura |
| **Issue** | #16 |
| **Decide** | `MultiTenantConnectionProvider` restablece el `search_path` a un esquema vacío en un `finally` al devolver la conexión, y descarta la conexión del pool si el restablecimiento falla |

## Contexto

`Decisiones_tecnicas.md` §2 exige limpiar el contexto de tenant en un `finally`. Eso limpia el `ThreadLocal`, pero **no la conexión física**: el `search_path` es estado de la sesión de PostgreSQL y sigue puesto cuando la conexión vuelve al pool. La siguiente petición, de otra clínica, que reciba esa conexión sin fijar el suyo leería el esquema anterior. Es la fuga entre clínicas que todo el diseño de tenencia intenta evitar.

## Opciones consideradas

| Opción | Ventaja | Costo |
|---|---|---|
| **Restablecer en `releaseConnection` y descartar si falla** | Barato, explícito y a prueba de fallos: si no se puede limpiar, la conexión no se reutiliza | Hay que implementarlo en el provider y probarlo con concurrencia |
| `DISCARD ALL` al devolver | Limpia todo el estado de la sesión | Una ida y vuelta extra por conexión y borra también estado legítimo del pool |
| `SET LOCAL` dentro de la transacción | Se revierte solo al terminar la transacción | Hibernate pide la conexión antes de abrir la transacción, así que no cubre el caso; no es una defensa suficiente por sí sola |
| `connectionInitSql` de Hikari | Estado inicial fijo | Solo actúa al crear la conexión, no al devolverla |

## Decisión

1. `getConnection(tenantId)` fija `search_path` al esquema de la clínica (más `ext`, donde vive `btree_gist`) y a nada más.
2. `releaseConnection(tenantId, conexión)` restablece el `search_path` a un esquema vacío **dentro de un `try/finally`**, antes de devolver la conexión al pool.
3. **Falla cerrada:** si el restablecimiento lanza una excepción, la conexión no vuelve al pool; se descarta (se cierra la conexión física) y se registra el fallo sin datos de la clínica.
4. El esquema vacío por defecto hace que una conexión sin tenant **falle** al consultar tablas de negocio, en vez de leer datos de otra clínica. Coincide con "si no hay tenant, no hay respuesta".
5. Una prueba de concurrencia con Testcontainers es obligatoria: muchas peticiones de dos clínicas comparten un pool pequeño y ninguna puede ver filas de la otra.

## Consecuencias

### Se gana

- Un `search_path` ajeno no sobrevive a la devolución de la conexión, ni siquiera ante un fallo.
- La defensa es de la capa de conexión, no de que cada servicio se acuerde de limpiar.

### Se paga

- Una sentencia `SET` más por conexión devuelta.
- El pool puede rotar conexiones cuando el restablecimiento falla; es el comportamiento buscado.

### Queda prohibido a partir de ahora

- Confiar solo en limpiar el `ThreadLocal`.
- Devolver al pool una conexión cuyo `search_path` no se pudo restablecer.
- Usar un esquema por defecto con datos de negocio como `search_path` inicial.

## Cómo se hace cumplir

| Mecanismo | Qué garantiza | Estado |
|---|---|---|
| Prueba de concurrencia con dos clínicas y un pool pequeño | Ninguna petición lee filas de otra clínica | PR 3 de la fase 2 |
| Prueba de fallo del restablecimiento | La conexión se descarta y no se reutiliza | PR 3 de la fase 2 |
| Revisión de PR | Que el provider no se salta el `finally` | Activo |

## Referencias

- `docs/Decisiones_tecnicas.md` §1 y §2.
- [ADR-0010](0010-aprovisionamiento-en-runtime.md).
- Issue #3 (fase 2), PR 3.
