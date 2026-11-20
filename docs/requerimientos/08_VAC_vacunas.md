# VAC · Vacunas

Superficies: `APP`, `WEB-CLI`. Actores: `veterinario`, `clinica_admin`.

El catálogo es **por clínica**: cada clínica mantiene el suyo y no hay catálogo global. Cada vacuna tiene un solo intervalo; los esquemas de varias dosis con intervalos distintos quedan fuera.

### VAC-001 · Mantener el catálogo de vacunas de la clínica
- **EARS:** Cuando el `clinica_admin` o el `veterinario` cree o edite una vacuna del catálogo de su clínica, el sistema deberá guardar su nombre, la especie a la que aplica y su intervalo en días.
- **Actor:** `clinica_admin`, `veterinario` · **Superficie:** `WEB-CLI`, `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Nombre, especie e intervalo (mayor que cero) son obligatorios.
  - Editar el intervalo no recalcula las próximas dosis ya registradas.
  - Una vacuna con aplicaciones registradas no se borra; se desactiva y deja de ofrecerse.

### VAC-002 · Registrar la aplicación de una vacuna
- **EARS:** Cuando el `veterinario` registre la aplicación de una vacuna a una mascota con fecha y lote, el sistema deberá guardarla en el carnet de la mascota.
- **Actor:** `veterinario` · **Superficie:** `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - La fecha no puede ser futura.
  - Lote y observaciones son opcionales, y la vacuna debe ser compatible con la especie de la mascota.
  - Una aplicación registrada por error se corrige con versión (mismas reglas que EXP-006).
  - Si la vacuna está ligada a un producto de inventario, aplica VAC-006.

### VAC-003 · Calcular la próxima dosis
- **EARS:** Cuando se registre la aplicación de una vacuna, el sistema deberá calcular la fecha de próxima dosis sumando el intervalo de la vacuna a la fecha de aplicación.
- **Actor:** Sistema · **Superficie:** `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Aplicada el 10 de enero con intervalo de 365 días, la próxima dosis es el 10 de enero del año siguiente (o la fecha que resulte de sumar 365 días).
  - Cada aplicación calcula su propia próxima dosis; la vigente es la de la aplicación más reciente.

### VAC-004 · Ajustar manualmente la próxima dosis
- **EARS:** Cuando el `veterinario` indique una fecha distinta de próxima dosis, el sistema deberá guardarla en lugar de la calculada y conservar el valor original.
- **Actor:** `veterinario` · **Superficie:** `APP` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - La fecha manual debe ser posterior a la fecha de aplicación.
  - El carnet marca el valor como ajustado a mano.

### VAC-005 · Ver el carnet de vacunación
- **EARS:** Cuando el `veterinario` o el `clinica_admin` abra el carnet de una mascota, el sistema deberá listar sus aplicaciones con vacuna, fecha, lote, veterinario y próxima dosis.
- **Actor:** `veterinario`, `clinica_admin` · **Superficie:** `APP`, `WEB-CLI` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - La `recepcionista` no accede al carnet ni a las próximas dosis, porque son datos clínicos.
  - El orden es cronológico inverso, y se agrupa por vacuna.

### VAC-006 · Registrar el cargo y el consumo de una vacuna aplicada
- **EARS:** Donde el plan de la clínica incluya `ventas` y la vacuna esté ligada a un concepto, cuando el `veterinario` registre su aplicación, el sistema deberá dejarla anotada como pendiente de cobro para la recepción y, donde el plan incluya `inventario`, descontar una unidad al emitirse el ticket.
- **Actor:** `veterinario` · **Superficie:** `APP` · **Prioridad:** Debería
- **Criterios de aceptación:**
  - El vínculo vacuna ↔ concepto es opcional y lo define el `clinica_admin`.
  - El veterinario no cobra ni emite tickets; solo deja el concepto anotado en la consulta (FAC-002).
  - Sin vínculo, la aplicación se registra y no genera nada.

### VAC-007 · Listar vacunas vencidas y próximas
- **EARS:** Cuando el `veterinario` o el `clinica_admin` abra la lista de alertas, el sistema deberá mostrar las mascotas activas con una próxima dosis vencida o dentro de los siguientes 30 días.
- **Actor:** `veterinario`, `clinica_admin` · **Superficie:** `APP`, `WEB-CLI` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Cada fila muestra mascota, propietario, vacuna, fecha de próxima dosis y días restantes o de retraso.
  - Se ordena por la fecha más antigua primero y se separa "vencidas" de "próximas".
  - Una dosis desaparece de la lista al registrarse la siguiente aplicación de la misma vacuna.
  - Las mascotas inactivas no aparecen.
  - Un `veterinario` con varias clínicas ve solo la clínica activa.
  - Los 30 días son un valor configurable.

### VAC-008 · Consultar el catálogo de vacunas de la clínica
- **EARS:** Cuando la `recepcionista` consulte el catálogo de vacunas, el sistema deberá mostrar el nombre y la especie de cada vacuna activa y, donde la vacuna esté ligada a un producto y el plan incluya `inventario`, su existencia.
- **Actor:** `recepcionista` · **Superficie:** `WEB-CLI` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Solo lectura: la recepción no crea ni edita vacunas.
  - Permite saber si la clínica tiene una vacuna disponible cuando un cliente llega solo a comprarla.
  - No muestra aplicaciones, carnets ni próximas dosis.
