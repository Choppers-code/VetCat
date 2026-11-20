# Alcance de VetCat

## Qué es

SaaS multi-clínica para veterinarias pequeñas: agenda de citas, expediente clínico, control de vacunas, portal del cliente, ventas y caja, e inventario simple. Una sola aplicación compartida por todas las clínicas, con datos aislados por clínica y funciones según el plan de cada clínica.

## Principios

- **Mobile-first.** La app móvil Flutter es el cliente de `veterinario` y `cliente`; `clinica_admin`, `recepcionista` y `super_admin` usan la consola web. Flutter Web es una consola, no el producto principal.
- **Android primero.** iOS entra cuando exista Mac, Xcode y cuenta de Apple Developer; no bloquea ninguna fase.
- **Proyecto completo, sin corte de MVP.** Se construye por fases hasta el alcance final; no hay una entrega que se declare "MVP y ya".
- **Multi-tenant híbrido.** Aplicación compartida (pool) y almacenamiento aislado por clínica mediante un esquema de PostgreSQL por tenant (silo).
- **Funciones por plan.** Cada clínica tiene un plan con vigencia que activa funciones y fija límites, sobre un único modelo de datos.
- **El backend decide.** La autorización y el aislamiento se aplican en el servidor; ocultar pantallas en la app no es seguridad.

## Clientes

| Cliente | Para quién | Prioridad |
|---|---|---|
| App móvil Flutter única (Android; iOS después) | `veterinario` y `cliente`, con una vista por rol según la clínica activa | Principal |
| Consola web Flutter Web, con dos áreas | Área de la clínica: `clinica_admin` y `recepcionista` (incluida la caja). Área de la plataforma: `super_admin` | Secundaria |

La app de escritorio queda descartada. La app móvil se podría partir en dos más adelante, una por rol; por eso las vistas viven en carpetas separadas.

## Incluido

| Módulo | Funcionalidad |
|---|---|
| Clínicas, planes y funciones | Alta de clínica, plan, vigencia, suspensión y funciones habilitadas por plan |
| Identidad y acceso | Varios roles por usuario, varias clínicas, recuperar contraseña |
| Configuración de la clínica | Personal, consultorios, horarios, bloqueos y logo |
| Agenda | Citas, disponibilidad, anti-empalme, `NO_ASISTIO`, solicitud de cita y de cancelación por el cliente, confirmación por el veterinario y citas a domicilio según el plan |
| Propietarios y mascotas | Registro, edición y consulta |
| Expediente clínico | Consultas, diagnósticos, tratamientos, notas y receta en PDF, con versiones; sin adjuntos |
| Vacunas | Catálogo por clínica, aplicaciones, próxima dosis, vencidas y próximas |
| Portal del cliente | Sus mascotas, citas, cartilla y resumen clínico autorizado |
| Notificaciones | Bandeja dentro de la aplicación y notificaciones push |
| Ventas, caja y tickets | Opcional por plan, solo efectivo: catálogo de conceptos, venta en mostrador, ticket con folio por clínica y turno de caja con apertura, cierre y arqueo |
| Inventario | Stock, caducidades y alertas; opcional por plan |
| Reportes | Conteos y totales por rango de fechas, para `clinica_admin` |
| Administración de plataforma | Alta y baja de clínicas, planes, pagos y vigencia, soporte y monitoreo |

## Fuera de alcance

| Excluido | Motivo |
|---|---|
| App de escritorio | Descartada |
| App iOS en el primer alcance | Requiere Mac, Xcode y cuenta de Apple Developer; se incorpora después |
| Multi-sede | Cada clínica es una sola sede |
| API y webhooks para terceros | Sin integraciones externas en este alcance |
| Marca blanca | Una sola marca para toda la plataforma |
| Pagos en línea de los planes y datos bancarios | El registro de pagos de los planes es manual |
| Adjuntar fotografías o estudios a la consulta | El análisis es presencial |
| Medios de pago distintos del efectivo | El ticket registra solo efectivo |
| Notificaciones WhatsApp/SMS | Las alertas van dentro de la aplicación y por push, que **sí** está dentro |
| Uso sin conexión | No en esta entrega: solo caché de lectura y reintento (`Decisiones_tecnicas.md` §9). Si se retoma, es una fase propia |
| Facturación fiscal (CFDI o equivalente) | Fuera por ahora: v1.2 emite un ticket como comprobante interno; la integración fiscal sería una fase aparte |

## Decisiones de alcance

- **Portal del cliente: dentro.** Es una fase con entrega propia (v1.1) y vive en la misma app, con rol `cliente`.
- **Facturación: dentro**, como fase propia (v1.2), después del portal.
- **POS: dentro de la fase de facturación.** Se entiende como catálogo de conceptos + cobro en mostrador + ticket. El control de existencias entra de forma simple (stock, caducidad, alertas) y es opcional por plan.
- **Planes: dentro**, asignados y renovados de forma manual por `super_admin`.
- **Push: dentro.**
- **Escritorio: fuera.**

## Modelo de despliegue

- **Primer entorno: la máquina del desarrollador**, con todo el stack en Docker Compose y una clínica de prueba.
- **Destino: nube.** El mismo stack con otra configuración; subir no debe implicar cambios de código.
- **Fuera por ahora:** servidor instalado en la clínica, alta disponibilidad y multi-región.

El detalle de entornos, migraciones, respaldos y checklist de subida está en [Despliegue_y_operacion.md](Despliegue_y_operacion.md).

## Pendiente de confirmar

Ninguno por ahora.
