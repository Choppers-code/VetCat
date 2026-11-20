# FAC · Ventas, caja y tickets

Superficie: `WEB-CLI`. Actores: `recepcionista`, `clinica_admin`. El `veterinario` no cobra.

Todo el módulo se ofrece solo donde el plan de la clínica incluye `ventas`. Una clínica que ya lleva sus ventas en otro sistema lo mantiene apagado, y el resto de VetCat funciona igual.

VetCat **no es un punto de venta fiscal**: el ticket es un comprobante interno, el pago es solo en efectivo y no hay factura (CFDI). Estados del ticket: `BORRADOR`, `EMITIDO`, `PAGADO`, `CANCELADO`.

### FAC-001 · Mantener el catálogo de conceptos
- **EARS:** Donde el plan de la clínica incluya `ventas`, cuando el `clinica_admin` cree, edite o desactive un concepto (servicio o producto), el sistema deberá guardar su nombre, tipo y precio.
- **Actor:** `clinica_admin` · **Superficie:** `WEB-CLI` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - El precio es mayor o igual a cero y tiene dos decimales.
  - Cambiar un precio no altera tickets ya emitidos: cada línea guarda el precio con que se vendió.
  - Un concepto con ventas no se borra; se desactiva.

### FAC-002 · Crear un ticket
- **EARS:** Donde el plan de la clínica incluya `ventas`, cuando la `recepcionista` agregue uno o más conceptos con cantidad, el sistema deberá crear un ticket en `BORRADOR` con el subtotal de cada línea y el total.
- **Actor:** `recepcionista`, `clinica_admin` · **Superficie:** `WEB-CLI` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - El ticket puede ligarse a un propietario y a una consulta, o ser de mostrador sin ninguno (por ejemplo, solo la compra de un producto).
  - Si el ticket se liga a una consulta, la recepción ve los conceptos que el veterinario dejó anotados (VAC-006) sin ver nada clínico más.
  - Un `BORRADOR` se puede editar y descartar.
  - Los importes se calculan sin punto flotante (RNF-DAT-003).

### FAC-003 · Emitir un ticket con folio consecutivo
- **EARS:** Donde el plan de la clínica incluya `ventas`, cuando la `recepcionista` emita un ticket en `BORRADOR`, el sistema deberá asignarle el siguiente folio consecutivo de su clínica, sin huecos, y pasarlo a `EMITIDO`.
- **Actor:** `recepcionista`, `clinica_admin` · **Superficie:** `WEB-CLI` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Dos emisiones simultáneas reciben folios distintos y consecutivos.
  - Un ticket no se emite sin al menos una línea.
  - Un ticket emitido no se edita.
  - Si la emisión falla, el folio no se consume.

### FAC-004 · Registrar el pago en efectivo
- **EARS:** Donde el plan de la clínica incluya `ventas`, cuando la `recepcionista` registre el efectivo recibido de un ticket `EMITIDO`, el sistema deberá calcular el cambio, guardar el pago y pasar el ticket a `PAGADO`.
- **Actor:** `recepcionista`, `clinica_admin` · **Superficie:** `WEB-CLI` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - El efectivo recibido no puede ser menor al total.
  - El pago es total; no hay pagos parciales ni otros medios.
  - Si la clínica usa turno de caja, el pago se asocia al turno abierto.

### FAC-005 · Imprimir o descargar el ticket
- **EARS:** Donde el plan de la clínica incluya `ventas`, cuando el personal de caja elija imprimir un ticket `EMITIDO` o `PAGADO`, el sistema deberá generar un documento con los datos de la clínica, el folio, la fecha, las líneas, el total y, si existe, el pago.
- **Actor:** `recepcionista`, `clinica_admin` · **Superficie:** `WEB-CLI` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Lleva la leyenda "Comprobante interno, no es factura".
  - Incluye el nombre, la dirección, el teléfono y el logo de la clínica si lo tiene (CON-010).
  - Un ticket `CANCELADO` se imprime con la marca de cancelado.

### FAC-006 · Cancelar un ticket
- **EARS:** Donde el plan de la clínica incluya `ventas`, cuando el `clinica_admin` cancele un ticket `EMITIDO` o `PAGADO` e indique el motivo, el sistema deberá pasarlo a `CANCELADO` y conservar su folio.
- **Actor:** `clinica_admin` · **Superficie:** `WEB-CLI` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - El motivo es obligatorio y la cancelación queda en auditoría.
  - El folio cancelado no se reutiliza ni se renumera.
  - Si el ticket descontó inventario, lo devuelve (INV-005).
  - Una `recepcionista` no cancela; lo solicita al `clinica_admin`.

### FAC-007 · Rechazar la cancelación de un ticket de un turno cerrado
- **EARS:** Si se intenta cancelar un ticket que pertenece a un turno de caja ya cerrado, entonces el sistema deberá rechazar la cancelación e informar que el turno está cerrado.
- **Actor:** Sistema · **Superficie:** `WEB-CLI` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Un ticket de un turno abierto sí se puede cancelar.
  - Una clínica sin turno de caja nunca tiene tickets en un turno cerrado.

### FAC-008 · Consultar tickets
- **EARS:** Donde el plan de la clínica incluya `ventas`, cuando el personal de caja consulte los tickets, el sistema deberá listarlos por fecha, folio, estado o propietario.
- **Actor:** `recepcionista`, `clinica_admin` · **Superficie:** `WEB-CLI` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Se muestra el total del periodo filtrado, sin contar tickets cancelados.

### FAC-009 · Abrir un turno de caja
- **EARS:** Donde el plan de la clínica incluya `turno_caja`, cuando la `recepcionista` abra su turno con un fondo inicial, el sistema deberá registrarlo con la fecha, la hora y quien lo abrió.
- **Actor:** `recepcionista` · **Superficie:** `WEB-CLI` · **Prioridad:** Debería
- **Criterios de aceptación:**
  - Solo hay un turno abierto por recepcionista a la vez.
  - Mientras la función esté incluida, no se registran pagos sin un turno abierto.

### FAC-010 · Cerrar el turno con arqueo
- **EARS:** Donde el plan de la clínica incluya `turno_caja`, cuando la `recepcionista` cierre su turno e indique el efectivo contado, el sistema deberá calcular el efectivo esperado (fondo más pagos del turno) y mostrar la diferencia.
- **Actor:** `recepcionista` · **Superficie:** `WEB-CLI` · **Prioridad:** Debería
- **Criterios de aceptación:**
  - Un turno cerrado no se reabre.
  - La diferencia y quién cerró quedan registradas.
  - El `clinica_admin` consulta los turnos y sus diferencias.
