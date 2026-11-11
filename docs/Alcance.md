# Alcance de VetCat

## Qué es

SaaS multi-clínica para veterinarias pequeñas: agenda de citas, expediente clínico, control de vacunas, portal del dueño y facturación. Una sola aplicación compartida por todas las clínicas, con los datos aislados por clínica.

## Principios

- **Mobile-first.** El cliente principal es la app móvil Flutter. Flutter Web queda como consola de administración, no como producto.
- **Android primero.** iOS entra cuando exista Mac, Xcode y cuenta de Apple Developer; no bloquea ninguna fase.
- **Proyecto completo, sin corte de MVP.** Se construye por fases hasta el alcance final; no hay una entrega que se declare "MVP y ya".
- **Multi-tenant híbrido.** Aplicación compartida (pool) y almacenamiento aislado por clínica mediante un esquema de PostgreSQL por tenant (silo).
- **El backend decide.** La autorización y el aislamiento se aplican en el servidor; ocultar pantallas en la app no es seguridad.

## Clientes

| Cliente | Para quién | Prioridad |
|---|---|---|
| App móvil Flutter (Android; iOS después) | Veterinario, recepcionista, administrador de clínica y dueño de mascota | Principal |
| Consola web Flutter Web | Administración de plataforma y de clínica desde el navegador | Secundaria |

La app de escritorio queda descartada.

## Incluido

| Módulo | Funcionalidad |
|---|---|
| Clínicas y multi-tenancy | Alta de clínica, aprovisionamiento de esquema, configuración, usuarios y roles |
| Agenda | Citas, disponibilidad, consultorios, horarios, bloqueos, anti-empalme |
| Propietarios y mascotas | Registro, edición y consulta |
| Expediente clínico | Consultas, diagnósticos, tratamientos, recetas, notas y adjuntos |
| Vacunas | Catálogo, aplicaciones, próxima dosis, vencidas y próximas |
| Portal del dueño | Sus mascotas, citas, cartilla y resumen clínico autorizado |
| Facturación y caja | Catálogo de conceptos, venta en mostrador, pagos, comprobante, folio por clínica |
| Administración de plataforma | Alta y baja de clínicas, soporte, monitoreo |

## Fuera de alcance

| Excluido | Motivo |
|---|---|
| App de escritorio | Descartada |
| App iOS en el primer alcance | Requiere Mac, Xcode y cuenta de Apple Developer; se incorpora después |
| Inventario | Existencias, lotes, caducidad y proveedores van en fase posterior. El catálogo de conceptos se diseña para engancharlo sin migrar datos |
| Notificaciones WhatsApp/SMS | Las alertas de vacunas son dentro de la aplicación |
| Facturación fiscal (CFDI o equivalente) | Fuera por ahora: v1.2 emite comprobante interno; la integración fiscal sería una fase aparte |

## Decisiones de alcance

- **Portal del dueño: dentro.** Es una fase con entrega propia (v1.1) y vive en la misma app, con rol `dueno`.
- **Facturación: dentro**, como fase propia (v1.2), después del portal.
- **POS: dentro de la fase de facturación.** Se entiende como catálogo de conceptos + cobro en mostrador + comprobante. El control de existencias (inventario) no entra.
- **Escritorio: fuera.**

## Modelo de despliegue

- **Primer entorno: la máquina del desarrollador**, con todo el stack en Docker Compose y una clínica de prueba.
- **Destino: nube.** El mismo stack con otra configuración; subir no debe implicar cambios de código.
- **Fuera por ahora:** servidor instalado en la clínica, alta disponibilidad y multi-región.

El detalle de entornos, migraciones, respaldos y checklist de subida está en [Despliegue_y_operacion.md](Despliegue_y_operacion.md).

## Pendiente de confirmar

1. Si se agregan notificaciones push.
2. Si se soporta uso sin conexión en la app móvil (la recomendación es que no en esta entrega).
