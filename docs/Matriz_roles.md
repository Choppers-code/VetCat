# Matriz de roles por acción

Qué puede hacer cada rol, acción por acción. `Decisiones_tecnicas.md` §4 la exige antes del primer `@PreAuthorize`, y esta matriz es la que se implementa: el código no añade permisos que no estén aquí. La referencia exacta de qué rol puede qué función, con los IDs de requerimiento, es [`requerimientos/Matriz_funciones_por_rol.md`](requerimientos/Matriz_funciones_por_rol.md).

**Cómo se lee.** ✓ permite · — no permite · ◐ permite con la condición escrita bajo la tabla. En las columnas, SA = `super_admin`, AC = `clinica_admin`, V = `veterinario`, R = `recepcionista`, C = `cliente`.

**Cómo se cambia.** Un rol nuevo o una acción nueva entra por PR a este archivo, revisado por el área 1. Nunca por un `@PreAuthorize` que alguien añade en su módulo.

## Clínica y usuarios

| Acción | SA | AC | V | R | C |
|---|---|---|---|---|---|
| Alta de una clínica | ✓ | — | — | — | — |
| Baja o suspensión de una clínica | ✓ | — | — | — | — |
| Configuración de la clínica y zona horaria | — | ✓ | — | — | — |
| Identidad de la clínica y logo | — | ✓ | — | — | — |
| Consultorios y horarios de atención | — | ✓ | — | — | — |
| Crear, editar y desactivar personal | — | ✓ | — | — | — |
| Invitar a un cliente y generar su código de vinculación | — | ✓ | — | ✓ | — |
| Ver el plan y el uso propios | — | ✓ | — | — | — |
| Ver la auditoría de la clínica | — | ✓ | — | — | — |
| Consultar la auditoría de la plataforma | ✓ | — | — | — | — |
| Monitoreo de la plataforma | ✓ | — | — | — | — |

Un usuario puede tener **varios roles en una misma clínica**. `AC` no puede crear usuarios `super_admin` ni `cliente`, ni quitarse el último rol `clinica_admin`, ni darse permisos que no tiene: la asignación de roles la valida el backend contra esta matriz, no contra la petición.

## Propietarios y mascotas

| Acción | SA | AC | V | R | C |
|---|---|---|---|---|---|
| Registrar propietario y mascota | — | ✓ | ✓ | ✓ | — |
| Editar sus datos | — | ✓ | ✓ | ✓ | — |
| Consultar y buscar | — | ✓ | ✓ | ✓ | ◐ |
| Dirección del propietario | — | ◐ | ◐ | ◐ | — |
| Desactivar (nunca borrar) | — | ✓ | — | — | — |

◐ `C` consulta solo los propietarios y mascotas de los que es propietario, y el backend comprueba esa propiedad en **cada** consulta, no en la primera. La dirección del propietario solo existe si el plan de la clínica incluye `visitas_domicilio`.

## Agenda

| Acción | SA | AC | V | R | C |
|---|---|---|---|---|---|
| Ver la agenda de toda la clínica | — | ✓ | ◐ | ✓ | — |
| Ver agenda propia | — | ✓ | ✓ | ✓ | — |
| Crear cita | — | ✓ | ◐ | ✓ | ◐ |
| Confirmar cita | — | — | ◐ | — | — |
| Reprogramar | — | ✓ | ◐ | ✓ | — |
| Cancelar | — | ✓ | ◐ | ✓ | — |
| Solicitar la cancelación de una cita | — | — | — | — | ◐ |
| Aprobar o rechazar una solicitud de cancelación | — | — | ◐ | — | — |
| Marcar atendida o no asistió | — | — | ◐ | — | — |
| Bloquear agenda | — | ✓ | ◐ | — | — |
| Ver sus citas y descargar el comprobante de cita | — | — | — | — | ◐ |

◐ `V` ve la agenda completa en lectura, pero solo crea, reprograma, cancela y bloquea sobre su propia agenda, y solo confirma, atiende, marca no asistió o resuelve solicitudes de las citas donde es el veterinario asignado. `C` **solicita** una cita (queda `PENDIENTE` hasta que el veterinario la confirma) solo para mascotas propias y con un máximo de 3 pendientes; solicita cancelar solo con la anticipación configurada. Todo lo de `C` requiere que el plan incluya `portal_cliente`.

## Expediente clínico

| Acción | SA | AC | V | R | C |
|---|---|---|---|---|---|
| Leer las consultas de un paciente | — | ✓ | ✓ | — | — |
| Registrar una consulta | — | — | ✓ | — | — |
| Corregir (genera una versión nueva) | — | — | ✓ | — | — |
| Descargar la receta en PDF | — | — | ✓ | — | ◐ |
| Ver el resumen autorizado | — | — | ✓ | — | ◐ |
| Ver la auditoría de lectura | — | ✓ | — | — | — |

◐ `C` ve solo los campos explícitamente permitidos del resumen (fecha, motivo, diagnóstico, tratamiento y receta) de la última versión de las consultas cerradas, nunca la consulta completa, y descarga solo las recetas de sus mascotas. La lectura del expediente queda en auditoría (`Decisiones_tecnicas.md` §6), también la del propio cliente. `R` y `SA` no acceden a nada de esta sección.

## Vacunas

| Acción | SA | AC | V | R | C |
|---|---|---|---|---|---|
| Mantener el catálogo de la clínica | — | ✓ | ✓ | — | — |
| Consultar el catálogo | — | ✓ | ✓ | ✓ | — |
| Registrar una aplicación | — | — | ✓ | — | — |
| Ver la cartilla | — | ✓ | ✓ | — | ◐ |
| Ver vencidas y próximas | — | ✓ | ✓ | — | — |

◐ `C` ve la cartilla de sus mascotas. `R` consulta el catálogo (nombre, especie y existencia si hay inventario) pero **nunca** aplicaciones, cartilla ni próximas dosis: son datos clínicos.

## Ventas, caja y tickets

Esta sección, la de inventario y la de reportes solo existen si el plan de la clínica incluye `ventas`, `turno_caja`, `inventario` o `reportes`.

| Acción | SA | AC | V | R | C |
|---|---|---|---|---|---|
| Mantener el catálogo de conceptos | — | ✓ | — | — | — |
| Cobrar en mostrador | — | ✓ | — | ✓ | — |
| Emitir el ticket con folio | — | ✓ | — | ✓ | — |
| Cancelar un ticket | — | ◐ | — | — | — |
| Abrir y cerrar turno de caja | — | — | — | ✓ | — |
| Ver el arqueo | — | ◐ | — | ✓ | — |

◐ `AC` cancela un ticket con motivo y auditoría, y **nunca** uno que pertenezca a un turno de caja ya cerrado. `AC` consulta los turnos y sus diferencias, pero no abre ni cierra turnos. `V` no cobra en esta versión de la matriz: si en la clínica real el veterinario cobra, se cambia la fila aquí; no se resuelve con un `if` en `facturacion/`.

## Inventario

| Acción | SA | AC | V | R | C |
|---|---|---|---|---|---|
| Productos, ajustes, alertas e historial | — | ✓ | — | — | — |
| Registrar entradas de stock | — | ✓ | — | ✓ | — |

## Reportes

| Acción | SA | AC | V | R | C |
|---|---|---|---|---|---|
| Consultar reportes | — | ✓ | — | — | — |

## Plataforma

| Acción | SA | AC | V | R | C |
|---|---|---|---|---|---|
| Ver el estado y las métricas del servicio | ✓ | — | — | — | — |
| Aprovisionar, editar datos administrativos, suspender, reactivar y dar de baja una clínica | ✓ | — | — | — | — |
| Crear al administrador de una clínica | ✓ | — | — | — | — |
| Mantener los planes y asignar un plan a una clínica | ✓ | — | — | — | — |
| Registrar el pago y la vigencia de un plan | ✓ | — | — | — | — |
| Ver datos clínicos de una clínica | — | — | — | — | — |

`SA` trabaja con el token de identidad, sin `tenant_id` (ADR-0003): no tiene fila en `usuario_clinica` y no puede resolver el esquema de una clínica ni por error de configuración. La baja de una clínica conserva sus datos y no se revierte desde la consola.

## Notificaciones

| Acción | SA | AC | V | R | C |
|---|---|---|---|---|---|
| Recibir y ver la bandeja | — | — | ✓ | — | ✓ |
| Registrar el dispositivo para push | — | — | ✓ | — | ✓ |

## Reglas que ninguna tabla relaja

1. **El backend decide.** Ocultar una pantalla en la app no autoriza nada; cada fila se aplica con `@PreAuthorize` en el servicio.
2. **`SA` no toca datos clínicos**, ni con motivo de soporte. El soporte actúa sobre configuración, metadatos y disponibilidad.
3. **`C` se re-verifica en cada consulta.** La propiedad de la mascota se comprueba contra el propietario del token, no contra un identificador del cuerpo.
4. **Un rol no viaja entre clínicas.** Si un usuario tiene varios roles en una clínica, su permiso es la unión de las filas de sus roles en esa clínica (ADR-0003 y ADR-0006).
5. **`SA` es el único rol que viene del realm**, porque no es un rol por clínica.
6. **Las funciones del plan se verifican en el servidor en cada petición:** una función que el plan no incluye responde con error aunque el rol la permita.

## Cómo se comprueba

Cada fila con ✓ necesita una prueba de permitido y cada fila con — una de denegado: sin JWT, 401; con el rol equivocado, 403. Las pruebas de rol viven en el módulo del dueño y la de aislamiento entre clínicas es transversal (área 5), según `Equipo_responsabilidades.md` §Fronteras.
