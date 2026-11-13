# ADR-0003 · emisión del token por clínica

| | |
|---|---|
| **Estado** | Aceptada |
| **Fecha** | 2026-11-12 |
| **Área** | 1 · Arquitectura |
| **Issue** | #36 |
| **Decide** | El token de tenant lo emite el backend tras validar la pertenencia; Keycloak emite solo la identidad |

## Contexto

`Decisiones_tecnicas.md` fija que el usuario elige la clínica activa al entrar y recibe un token acotado a ella, que `tenant_id` proviene solo de un token firmado y que el rol efectivo sale de `catalogo.usuario_clinica`, no del rol de realm. Lo que quedó abierto es **quién firma ese token y con qué mecanismo**, y sin eso no hay esquema que resolver: la fase 2 y la fase 3 no pueden empezar.

Las restricciones que la decisión no puede romper son cuatro: la pertenencia tiene una sola fuente de verdad (`usuario_clinica`), no se duplica en Keycloak y en base de datos a la vez, el tenant no viaja en parámetro ni cabecera, y el rol no se hereda de una clínica a otra.

Hay una quinta que no está escrita en ningún documento y es la que decide el asunto: **Keycloak no sabe qué clínica eligió el usuario**. La elección ocurre después del login, en la app. Para emitir `tenant_id` en el token de Keycloak haría falta que alguien se lo contara, y eso significa una extensión que captura la elección y otra que la escribe en el token.

## Opciones consideradas

| Opción | Ventaja | Costo |
|---|---|---|
| Mapper/SPI propio en Keycloak que lea `catalogo.usuario_clinica` | Una sola autoridad de emisión; el tenant llega en el token del IdP, como en cualquier Resource Server | Acopla Keycloak al esquema de la aplicación y le da credenciales de su base; y no basta con el mapper: hace falta además un autenticador que guarde la clínica elegida en la sesión de Keycloak. Dos extensiones propias que mantener y versionar con el esquema |
| Organizations de Keycloak | Nativo, sin código | Duplica la pertenencia dentro de Keycloak: choca con "una sola fuente de verdad" y con el anti-patrón de `Tecnologias_y_patrones.md` §5 |
| **Token de tenant emitido por el backend** | Sin extensiones en Keycloak, sin duplicar la pertenencia, y el tenant sigue llegando en un token firmado; el backend conserva la potestad de re-verificar | Dos emisores que validar y una clave de firma propia que custodiar |

## Decisión

1. **Identidad y autorización se separan.** Keycloak autentica y emite un token de identidad con el `sub`; el backend emite un **token de tenant** con lo que decide la autorización.
2. `GET /api/v1/sesion/clinicas` devuelve las clínicas activas del usuario, leídas de `catalogo.usuario_clinica` con la unidad de persistencia **no** multi-tenant.
3. `POST /api/v1/sesion/clinicas/{id}` valida que la relación exista y esté activa, y devuelve el token de tenant: firmado por la API, 15 minutos de vida, con `sub` (el `keycloak_id`), `tenant_id` (identificador interno de `catalogo.clinica`, nunca el nombre del esquema), el rol efectivo de **esa** clínica y un `jti`.
4. **El token de identidad solo vale para abrir sesión y para la plataforma.** `/api/v1/sesion/**` y `/api/v1/plataforma/**` se autorizan con el token de identidad; todo lo demás exige token de tenant. El motivo del segundo es que `administrador_plataforma` no pertenece a ninguna clínica —no tiene fila en `usuario_clinica`— y su trabajo empieza antes de que la clínica exista. Es el **único** rol que se toma del realm, y solo porque no es un rol por clínica.
5. La clave de firma vive en el gestor de secretos, con `kid` desde el primer día para poder rotarla sin cortar sesiones.

## Consecuencias

### Se gana

- Una sola verdad de la pertenencia, y la elección de clínica se resuelve donde ya están los datos para resolverla.
- Keycloak queda como IdP puro: no se le pide leer el esquema de la aplicación ni se le da una credencial más.
- El rol de una clínica no puede viajar a otra, porque sale de la fila concreta en el momento de emitir.

### Se paga

- Dos emisores, dos `JwtDecoder` y un `JwtIssuerAuthenticationManagerResolver`. Es la parte del diseño que más se parece a tener un servidor de autorización propio, aunque sea pequeño.
- La pertenencia se valida **al emitir**, no en cada petición: un cambio de rol o una baja tardan como mucho 15 minutos en aplicarse. Si eso no fuera aceptable, habría que añadir una lectura al catálogo por petición con caché, y esa es otra decisión.
- Hay que custodiar y rotar una clave de firma, y el día que haya varios nodos, todos tienen que ver la misma clave.

### Queda prohibido a partir de ahora

- Aceptar el token de identidad fuera del endpoint de sesión.
- Tomar el tenant de un parámetro, cuerpo o cabecera (ya vigente).
- Copiar el rol de realm al token de tenant: el rol se lee de `usuario_clinica`.
- Emitir un token de tenant sin `tenant_id`: si falta, el endpoint responde 401 en vez de resolver un esquema por defecto.
- Dejar que el rol de plataforma abra datos de una clínica: con el token de identidad no hay `tenant_id`, y sin `tenant_id` no hay esquema que resolver.

## Cómo se hace cumplir

| Mecanismo | Qué garantiza | Estado |
|---|---|---|
| Dos validadores enrutados por `iss` | Que el token de identidad no abre nada fuera de `sesion/` y `plataforma/` | Fase 3 |
| Prueba de rol ajeno | Que el rol de la clínica A falla en la clínica B | Fase 3 |
| Prueba de aislamiento con dos clínicas | Que una petición con token de la A nunca devuelve datos de la B | Fase 2 |

## Referencias

- `docs/Decisiones_tecnicas.md` §2 y §3.
- `docs/Tecnologias_y_patrones.md` §5 — anti-patrones de tenancy.
- `docs/Matriz_roles.md` — quién puede qué una vez dentro de la clínica.
