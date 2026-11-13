# ADR-0004 · autenticación de la consola web

| | |
|---|---|
| **Estado** | Aceptada |
| **Fecha** | 2026-11-12 |
| **Área** | 4 · Frontend, con el área 1 |
| **Issue** | #37 |
| **Decide** | Móvil y consola web comparten la interfaz de sesión y no la implementación: `flutter_appauth` en la app, `openid_client` en el navegador, tokens en memoria |

## Contexto

El plan tiene dos clientes y un solo flujo de autenticación escrito. `Tecnologias_y_patrones.md` elige `flutter_appauth` y `flutter_secure_storage`, y `Decisiones_tecnicas.md` §9 describe únicamente el flujo móvil: cliente público, PKCE, redirect a esquema propio `vetcat://callback`, navegador del sistema, tokens en el almacenamiento seguro.

La consola de administración es Flutter Web (`Tecnologias_y_patrones.md` §1, `Alcance.md` §Clientes), y ahí esa receta no se sostiene:

- `flutter_appauth` soporta Android, iOS y macOS. No web.
- `vetcat://callback` es un esquema de aplicación: un navegador no lo tiene registrado.
- "Almacenamiento seguro del sistema" describe el Keychain y el Keystore. En el navegador no existen.
- El `redirect_uri` de `Despliegue_y_operacion.md` §3 es un `http://localhost:PUERTO/*` genérico, que no distingue app de consola.
- No hay una sola mención a CORS en los ocho documentos, y una consola que llama a otro origen lo necesita en los dos lados: en Keycloak para el `/token` y en Spring para la API.

Sin esta decisión no se pueden empezar ni el paquete `frontend/packages/core` ni la fase 10.

## Opciones consideradas

| Opción | Ventaja | Costo |
|---|---|---|
| `openid_client` con PKCE en el navegador | Ya está en la tabla del stack como alternativa; mismo modelo de sesión que móvil y misma interfaz | Los tokens viven en el navegador: hay que decidir dónde y por cuánto tiempo |
| Adaptador JS de Keycloak | Oficial y muy probado | Depende de un script externo y de globales JS: rompe el patrón de una interfaz por variante y mete una segunda forma de hacer lo mismo |
| BFF con sesión por cookie | El navegador nunca ve un token | Una pieza más que desplegar, una sesión que mantener y un salto más en cada petición: desproporcionado para una consola interna |

## Decisión

1. **Una interfaz, dos implementaciones.** En `frontend/packages/core` hay un servicio de sesión único (`iniciar`, `renovar`, `cerrar`); `flutter_appauth` lo implementa en móvil y `openid_client` en la consola. Ningún widget conoce la librería.
2. **Un cliente de Keycloak por cliente de la aplicación**, los dos públicos, con PKCE y sin secreto: el de la app y el de la consola. Cada uno con su `redirect_uri` por entorno y sus *Web Origins* declarados. Lo que no se hace es compartir cliente entre app y navegador: los flujos de redirect son distintos.
3. **En el navegador los tokens viven en memoria.** Recargar la pestaña reautentica; no hay persistencia. `localStorage` y `flutter_secure_storage` quedan fuera: en el navegador no son un almacén seguro.
4. **CORS explícito y restringido**, sin credenciales, porque los tokens viajan en la cabecera `Authorization`: en Spring, solo el origen de la consola; en Keycloak, solo ese mismo origen en *Web Origins*.
5. **La consola usa el mismo modelo de sesión que la app**: identidad en Keycloak, y si el usuario es de clínica, token de tenant según el ADR-0003. `administrador_plataforma` no elige clínica y trabaja con el token de identidad.

## Consecuencias

### Se gana

- La consola deja de ser un hueco de diseño: tiene flujo, cliente, origen y política de tokens.
- Un solo modelo mental de sesión en los dos clientes, y el paquete común no se ramifica.
- Sin secretos de cliente en el navegador: son clientes públicos con PKCE, como manda el plan.

### Se paga

- Recargar la pestaña cuesta una reautenticación. Aceptable en una consola de administración, que se usa en sesiones cortas.
- Dos clientes de Keycloak que mantener sincronizados en `infrastructure/keycloak/realm-vetcat.json`.
- CORS es una configuración que hay que acertar en dos sitios; mal puesta, el síntoma es un navegador que no ve el error.

### Queda prohibido a partir de ahora

- Responder "`flutter_appauth`" a la pregunta de cómo se autentica el proyecto.
- Guardar tokens en `localStorage`, en `sessionStorage` o en `flutter_secure_storage` desde el navegador.
- Abrir un WebView para el login en cualquiera de los dos clientes.
- CORS con `*` o con credenciales.

## Cómo se hace cumplir

| Mecanismo | Qué garantiza | Estado |
|---|---|---|
| Prueba del paquete `core` | Que el servicio de sesión es sustituible y ningún widget importa la librería | Fase 3 |
| Configuración de CORS con un solo origen | Que la API no queda abierta a cualquier página | Fase 3 |
| Revisión de PR | Que no aparece un cliente de Keycloak nuevo sin su `redirect_uri` y sus *Web Origins* | Activo |

## Referencias

- `docs/Decisiones_tecnicas.md` §9 — el flujo móvil, que sigue vigente tal como está.
- `docs/adr/0003-token-por-clinica.md` — identidad y token de tenant.
- `docs/Despliegue_y_operacion.md` §3 — `redirect_uri` por entorno.
