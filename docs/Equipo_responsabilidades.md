# Equipo y responsabilidades

Cinco personas, cinco áreas. La regla que evita el solape: **cada módulo y cada superficie tiene un solo dueño**; los demás aportan por PR revisado, no escribiendo en paralelo.

## Las cinco áreas

| # | Área | Objetivo | Fases | Dónde escribe |
|---|---|---|---|---|
| 1 | Líder técnico y arquitectura | Que el sistema funcione como uno solo: arquitectura, seguridad, contratos, revisión e integración | 0, 2, 3, 10 | `backend/.../security`, `tenancy`, `catalogo`, `plataforma`; `docs/` |
| 2 | Backend — Pacientes y expediente | Propietarios, mascotas, expediente clínico, adjuntos y backend del portal | 4, 6, 8 | `backend/.../propietarios`, `mascotas`, `expediente`, `portal` |
| 3 | Backend — Operación y cobro | Agenda, vacunas, facturación y caja | 5, 7, 9 | `backend/.../citas`, `vacunas`, `facturacion` |
| 4 | Frontend Flutter | Design system, autenticación en la app, navegación y pantallas complejas | 3 a 10 | `frontend/apps`, `frontend/packages` |
| 5 | Infraestructura, QA y seguridad | Docker, PostgreSQL, Keycloak, migraciones, CI/CD, Play Store y pruebas transversales | 1, 11 | `infrastructure/`, `database/`, `.github/` |

La fase 3 la comparten las áreas 1, 4 y 5: el backend valida tokens, Keycloak se configura y la app implementa el flujo. No es un solape porque cada una toca una capa distinta.

## Fronteras explícitas

| Punto compartido | Dueño | Los demás solo |
|---|---|---|
| Keycloak | 5 configura realm, clientes y export JSON | 1 valida tokens; 4 usa el cliente ya configurado |
| Spring Security y resolver de tenant | 1 | 2 y 3 usan las anotaciones, no las modifican |
| Migraciones | Cada dueño de módulo escribe las suyas | 5 es dueño del runner, del versionado y del aprovisionamiento |
| Pruebas | Cada dueño prueba su módulo | 5 es dueño de las transversales: aislamiento, concurrencia, autorización, aceptación |
| Adjuntos | 2 es dueño del módulo (entidad, validación, URL firmada) | 5 aprovisiona el almacenamiento |
| Portal del dueño | 2 backend, 4 UI | 3 aporta solo la consulta de vacunas |
| Autenticación en la app | 5 configura, 4 implementa el flujo, 1 valida | — |
| Design system | 4 | 2 y 3 arman sus pantallas con esos componentes |
| Consola web de administración | 1 backend, 4 UI | — |
| Contratos OpenAPI | El dueño del módulo escribe el del suyo | 1 revisa la convención; 2, 3 y 4 lo consumen |

## Cómo se hace cumplir

El documento dice de quién es cada cosa; estos dos mecanismos lo ejecutan.

**Una tarjeta, un responsable.** En el tablero, el campo `Área` indica la columna del equipo y el *assignee* es el único responsable. Los apoyos no se asignan a la tarjeta: se piden como revisores del PR. Dos assignees en una tarjeta es la forma más rápida de que nadie responda.

**CODEOWNERS.** Un PR que toca una carpeta de otro pide su revisión solo. Es la diferencia entre una regla escrita y una regla que se cumple.

| Ruta | Área |
|---|---|
| `/backend/src/main/java/com/vetcat/security/` | 1 |
| `/backend/src/main/java/com/vetcat/tenancy/` | 1 |
| `/backend/src/main/java/com/vetcat/catalogo/` | 1 |
| `/backend/src/main/java/com/vetcat/plataforma/` | 1 |
| `/backend/src/main/java/com/vetcat/propietarios/` | 2 |
| `/backend/src/main/java/com/vetcat/mascotas/` | 2 |
| `/backend/src/main/java/com/vetcat/expediente/` | 2 |
| `/backend/src/main/java/com/vetcat/portal/` | 2 |
| `/backend/src/main/java/com/vetcat/citas/` | 3 |
| `/backend/src/main/java/com/vetcat/vacunas/` | 3 |
| `/backend/src/main/java/com/vetcat/facturacion/` | 3 |
| `/frontend/` | 4 |
| `/database/` | 5 |
| `/infrastructure/` | 5 |
| `/.github/` | 5 |
| `/docs/` | 1 |

## Matriz (R = único responsable, A = apoyo puntual)

| Área | 1 | 2 | 3 | 4 | 5 |
|---|---|---|---|---|---|
| Repositorio, Git Flow, convenciones | R | | | | A |
| Convención OpenAPI y sobre de respuesta | R | A | A | A | |
| Seguridad backend y resolver de tenant | R | | | | A |
| Keycloak: realm, clientes, export | A | | | A | R |
| Docker, redes, volúmenes | | | | | R |
| PostgreSQL, extensiones, respaldos | A | | | | R |
| Flyway y aprovisionamiento de esquemas | A | | | | R |
| Propietarios y mascotas | | R | | | |
| Expediente clínico | | R | | | |
| Adjuntos | | R | | | A |
| Agenda, consultorios, horarios | | | R | | |
| Vacunas | | | R | | |
| Facturación y caja | | | R | | A |
| Portal del dueño (backend) | | R | A | A | |
| Administración de plataforma (backend) | R | | | A | |
| Design system y navegación | | | | R | |
| Cliente HTTP y sobre de respuesta | | A | A | R | |
| Autenticación en la app (PKCE, sesión) | A | | | R | A |
| Pantallas de propietarios, mascotas y expediente | | R | | A | |
| Calendario de agenda y dashboard | | | A | R | |
| Formulario de cita y pantallas de vacunas | | | R | A | |
| Pantallas de facturación y caja | | | R | A | |
| Portal del dueño (UI) | | A | | R | |
| Consola web de administración (UI) | A | | | R | |
| Pruebas de módulo | | R | R | R | A |
| Pruebas de aislamiento y concurrencia | A | | | | R |
| Auditoría | R | A | A | | A |
| CI/CD, firma de APK y Play Store | | | | A | R |
| Documentación de API | | R | R | R | A |
| Manuales de instalación y operación | | | | | R |

Cada dueño de módulo escribe el contrato de su módulo; la fila "Convención OpenAPI" es la del formato común, no la de todos los contratos.

## Reglas de trabajo

1. **Un módulo de backend, un dueño.** Nadie más escribe en `propietarios/`, `citas/` o `facturacion/`.
2. **Una superficie de UI, un dueño.** La persona 4 es dueña del design system; si un widget se usa dos veces, vive ahí y no se copia.
3. **Lo transversal solo lo toca 1 o 5.** Si un dueño de módulo necesita cambiar seguridad o el compose, lo pide.
4. **Contrato antes que código.** El dueño del módulo escribe el OpenAPI; la persona 1 lo revisa. Así la 1 no se vuelve cuello de botella.
5. **Una pantalla piloto.** La persona 4 construye la pantalla de mascotas como referencia; las demás se replican sobre ese patrón.
6. **Artefactos versionados.** El export del realm y las migraciones son archivos, no configuración oral.

## Carga y riesgos

| Persona | Carga | Riesgo |
|---|---|---|
| 1 | Arquitectura, seguridad, tenants, administración de plataforma, auditoría, PRs | Alta, pero sin código de módulos |
| 2 | Propietarios, mascotas, expediente, adjuntos, portal backend y sus pantallas | Alta |
| 3 | Agenda, vacunas, facturación y caja, y sus pantallas | Alta |
| 4 | Design system, autenticación, cliente HTTP, calendario, dashboard, portal y consola web | Media-alta |
| 5 | Docker, PostgreSQL, Keycloak, Flyway, CI/CD, Play Store y pruebas transversales | Alta |

Riesgo principal: la persona 4 entrega los componentes tarde y las personas 2 y 3 empiezan sus pantallas antes de tiempo. Mitigación: la pantalla piloto y el orden del roadmap.

El desbalance se vigila en la vista del tablero agrupada por responsable y filtrada a lo que no está cerrado. Quince minutos a la semana; si una columna crece dos semanas seguidas, se reasigna trabajo, no se espera.

## Continuidad

- Si el dueño de un área no está, el trabajo de su módulo **espera**; no lo toma otro por iniciativa propia. La persona 1 reasigna en el tablero si la espera compromete una fase.
- Un cambio de dueño es una decisión explícita: se actualiza esta tabla, CODEOWNERS y las tarjetas abiertas, en el mismo PR.
- La persona 1 es el único punto de revisión de arquitectura, y por eso no es dueña de módulos de negocio: si además escribiera `citas/`, revisaría su propio código.
