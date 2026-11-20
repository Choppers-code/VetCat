# Requerimientos de VetCat

Requerimientos funcionales (RF) y no funcionales (RNF) del sistema. Son la base del proyecto: la documentación técnica, los issues, las especificaciones y el código se derivan de ellos. **Si otro documento contradice a este directorio, gana este directorio** y el otro documento se corrige por PR.

## Índice

| Archivo | Módulo | Superficie principal |
|---|---|---|
| [01_CLI_clinicas_plataforma.md](01_CLI_clinicas_plataforma.md) | Clínicas y administración de plataforma | `WEB-PLA` |
| [02_PLA_planes.md](02_PLA_planes.md) | Planes y funciones habilitadas | `WEB-PLA`, `WEB-CLI` |
| [03_IDA_identidad_acceso.md](03_IDA_identidad_acceso.md) | Identidad y acceso | Todas |
| [04_CON_configuracion_clinica.md](04_CON_configuracion_clinica.md) | Configuración de la clínica | `WEB-CLI`, `APP` |
| [05_PRO_propietarios_mascotas.md](05_PRO_propietarios_mascotas.md) | Propietarios y mascotas | `WEB-CLI`, `APP` |
| [06_AGE_agenda.md](06_AGE_agenda.md) | Agenda de citas | `WEB-CLI`, `APP` |
| [07_EXP_expediente.md](07_EXP_expediente.md) | Expediente clínico | `APP` |
| [08_VAC_vacunas.md](08_VAC_vacunas.md) | Vacunas | `APP`, `WEB-CLI` |
| [09_POR_portal_cliente.md](09_POR_portal_cliente.md) | Portal del cliente | `APP` |
| [10_NOT_notificaciones.md](10_NOT_notificaciones.md) | Notificaciones | `APP` |
| [11_FAC_caja_tickets.md](11_FAC_caja_tickets.md) | Ventas, caja y tickets | `WEB-CLI` |
| [12_INV_inventario.md](12_INV_inventario.md) | Inventario | `WEB-CLI` |
| [13_REP_reportes.md](13_REP_reportes.md) | Reportes | `WEB-CLI` |
| [RNF_no_funcionales.md](RNF_no_funcionales.md) | Requerimientos no funcionales | Todas |
| [Matriz_funciones_por_rol.md](Matriz_funciones_por_rol.md) | Qué rol puede cada función | Todas |

## Cómo se escribe un RF

Cada RF es una sola frase en EARS (Easy Approach to Requirements Syntax), con un único "deberá".

| Patrón | Plantilla |
|---|---|
| Siempre | El sistema deberá … |
| Evento | Cuando <evento>, el sistema deberá … |
| Estado | Mientras <estado>, el sistema deberá … |
| No deseado | Si <situación>, entonces el sistema deberá … |
| Opcional | Donde <función del plan>, el sistema deberá … |

Cada RF lleva un ID `MÓDULO-NNN` que no se reutiliza ni se renumera, el actor, la superficie, la prioridad (*Debe*, *Debería*, *Podría*) y sus criterios de aceptación. Los criterios de aceptación son la base de la sección "Criterios de aceptación" de cada issue.

## Roles

Un usuario puede tener varios roles en una clínica y roles distintos en clínicas distintas.

| Rol | Quién es |
|---|---|
| `super_admin` | El equipo de VetCat. Solo ve datos administrativos de las clínicas, nunca datos clínicos ni de pacientes |
| `clinica_admin` | Dueño o gerente de una clínica |
| `veterinario` | Personal clínico |
| `recepcionista` | Personal de recepción y caja; sin acceso a datos clínicos |
| `cliente` | Dueño de mascotas; ve solo lo suyo |

## Superficies

| Código | Superficie | Roles |
|---|---|---|
| `APP` | App móvil Android única, con vista de veterinario y vista de cliente según el rol en la clínica activa | `veterinario`, `cliente` |
| `WEB-CLI` | Consola web de la clínica | `clinica_admin`, `recepcionista` |
| `WEB-PLA` | Consola web de la plataforma | `super_admin` |

Todas usan la misma API. La autorización se valida siempre en el backend.

## Funciones de plan

Cada clínica tiene un plan con vigencia. El plan activa o apaga funciones y fija límites, sobre un solo modelo de datos. Las claves son: `portal_cliente`, `recordatorios`, `ventas`, `turno_caja` (requiere `ventas`), `inventario` (requiere `ventas`), `visitas_domicilio` y `reportes`. El detalle está en [02_PLA_planes.md](02_PLA_planes.md).

## Términos

| Término | Significado |
|---|---|
| Clínica | Cliente de la plataforma; tiene su propio esquema de datos aislado |
| Propietario | Ficha de una persona en una clínica; puede estar vinculada o no a una cuenta `cliente` |
| Mascota | Paciente; pertenece a un solo propietario y no se transfiere |
| Cita | Reserva de horario. Estados: `PENDIENTE`, `CONFIRMADA`, `ATENDIDA`, `NO_ASISTIO`, `CANCELADA` |
| Consulta | Registro clínico de una atención; puede o no estar ligada a una cita |
| Concepto | Servicio o producto con precio que se puede cobrar |
| Ticket | Comprobante interno de cobro en efectivo, con folio consecutivo; no es factura fiscal |
| Turno de caja | Periodo entre la apertura y el cierre de caja de una recepcionista |
| Bloqueo | Periodo en que un veterinario o un consultorio no recibe citas |

## Fuera de alcance

iOS y app de escritorio · modo sin conexión · WhatsApp y SMS · factura fiscal (CFDI) y medios de pago distintos del efectivo · pagos en línea de los planes y datos bancarios · adjuntar fotografías o estudios a la consulta · multi-sede, API y webhooks para terceros, y marca blanca.
