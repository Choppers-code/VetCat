# Sprints 20 a 31 · Fases 8 a 11

Parte de [`Plan_de_sprints.md`](../Plan_de_sprints.md). Entrega el portal del cliente y las notificaciones (v1.1), las ventas, la caja y el inventario (v1.2), la consola de plataforma (v1.3) y el endurecimiento hasta la publicación (v2.0).

**Cómo leer las tablas.** *Req.* es el módulo de `docs/requerimientos/`. *Depende de* son filas o issues que deben estar fusionados antes. Un `#N` ya existe como issue; el resto se crea al empezar el sprint. Donde dice "(aprueba X)", escribe una persona y aprueba el dueño del módulo, según [`Equipo_responsabilidades.md`](../Equipo_responsabilidades.md).

## Sprint 20 · 14 – 20 mar 2027 · Fase 8

| Issue | Responsable | Req. | Depende de |
|---|---|---|---|
| Portal del cliente (backend): sus mascotas, citas, cartilla y resumen autorizado | `Raul-Rolu` | POR | H3 |
| Bandeja de notificaciones por usuario y clínica, con retención de 90 días | `ToNyyF1` | NOT | H3 |
| Registro y baja de los tokens de dispositivo al abrir y cerrar sesión | `ToNyyF1` | NOT | Bandeja |
| Trabajos programados que reciben el tenant de forma explícita | `Anton-code-sp` | NOT, PLA | Resolver de tenant |
| Proyecto de Firebase y credenciales como secreto fuera del repositorio | `Carlosss1819` | NOT | — |
| Vista del cliente en la app: inicio, mascotas y citas | `Nacho-Nacho3` | POR | Portal del cliente |

## Sprint 21 · 21 – 27 mar 2027 · Fase 8 (cierre) · H4

| Issue | Responsable | Req. | Depende de |
|---|---|---|---|
| Envío por push con Firebase Cloud Messaging detrás de un adaptador; sin datos clínicos en el texto | `ToNyyF1` | NOT | Credenciales, bandeja |
| Recordatorios de cita (función de plan `recordatorios`) | `ToNyyF1` | NOT, PLA | Trabajos programados |
| Descargar la receta en PDF de sus mascotas, para el `cliente` | `Raul-Rolu` | EXP, POR | Receta |
| Solicitar cita y solicitar cancelación desde la app del cliente | `Nacho-Nacho3` | AGE, POR | Solicitudes de cita |
| Bandeja de notificaciones y registro del dispositivo en la app | `Nacho-Nacho3` | NOT | Bandeja, tokens |
| Pruebas: propiedad verificada, push sin datos clínicos y bandeja ante fallo del push | `Carlosss1819` | POR, NOT | Envío por push |

**H4 (v1.1):** el cliente consulta sus mascotas, citas y vacunas, solicita citas y recibe notificaciones.

## Sprint 22 · 28 mar – 3 abr 2027 · Fase 9

| Issue | Responsable | Req. | Depende de |
|---|---|---|---|
| Catálogo de conceptos con precio | `ToNyyF1` | FAC | H4 |
| Ticket: `BORRADOR`, `EMITIDO` y `PAGADO`, con folio consecutivo sin huecos por clínica | `ToNyyF1` | FAC | Catálogo de conceptos |
| Dependencias entre funciones de plan: `turno_caja` e `inventario` requieren `ventas` | `Anton-code-sp` | PLA | Verificación de funciones |
| Pantalla de venta en mostrador (aprueba `ToNyyF1`) | `Nacho-Nacho3` | FAC | Ticket |
| Prueba del folio sin huecos con emisiones concurrentes | `Carlosss1819` | FAC | Ticket |

## Sprint 23 · 4 – 10 abr 2027 · Fase 9

| Issue | Responsable | Req. | Depende de |
|---|---|---|---|
| Cobro en efectivo y cancelación de ticket solo por `clinica_admin`, con motivo, auditoría y folio conservado | `ToNyyF1` | FAC | Ticket |
| Turno de caja: apertura, cierre y arqueo | `ToNyyF1` | FAC | Cobro |
| Auditoría de la cancelación de tickets | `Anton-code-sp` | FAC, RNF | Cancelación |
| Pantallas de caja y de tickets (aprueba `ToNyyF1`) | `Nacho-Nacho3` | FAC | Turno de caja |
| Pruebas: no se cancela un ticket de un turno cerrado; el dinero no usa punto flotante | `Carlosss1819` | FAC | Cancelación |

## Sprint 24 · 11 – 17 abr 2027 · Fase 9

| Issue | Responsable | Req. | Depende de |
|---|---|---|---|
| Productos controlados y entradas de stock con fecha de caducidad (aprueba `ToNyyF1`) | `Raul-Rolu` | INV | Función `inventario` |
| Descontar stock al emitir el ticket, primero lo que caduca antes; nunca negativo; devolver al cancelar (aprueba `ToNyyF1`) | `Raul-Rolu` | INV, FAC | Entradas de stock |
| Pantallas de inventario (aprueba `Raul-Rolu`) | `Nacho-Nacho3` | INV | Productos |
| Pruebas de concurrencia sobre el stock | `Carlosss1819` | INV | Descuento de stock |

## Sprint 25 · 18 – 24 abr 2027 · Fase 9 (cierre) · H5

| Issue | Responsable | Req. | Depende de |
|---|---|---|---|
| Alertas de stock bajo y de caducidad a 30 días, e historial (aprueba `ToNyyF1`) | `Raul-Rolu` | INV | Entradas de stock |
| Reportes de solo lectura: conteos y totales por rango de fechas (aprueba `ToNyyF1`) | `Raul-Rolu` | REP | Tickets, citas |
| Pantallas de reportes y alertas de inventario | `Nacho-Nacho3` | REP, INV | Reportes |
| Si el plan no incluye `inventario`, los productos son solo conceptos con precio | `ToNyyF1` | INV, PLA | Función `inventario` |
| Corte `release/1.2.0` y pruebas de aceptación de caja e inventario | `Carlosss1819` | FAC, INV, REP | Todo lo anterior |

**H5 (v1.2):** una clínica cobra una consulta con ticket y controla su inventario si su plan lo incluye.

## Sprint 26 · 25 abr – 1 may 2027 · Fase 10

| Issue | Responsable | Req. | Depende de |
|---|---|---|---|
| Alta de una clínica desde la plataforma: aprovisionar y crear a su administrador | `Anton-code-sp` | CLI | Aprovisionamiento |
| Mantener los planes y asignar un plan a una clínica | `Anton-code-sp` | PLA, CLI | Modelo de planes |
| Consola de la plataforma: lista de clínicas y alta | `Nacho-Nacho3` | CLI | Alta de clínica |
| Pruebas: `super_admin` no accede a datos clínicos ni de pacientes | `Carlosss1819` | CLI, RNF | Alta de clínica |

## Sprint 27 · 2 – 8 may 2027 · Fase 10

| Issue | Responsable | Req. | Depende de |
|---|---|---|---|
| Editar datos administrativos, suspender, reactivar y dar de baja una clínica | `Anton-code-sp` | CLI | Alta de clínica |
| Registrar el pago y la vigencia de un plan, de forma manual | `Raul-Rolu` | PLA | Planes asignados |
| Ver el plan y el uso propios, para `clinica_admin` (backend) | `ToNyyF1` | PLA | Verificación de funciones |
| Consola de la plataforma: planes, pagos y vigencia | `Nacho-Nacho3` | PLA | Pagos y vigencia |
| Pruebas del ciclo de vida de una clínica, incluida la suspensión automática | `Carlosss1819` | CLI, PLA | Suspensión |

## Sprint 28 · 9 – 15 may 2027 · Fase 10 (cierre)

| Issue | Responsable | Req. | Depende de |
|---|---|---|---|
| Consultar la auditoría de la plataforma, para `super_admin` | `Anton-code-sp` | CLI | Auditoría de la plataforma |
| Consola de la clínica: mi plan, personal y configuración | `Nacho-Nacho3` | CON, PLA | Plan propio |
| Reserva para correcciones de las fases 4 a 9 | `Raul-Rolu`, `ToNyyF1` | — | — |
| Corte `release/1.3.0` y pruebas de aceptación de la consola | `Carlosss1819` | CLI, CON | Todo lo anterior |

## Sprint 29 · 16 – 22 may 2027 · Fase 11

| Issue | Responsable | Req. | Depende de |
|---|---|---|---|
| Respaldos: dump por esquema, base backup con WAL y restauración de una clínica | `Carlosss1819` | RNF | — |
| Compose de nube: Keycloak con `start`, `KC_HOSTNAME` y TLS | `Carlosss1819` | RNF | — |
| Revisión de seguridad transversal y corrección de hallazgos | `Anton-code-sp` | RNF | — |
| Revisión de la app y las consolas contra los requerimientos no funcionales | `Nacho-Nacho3` | RNF | — |

## Sprint 30 · 23 – 29 may 2027 · Fase 11

| Issue | Responsable | Req. | Depende de |
|---|---|---|---|
| Firma del APK y ficha de la aplicación en Play Store | `Carlosss1819` | RNF | Respaldos |
| Despliegue reproducible en nube: el mismo stack con otra configuración | `Carlosss1819` | RNF | Compose de nube |
| Lista de verificación para subir a nube, con la suspensión automática probada | `Anton-code-sp` | PLA, RNF | Despliegue |
| Compilación de release de la app, íconos y capturas de la ficha | `Nacho-Nacho3` | RNF | Firma del APK |
| Manual de instalación y operación | `Raul-Rolu` | RNF | Despliegue |

## Sprint 31 · 30 may – 5 jun 2027 · Fase 11 (cierre) · H6

| Issue | Responsable | Req. | Depende de |
|---|---|---|---|
| Pruebas de aceptación finales sobre el entorno de nube | `Carlosss1819` | todos | Despliegue |
| Corte `release/2.0.0` y publicación en Play Store | `Carlosss1819` | RNF | Pruebas de aceptación |
| Correcciones de última hora y congelamiento | `Raul-Rolu`, `ToNyyF1`, `Anton-code-sp` | — | Pruebas de aceptación |

**H6 (v2.0):** publicación en Play Store, respaldos y despliegue reproducible.
