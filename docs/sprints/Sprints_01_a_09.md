# Sprints 1 a 9 · Fases 0 a 3

Parte de [`Plan_de_sprints.md`](../Plan_de_sprints.md). Cierra la base del repositorio, levanta el stack local, implementa el aislamiento entre clínicas y deja la autenticación y los planes funcionando.

**Cómo leer las tablas.** *Req.* es el módulo de `docs/requerimientos/`. *Depende de* son filas o issues que deben estar fusionados antes. Un `#N` ya existe como issue; el resto se crea al empezar el sprint.

## Sprint 1 · 1 – 7 nov 2026

Sin trabajo registrado en el repositorio.

## Sprint 2 · 8 – 14 nov 2026 · Fase 0 y 1 (entregado)

| Entregado | Responsable | Referencia |
|---|---|---|
| Guía de trabajo, ADR de Git Flow, scripts de ramas y README | `Anton-code-sp` | #22, #28, #30, #31 |
| ADR de versiones del stack, token por clínica, consola web y RustFS | `Anton-code-sp`, `Carlosss1819` | #43, #44, #45, #50 |
| Matriz de roles y alineación de alcance y tenencia | `Anton-code-sp` | #46, #47 |

## Sprint 3 · 15 – 21 nov 2026 · Fase 0 y 1 (cierre) · H1a

| Issue | Responsable | Req. | Estado |
|---|---|---|---|
| Compose parametrizado: PostgreSQL 17, Keycloak, RustFS y pgAdmin | `Anton-code-sp` | — | Entregado (#54) |
| Runner de Flyway y primera migración del catálogo | `Anton-code-sp` | — | Entregado (#55) |
| CI del compose y documentación de variables | `Anton-code-sp` | — | Entregado (#56, #57) |
| Esqueleto de Flutter: dos apps y dos paquetes | `Nacho-Nacho3` | — | Entregado (#59) |
| Requerimientos: índice, RNF, matriz y módulos 01 a 04 | `Raul-Rolu` | todos | Entregado (#67, #69) |
| Requerimientos, módulos 05 a 13 | `Raul-Rolu` | todos | PR abiertos (#70, #71) |
| Alinear los documentos con los requerimientos (matriz de roles, alcance, decisiones técnicas, ADR, roadmap, equipo, flujo, despliegue, AGENTS y README) | `Raul-Rolu` | todos | PR abiertos (#72 a #80) |
| Spec Kit y plan de sprints | `Raul-Rolu` | — | PR abiertos |

## Sprint 4 · 22 – 28 nov 2026 · Fase 1 (cierre) y 2 · Corte del 30 de noviembre

Es el sprint del plazo (ver [`Plan_de_sprints.md`](../Plan_de_sprints.md) §2). La capacidad se reparte entre los cimientos y la revisión de los PR de documentación.

| Issue | Responsable | Req. | Depende de |
|---|---|---|---|
| Revisar y aprobar los PR de documentación y de Spec Kit (#70 a #80 y los de Spec Kit y sprints) | `Anton-code-sp` | todos | PR abiertos |
| Spike: Boot 4.1 y JDK 25 con Security 7, springdoc, Testcontainers y ArchUnit | `Anton-code-sp` | — | #40 |
| ADR de aprovisionamiento en runtime y de restablecer `search_path` | `Anton-code-sp` | CLI | #15, #16 |
| Esqueleto del backend: Gradle y Spring Boot | `Carlosss1819` | — | Spike |
| CI de build y pruebas en cada PR | `Carlosss1819` | — | Esqueleto, #20 |
| Retirar RustFS del compose y de `.env.example`, y añadir el buzón de correo de pruebas | `Carlosss1819` | IDA | ADR-0009 fusionado |
| Convención del sobre de respuesta y OpenAPI base | `Raul-Rolu` | RNF | #18 |
| Primera especificación real con Spec Kit: la agenda | `ToNyyF1` | AGE | Spec Kit fusionado |
| Tema del design system: colores, tipografía y espaciado | `Nacho-Nacho3` | — | #60 |

## Sprint 5 · 29 nov – 5 dic 2026 · Fase 2 · Plazo: 30 nov

Los días 29 y 30 de noviembre cierran el plazo: revisión del Sprint 4 con el tablero, planificación del Sprint 5 y arranque del modelo del catálogo. El resto del sprint ya queda fuera del plazo.

| Issue | Responsable | Req. | Depende de |
|---|---|---|---|
| Modelo del catálogo: `clinica` y `usuario_clinica` por `(keycloak_id, clinica_id, rol)` | `Anton-code-sp` | CLI, IDA | Spike |
| Resolver de tenant y proveedor de conexiones con `search_path` que incluye `ext` | `Anton-code-sp` | CLI | Modelo del catálogo |
| Bucle de migración por esquema de clínica (Flyway y `migracion_tenant`) | `Carlosss1819` | CLI | #55 |
| Exigir los checks de CI en el ruleset de `develop` y `main` | `Carlosss1819` | — | CI de build |
| Cliente HTTP y sobre de respuesta en `vetcat_core` | `Nacho-Nacho3` | RNF | Convención del sobre |
| Especificar propietarios y mascotas con Spec Kit | `Raul-Rolu` | PRO | — |
| Plan y tareas de la agenda con Spec Kit | `ToNyyF1` | AGE | Especificación de la agenda |

Capacidad: el check de BOM (#63) y el de variables (#62) entran aquí si sobra capacidad en infraestructura.

## Sprint 6 · 6 – 12 dic 2026 · Fase 2 (cierre) y 3 · H2

| Issue | Responsable | Req. | Depende de |
|---|---|---|---|
| Aprovisionamiento de una clínica: esquema, migración y fila de catálogo | `Anton-code-sp` | CLI | Resolver, bucle de migración |
| Prueba de aislamiento con dos clínicas (Testcontainers) | `Carlosss1819` | RNF | Aprovisionamiento |
| Realm `vetcat`: roles y clientes públicos con PKCE para la app y la consola | `Carlosss1819` | IDA | Buzón de correo |
| Matriz de roles por acción (`Matriz_roles.md`) | `Raul-Rolu` | todos | #17 |
| Contrato OpenAPI de propietarios y mascotas | `Raul-Rolu` | PRO | Convención |
| Contrato OpenAPI de la agenda | `ToNyyF1` | AGE | Convención |
| Navegación base y estructura de `features/veterinario` y `features/cliente` | `Nacho-Nacho3` | — | Tema |

**H2:** dos clínicas de prueba con datos aislados y acceso cruzado denegado.

## Sprint 7 · 13 – 19 dic 2026 · Fase 3

| Issue | Responsable | Req. | Depende de |
|---|---|---|---|
| Token de tenant por clínica con lista de `roles`, validado contra `usuario_clinica` | `Anton-code-sp` | IDA | Realm |
| `@PreAuthorize` base y respuestas 401 y 403 con el sobre de error | `Anton-code-sp` | RNF | Token por clínica |
| Modelo de planes: `plan`, `plan_funcion`, `plan_limite` y `pago_plan` | `Anton-code-sp` | PLA | Catálogo |
| Prueba de autorización: 401 sin token y 403 con rol equivocado | `Carlosss1819` | RNF | `@PreAuthorize` |
| Inicio de sesión en la app: PKCE y almacenamiento seguro | `Nacho-Nacho3` | IDA | Realm |
| Identidad de la clínica y logo (imagen PNG o JPG de hasta 500 KB, guardada en la base) | `Raul-Rolu` | CON | Aislamiento |

Capacidad reducida desde esta semana por las fechas de fin de año.

## Sprint 8 · 20 – 26 dic 2026 · Fase 3

| Issue | Responsable | Req. | Depende de |
|---|---|---|---|
| Verificación de funciones y límites del plan en cada petición | `Anton-code-sp` | PLA | Modelo de planes |
| Personal de la clínica: crear, editar y desactivar con varios roles | `Raul-Rolu` | CON, IDA | Token por clínica |
| Recuperación de contraseña e invitaciones por correo | `Carlosss1819` | IDA | Buzón de correo |
| Guardas de ruta por rol y por función del plan | `Nacho-Nacho3` | IDA, PLA | Inicio de sesión |

## Sprint 9 · 27 dic – 2 ene 2027 · Fase 3 (cierre) · H1b

| Issue | Responsable | Req. | Depende de |
|---|---|---|---|
| Vigencia del plan y suspensión automática con 3 días de gracia | `Anton-code-sp` | PLA, CLI | Verificación de funciones |
| Invitar a un cliente y código de vinculación de un solo uso (2 días) | `Raul-Rolu` | IDA | Personal de la clínica |
| Prueba de funciones de plan: error aunque el rol lo permita | `Carlosss1819` | PLA | Verificación de funciones |
| Selector de clínica activa y cambio de vista veterinario o cliente | `Nacho-Nacho3` | IDA | Guardas de ruta |

**H1b:** un usuario del realm `vetcat` entra y obtiene token.
