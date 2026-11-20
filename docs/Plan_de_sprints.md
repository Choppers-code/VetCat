# Plan de sprints

Plan semanal del proyecto completo, sin corte de MVP, desde el Sprint 1 (domingo 1 de noviembre de 2026). Cada sprint es un periodo fijo de una semana (domingo a sábado) con un conjunto de issues del tablero; el sprint no es un release (ver [`Flujo_de_trabajo.md`](Flujo_de_trabajo.md) §8 y [`Scrum.md`](Scrum.md)).

**Plazo: 30 de noviembre de 2026.** El proyecto completo no cabe en esa fecha: el plan completo dura 31 sprints. Lo que se compromete para el 30 de noviembre son los **cimientos** (sección 2); el resto del plan sigue después, sin cambios de alcance.

**Estado: propuesto.** Parte de los requerimientos de [`requerimientos/`](requerimientos/README.md) y del reparto de áreas propuesto para [`Equipo_responsabilidades.md`](Equipo_responsabilidades.md). Debe confirmarlo el equipo; lo marcado como *supuesto* se corrige en el primer sprint en el que se compruebe.

## 1. Cómo se lee

- La **fase del Roadmap** es la épica; cada fila de los sprints es un **issue** (un PR, máximo 400 líneas efectivas).
- Cada issue pasa por Spec Kit antes de programarse: especificar, planear y dividir en tareas dentro de `specs/NNN-slug/` ([`Spec_Kit.md`](Spec_Kit.md)).
- Las filas citan el **módulo de requerimientos** (`AGE`, `EXP`…). Los IDs exactos de cada RF se fijan al especificar el issue.
- Una fila con `#N` ya tiene issue; las demás son **issues por crear** (se abren al empezar el sprint, no antes).
- El detalle está en tres archivos: [Sprints 1 a 9](sprints/Sprints_01_a_09.md) (fases 0 a 3), [Sprints 10 a 19](sprints/Sprints_10_a_19.md) (fases 4 a 7, hito H3) y [Sprints 20 a 31](sprints/Sprints_20_a_31.md) (fases 8 a 11).

## 2. Qué se entrega el 30 de noviembre

El plazo cubre el Sprint 4 (22 – 28 nov) y los dos primeros días del Sprint 5 (29 y 30 nov). Se compromete:

| # | Entregable | Responsable | Sprint |
|---|---|---|---|
| 1 | Documentación alineada con los requerimientos: los PR de documentación fusionados (#70 a #80) | `Anton-code-sp` revisa; `Carlosss1819` revisa el de `CODEOWNERS` | 4 |
| 2 | Spec Kit en el repositorio, con la constitución y las plantillas en español, y al menos una especificación real (agenda) | `Raul-Rolu`, `ToNyyF1` | 4 |
| 3 | Plan de sprints aprobado por el equipo | `Raul-Rolu` | 4 |
| 4 | Stack local sin RustFS y con buzón de correo de pruebas | `Carlosss1819` | 4 |
| 5 | Esqueleto del backend, CI de build y pruebas, y spike del stack cerrado (#40) | `Carlosss1819`, `Anton-code-sp` | 4 |
| 6 | Convención del sobre de respuesta y OpenAPI base (#18), y tema del design system (#60) | `Raul-Rolu`, `Nacho-Nacho3` | 4 |
| 7 | Revisión del Sprint 4 y planificación del Sprint 5, con el modelo del catálogo de tenancy iniciado | todos | 4 y 5 |

**No entra en el plazo:** el código de multi-tenancy, la autenticación, los planes y cualquier módulo funcional. Empiezan en el Sprint 5 y siguen la secuencia de las dependencias duras del Roadmap: multi-tenancy primero.

**Cuello de botella conocido.** Los 11 PR de documentación y los 5 de Spec Kit y sprints piden la aprobación del equipo `arquitectura`, y `Raul-Rolu` es autor de todos, así que la revisión recae en `Anton-code-sp`. Para llegar al 30 de noviembre hay que revisarlos en lotes durante el Sprint 4.

## 3. Equipo y reparto

| Persona | Área | Escribe sobre todo en |
|---|---|---|
| `Anton-code-sp` | 1 Arquitectura (responsable de las tarjetas de arquitectura); apoyo del área 5 | `security`, `tenancy`, `catalogo`, `plataforma`, `planes`, `docs/` |
| `Raul-Rolu` | 2 Pacientes y expediente; revisa el área 1 | `propietarios`, `mascotas`, `expediente`, `portal`; matriz de roles y convenciones |
| `ToNyyF1` | 3 Operación y cobro | `citas`, `vacunas`, `facturacion`, `notificaciones` |
| `Nacho-Nacho3` | 4 Frontend | `frontend/apps`, `frontend/packages` |
| `Carlosss1819` | 5 Infraestructura, QA y seguridad | `infrastructure/`, `database/`, `.github/`, pruebas transversales |

Reglas que el plan respeta:

1. **Una tarjeta, un responsable.** Los apoyos se piden como revisores del PR, no como segundo *assignee*.
2. **Quien escribe no aprueba.** Cada área tiene dos miembros en [`Flujo_de_trabajo.md`](Flujo_de_trabajo.md) §9; el otro miembro revisa.
3. **Lo transversal solo lo tocan las áreas 1 y 5.**
4. **Anton y Raúl comparten el área 1.** Para no repartir una tarjeta entre dos, este plan asigna las tarjetas de arquitectura a Anton y las de convenciones, matriz de roles y contratos a Raúl, y deja a Raúl como revisor de las de Anton.

## 4. Supuestos

| Supuesto | Valor | Qué pasa si no se cumple |
|---|---|---|
| Capacidad por persona | 3 issues de hasta 400 líneas efectivas por sprint | Se mueven filas al siguiente sprint; el calendario se corre |
| Corte de MVP | Ninguno (el alcance dice "proyecto completo"); el plazo del 30 de noviembre solo cubre los cimientos | Si el equipo define un corte, el candidato natural es el hito H3 (fin del Sprint 19) |
| Vacaciones de diciembre | Capacidad reducida en los Sprints 8 y 9 | Se corre una semana la fase 3 |
| Fases 4 y 5 | Primero propietarios y mascotas, después agenda | La agenda guarda `mascota_id` sin llave foránea hasta que existan las mascotas |
| Sprints 1 y 2 | El primer commit del repositorio es del 12 de noviembre | El Sprint 1 queda sin trabajo registrado |

## 5. Calendario

| Sprint | Fechas | Fase del Roadmap | Hito |
|---|---|---|---|
| 1 | 1 – 7 nov 2026 | Sin trabajo registrado en el repositorio | |
| 2 | 8 – 14 nov | 0 y 1 | Fase 0 completa; compose y migraciones del catálogo |
| 3 | 15 – 21 nov | 0 y 1 (cierre) | H1a |
| 4 | 22 – 28 nov | 1 (cierre) y 2 | **Corte del 30 de noviembre** (cimientos) |
| 5 | 29 nov – 5 dic | 2 | Plazo: 30 nov |
| 6 | 6 – 12 dic | 2 y 3 | **H2** |
| 7 | 13 – 19 dic | 3 | |
| 8 | 20 – 26 dic | 3 | |
| 9 | 27 dic – 2 ene | 3 | **H1b** |
| 10 – 11 | 3 – 16 ene | 4 | |
| 12 – 14 | 17 ene – 6 feb | 5 | |
| 15 – 17 | 7 – 27 feb | 6 | |
| 18 – 19 | 28 feb – 13 mar | 7 | **H3** (v1.0) |
| 20 – 21 | 14 – 27 mar | 8 | **H4** (v1.1) |
| 22 – 25 | 28 mar – 24 abr | 9 | **H5** (v1.2) |
| 26 – 28 | 25 abr – 15 may | 10 | (v1.3) |
| 29 – 31 | 16 may – 5 jun | 11 | **H6** (v2.0) |

El plan completo dura **31 sprints**, hasta el 5 de junio de 2027. Es una estimación: se recalcula al cerrar cada fase con la velocidad real.

## 6. Riesgos

| Riesgo | Efecto | Mitigación |
|---|---|---|
| `ToNyyF1` es el único responsable de agenda, vacunas, ventas, caja, notificaciones, inventario y reportes | Cuello de botella de los Sprints 12 al 25 | Raúl escribe inventario y reportes (Sprints 24 y 25) y `ToNyyF1` los aprueba; las demás áreas pueden escribir en `citas` con su revisión |
| `Raul-Rolu` está en las áreas 1 y 2 | Podría revisar su propio código | Anton es siempre el segundo revisor; las tarjetas de arquitectura de Raúl las aprueba Anton |
| `Anton-code-sp` aparece en las cinco áreas como revisor y es el único revisor posible de los PR de `Raul-Rolu` en arquitectura | Cuello de botella de aprobaciones, agudo en el Sprint 4 | El apoyo del área 5 solo entra con los disparadores de suplencia de `Equipo_responsabilidades.md` |
| El spike de Boot 4.1, JDK 25 y Security 7 (#40) sigue abierto | La fase 2 podría rehacerse si algo no es compatible | Se cierra en el Sprint 4, antes de escribir código de tenancy |
| El plazo del 30 de noviembre cubre una semana y dos días | El código de negocio no llega a esa fecha | El compromiso del plazo son los cimientos de la sección 2; el plan completo continúa |
| Sin CI de build y pruebas (#20) | La regla de "no se fusiona con CI en rojo" no se cumple con código | Se hace en el Sprint 4 y después se exigen los checks en el ruleset |
| RustFS sigue en el compose | Servicio que ya no se usa (no hay adjuntos) | Se retira en el Sprint 4 |
| Fase 9 grande (ventas, caja, inventario y reportes) | Se desborda en 4 sprints | Ventas y caja primero; inventario y reportes son opcionales por plan y pueden pasar a un sprint adicional |

## 7. Cómo se mantiene

- Al **empezar** un sprint, el responsable de arquitectura crea los issues de sus filas, les asigna responsable y el campo *Sprint*.
- Al **cerrar** un sprint, lo no terminado pasa al siguiente y se anota el motivo en el issue.
- Cada **viernes** se revisa la vista *Por persona* del tablero; el plan se ajusta con una PR a este archivo, no de palabra.
- Un cambio de fecha de un hito se comunica en el PR que lo cambia.
