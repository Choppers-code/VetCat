# INV · Inventario

Superficie: `WEB-CLI`. Actores: `clinica_admin`, `recepcionista`.

El inventario es **opcional por clínica** y depende de `ventas`. Resuelve el caso de la clínica que ya lleva su inventario en otro sistema y no quiere capturar todo dos veces:

- Con `inventario` **apagado**, los productos del catálogo (FAC-001) son solo conceptos con precio: se venden, pero no tienen stock ni caducidad. No hay doble captura.
- Con `inventario` **encendido**, el stock se suma al registrar entradas y se resta solo al emitir un ticket. Nadie descuenta a mano. Es un control simple de existencias, caducidades y alertas, no un sistema de compras.

Una clínica puede empezar sin inventario y encenderlo después: al hacerlo, registra el stock inicial de cada producto.

### INV-001 · Marcar un producto como controlado
- **EARS:** Donde el plan de la clínica incluya `inventario`, cuando el `clinica_admin` marque un producto como controlado, el sistema deberá guardar su unidad, su stock mínimo y si tiene caducidad.
- **Actor:** `clinica_admin` · **Superficie:** `WEB-CLI` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Un producto no controlado sigue siendo vendible, sin stock.
  - Los servicios no llevan stock.
  - Un producto sin caducidad (por ejemplo, una correa) no pide fecha de caducidad en sus entradas.

### INV-002 · Registrar una entrada de stock
- **EARS:** Donde el plan de la clínica incluya `inventario`, cuando el `clinica_admin` o la `recepcionista` registren la entrada de un producto controlado con su cantidad, el sistema deberá sumarla al stock y guardar el movimiento.
- **Actor:** `clinica_admin`, `recepcionista` · **Superficie:** `WEB-CLI` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - La cantidad es un entero mayor que cero.
  - Si el producto tiene caducidad, la fecha de caducidad de la entrada es obligatoria y no puede ser pasada.
  - Cada entrada conserva su propia fecha de caducidad y su cantidad restante.
  - El movimiento guarda fecha, autor y cantidad.

### INV-003 · Descontar stock al emitir un ticket
- **EARS:** Donde el plan de la clínica incluya `inventario`, cuando se emita un ticket con productos controlados, el sistema deberá restar de cada producto la cantidad vendida, empezando por las entradas que caducan antes, y guardar el movimiento.
- **Actor:** Sistema · **Superficie:** `WEB-CLI` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Incluye las vacunas ligadas a un producto (VAC-006).
  - El descuento y la emisión ocurren juntos o ninguno ocurre.
  - Un `BORRADOR` no descuenta.
  - Las entradas ya caducadas no se descuentan; si solo quedan existencias caducadas, se trata como sin existencias (INV-004).

### INV-004 · Impedir ventas sin existencias
- **EARS:** Si un ticket pide más cantidad de la que hay en stock vigente de un producto controlado, entonces el sistema deberá rechazar su emisión e informar la existencia disponible.
- **Actor:** Sistema · **Superficie:** `WEB-CLI` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - El stock nunca queda negativo.
  - Si la existencia física real es mayor que la registrada, el `clinica_admin` la corrige con un ajuste (INV-006).

### INV-005 · Devolver stock al cancelar un ticket
- **EARS:** Cuando se cancele un ticket que descontó stock, el sistema deberá devolver a cada producto la cantidad descontada, a las entradas de las que salió, y guardar el movimiento.
- **Actor:** Sistema · **Superficie:** `WEB-CLI` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Cancelar dos veces no devuelve dos veces.

### INV-006 · Ajustar el stock
- **EARS:** Donde el plan de la clínica incluya `inventario`, cuando el `clinica_admin` ajuste la cantidad de un producto con un motivo, el sistema deberá guardar el ajuste como un movimiento.
- **Actor:** `clinica_admin` · **Superficie:** `WEB-CLI` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - El motivo es obligatorio (merma, caducidad, conteo físico, otro).
  - El ajuste queda en la auditoría.

### INV-007 · Alertar el stock bajo
- **EARS:** Cuando el stock de un producto controlado quede igual o por debajo de su mínimo, el sistema deberá incluirlo en la lista de stock bajo del `clinica_admin`.
- **Actor:** `clinica_admin` · **Superficie:** `WEB-CLI` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - La lista muestra producto, existencia y mínimo.
  - Sale de la lista cuando una entrada supera el mínimo.

### INV-008 · Alertar los productos por caducar y caducados
- **EARS:** Cuando una entrada con existencias vaya a caducar en los próximos 30 días o ya haya caducado, el sistema deberá incluirla en la lista de caducidades del `clinica_admin`.
- **Actor:** `clinica_admin` · **Superficie:** `WEB-CLI` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - La lista muestra producto, cantidad restante, fecha de caducidad y días restantes o de retraso, con las caducadas separadas.
  - Se ordena por la fecha más próxima primero.
  - Una entrada caducada se retira del stock vigente y se registra como merma con un ajuste.

### INV-009 · Consultar el historial de movimientos
- **EARS:** Cuando el `clinica_admin` consulte un producto, el sistema deberá mostrar sus movimientos (entradas, ventas, devoluciones y ajustes) con fecha, cantidad, autor y saldo.
- **Actor:** `clinica_admin` · **Superficie:** `WEB-CLI` · **Prioridad:** Debería
- **Criterios de aceptación:**
  - El saldo del último movimiento coincide con el stock actual.

### INV-010 · Apagar el inventario
- **EARS:** Si una clínica deja de tener las funciones `inventario` o `ventas`, entonces el sistema deberá dejar de descontar stock y de exigir existencias, y conservar el stock y los movimientos registrados.
- **Actor:** Sistema · **Superficie:** `WEB-CLI` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Al volver a encenderlo, el `clinica_admin` revisa y ajusta el stock porque pudo cambiar mientras estuvo apagado.
