# REP · Reportes

Superficie: `WEB-CLI`. Actor: `clinica_admin`. Solo lectura.

Todo el módulo se ofrece solo donde el plan de la clínica incluye `reportes`. Todos los reportes se piden por rango de fechas y muestran conteos y totales de la clínica, nunca el contenido de un expediente.

### REP-001 · Reporte de citas
- **EARS:** Donde el plan de la clínica incluya `reportes`, cuando el `clinica_admin` elija un rango de fechas, el sistema deberá mostrar el número de citas por estado y por veterinario.
- **Actor:** `clinica_admin` · **Superficie:** `WEB-CLI` · **Prioridad:** Debería
- **Criterios de aceptación:**
  - Muestra el total de citas y el porcentaje de cada estado.
  - Se puede filtrar por veterinario.

### REP-002 · Reporte de ventas
- **EARS:** Donde el plan de la clínica incluya `reportes` y `ventas`, cuando el `clinica_admin` elija un rango de fechas, el sistema deberá mostrar el número de tickets pagados y el total vendido, sin contar los cancelados.
- **Actor:** `clinica_admin` · **Superficie:** `WEB-CLI` · **Prioridad:** Debería
- **Criterios de aceptación:**
  - Muestra el total por día.
  - El total coincide con la suma de los tickets pagados del rango (FAC-008).

### REP-003 · Reporte de vacunas aplicadas
- **EARS:** Donde el plan de la clínica incluya `reportes`, cuando el `clinica_admin` elija un rango de fechas, el sistema deberá mostrar cuántas dosis se aplicaron de cada vacuna.
- **Actor:** `clinica_admin` · **Superficie:** `WEB-CLI` · **Prioridad:** Podría
- **Criterios de aceptación:**
  - Solo muestra conteos por vacuna, no mascotas ni propietarios.

### REP-004 · Reporte de productos
- **EARS:** Donde el plan de la clínica incluya `reportes`, `ventas` e `inventario`, cuando el `clinica_admin` elija un rango de fechas, el sistema deberá mostrar los productos más vendidos y los que están en stock bajo.
- **Actor:** `clinica_admin` · **Superficie:** `WEB-CLI` · **Prioridad:** Podría
- **Criterios de aceptación:**
  - Muestra la cantidad vendida de cada producto en el rango.
  - Los productos en stock bajo coinciden con INV-007.
