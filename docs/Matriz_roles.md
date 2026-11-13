# Matriz de roles por acción

Qué puede hacer cada rol, acción por acción. `Decisiones_tecnicas.md` §4 la exige antes del primer `@PreAuthorize`, y esta matriz es la que se implementa: el código no añade permisos que no estén aquí.

**Cómo se lee.** ✓ permite · — no permite · ◐ permite con la condición escrita bajo la tabla. En las columnas, AP = `administrador_plataforma`, AC = `administrador_clinica`, V = `veterinario`, R = `recepcionista`, D = `dueno`.

**Cómo se cambia.** Un rol nuevo o una acción nueva entra por PR a este archivo, revisado por el área 1. Nunca por un `@PreAuthorize` que alguien añade en su módulo.

## Clínica y usuarios

| Acción | AP | AC | V | R | D |
|---|---|---|---|---|---|
| Alta de una clínica | ✓ | — | — | — | — |
| Baja o suspensión de una clínica | ✓ | — | — | — | — |
| Configuración de la clínica y zona horaria | — | ✓ | — | — | — |
| Consultorios y horarios de atención | — | ✓ | — | — | — |
| Invitar y dar de baja usuarios | — | ✓ | — | — | — |
| Asignar el rol de un usuario en la clínica | — | ✓ | — | — | — |
| Ver la auditoría de la clínica | — | ✓ | — | — | — |
| Monitoreo de la plataforma | ✓ | — | — | — | — |

`AC` no puede cambiar su propio rol ni darse permisos que no tiene: la asignación de roles la valida el backend contra esta matriz, no contra la petición.

## Propietarios y mascotas

| Acción | AP | AC | V | R | D |
|---|---|---|---|---|---|
| Registrar propietario y mascota | — | ✓ | ✓ | ✓ | — |
| Editar sus datos | — | ✓ | ✓ | ✓ | — |
| Consultar | — | ✓ | ✓ | ✓ | ◐ |
| Desactivar (nunca borrar) | — | ✓ | — | — | — |

◐ `D` consulta solo los propietarios y mascotas de los que es dueño, y el backend comprueba esa propiedad en **cada** consulta, no en la primera.

## Agenda

| Acción | AP | AC | V | R | D |
|---|---|---|---|---|---|
| Ver la agenda de toda la clínica | — | ✓ | ◐ | ✓ | — |
| Ver agenda propia | — | ✓ | ✓ | ✓ | — |
| Crear cita | — | ✓ | ✓ | ✓ | — |
| Reprogramar | — | ✓ | ✓ | ✓ | — |
| Cancelar | — | ✓ | ✓ | ✓ | — |
| Marcar atendida o no asistió | — | — | ✓ | — | — |
| Bloquear agenda | — | ✓ | ✓ | — | — |
| Ver sus citas | — | — | — | — | ◐ |

◐ `V` ve la agenda completa en lectura, pero solo crea y reprograma sobre su propia columna. `D` ve las citas de sus mascotas, con propiedad verificada.

## Expediente clínico

| Acción | AP | AC | V | R | D |
|---|---|---|---|---|---|
| Leer las consultas de un paciente | — | ✓ | ✓ | — | — |
| Registrar una consulta | — | — | ✓ | — | — |
| Corregir: consulta nueva que sustituye | — | — | ✓ | — | — |
| Adjuntar archivos a una consulta | — | — | ✓ | — | — |
| Ver el resumen autorizado | — | — | ✓ | — | ◐ |
| Ver la auditoría de lectura | — | ✓ | — | — | — |

◐ `D` ve solo los campos explícitamente permitidos del resumen, nunca la consulta completa. La lectura del expediente queda en auditoría (`Decisiones_tecnicas.md` §6), también la del propio dueño.

## Vacunas

| Acción | AP | AC | V | R | D |
|---|---|---|---|---|---|
| Mantener el catálogo global | ✓ | — | — | — | — |
| Override de una vacuna en la clínica | — | ✓ | ✓ | — | — |
| Registrar una aplicación | — | — | ✓ | — | — |
| Ver la cartilla | — | ✓ | ✓ | — | ◐ |
| Ver vencidas y próximas | — | ✓ | ✓ | — | — |

◐ `D` ve la cartilla de sus mascotas. **Hueco detectado:** `Decisiones_tecnicas.md` §4 dice que `recepcionista` no accede a datos clínicos, y la próxima dosis es un dato clínico. Si recepción necesita saber la próxima dosis para agendar el recordatorio, eso es una fila que se añade aquí de forma explícita, no una consulta que se cuela en el módulo.

## Facturación y caja

| Acción | AP | AC | V | R | D |
|---|---|---|---|---|---|
| Mantener el catálogo de conceptos | — | ✓ | — | — | — |
| Cobrar en mostrador | — | ✓ | — | ✓ | — |
| Emitir el comprobante con folio | — | ✓ | — | ✓ | — |
| Cancelar una factura | — | ✓ | — | — | — |
| Abrir y cerrar turno de caja | — | ✓ | — | ✓ | — |
| Ver el arqueo | — | ✓ | — | — | — |

`V` no cobra en esta versión de la matriz. Si en la clínica real el veterinario cobra, se cambia la fila aquí; no se resuelve con un `if` en `facturacion/`.

## Plataforma

| Acción | AP | AC | V | R | D |
|---|---|---|---|---|---|
| Ver el estado y las métricas del servicio | ✓ | — | — | — | — |
| Aprovisionar, suspender y restaurar una clínica | ✓ | — | — | — | — |
| Ver datos clínicos de una clínica | — | — | — | — | — |

`AP` trabaja con el token de identidad, sin `tenant_id` (ADR-0003): no tiene fila en `usuario_clinica` y no puede resolver el esquema de una clínica ni por error de configuración.

## Reglas que ninguna tabla relaja

1. **El backend decide.** Ocultar una pantalla en la app no autoriza nada; cada fila se aplica con `@PreAuthorize` en el servicio.
2. **`AP` no toca datos clínicos**, ni con motivo de soporte. El soporte actúa sobre configuración, metadatos y disponibilidad.
3. **`D` se re-verifica en cada consulta.** La propiedad de la mascota se comprueba contra el propietario del token, no contra un identificador del cuerpo.
4. **Un rol no viaja entre clínicas.** El rol efectivo sale de `usuario_clinica` para la clínica del token (ADR-0003).
5. **`AP` es el único rol que viene del realm**, porque no es un rol por clínica.

## Cómo se comprueba

Cada fila con ✓ necesita una prueba de permitido y cada fila con — una de denegado: sin JWT, 401; con el rol equivocado, 403. Las pruebas de rol viven en el módulo del dueño y la de aislamiento entre clínicas es transversal (área 5), según `Equipo_responsabilidades.md` §Fronteras.
