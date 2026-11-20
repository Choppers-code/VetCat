# PLA · Planes y funciones habilitadas

Hace que VetCat sea SaaS: cada clínica tiene un plan con vigencia, y el plan decide qué límites y funciones tiene. Hay un solo modelo de datos; el plan enciende o apaga funciones y fija límites, sin duplicar esquemas. Los nombres, precios y límites de cada plan se definen desde PLA-001 y no se escriben en el código. El alta y la renovación son manuales: el `super_admin` registra el plan y el pago; no hay cobro en línea ni captura de datos bancarios.

**Funciones de plan.** Cada una es una clave que los demás módulos consultan:

| Clave | Función | Requiere | Módulos |
|---|---|---|---|
| `portal_cliente` | Vista del cliente en la app: sus mascotas, citas, carnet y consultas | — | POR, AGE, IDA |
| `recordatorios` | Recordatorios automáticos de citas y vacunas | — | NOT |
| `ventas` | Catálogo de conceptos, tickets y cobro en efectivo | — | FAC |
| `turno_caja` | Apertura, cierre y arqueo de caja | `ventas` | FAC |
| `inventario` | Stock, caducidades y alertas | `ventas` | INV |
| `visitas_domicilio` | Dirección del propietario y citas a domicilio | — | PRO, AGE |
| `reportes` | Reportes para el administrador de la clínica | — | REP |

Quedan fuera de esta versión: multi-sede, API y webhooks para terceros, y marca blanca.

### PLA-001 · Mantener el catálogo de planes
- **EARS:** Cuando el `super_admin` cree o edite un plan, el sistema deberá guardar su nombre, descripción, límites (usuarios, veterinarios, mascotas) y la lista de funciones de plan que incluye.
- **Actor:** `super_admin` · **Superficie:** `WEB-PLA` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Un límite vacío significa "sin límite".
  - Un plan no puede incluir una función sin su requisito (por ejemplo, `inventario` sin `ventas`).
  - Un plan en uso no se puede borrar; se marca como inactivo y deja de ofrecerse a clínicas nuevas.
  - Cambiar un plan en uso se aplica a todas sus clínicas y lo confirma el `super_admin` antes de guardar.

### PLA-002 · Asignar un plan a una clínica
- **EARS:** Cuando el `super_admin` asigne o cambie el plan de una clínica, el sistema deberá aplicar de inmediato los límites y las funciones del nuevo plan a esa clínica.
- **Actor:** `super_admin` · **Superficie:** `WEB-PLA` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Toda clínica tiene exactamente un plan; al darla de alta (CLI-001) se elige uno.
  - El cambio queda en la auditoría de la plataforma con plan anterior, plan nuevo y autor.

### PLA-003 · Aplicar los límites del plan
- **EARS:** Si una operación haría que la clínica superara un límite de su plan (usuarios, veterinarios o mascotas), entonces el sistema deberá rechazarla e informar el límite alcanzado y el plan actual.
- **Actor:** Sistema · **Superficie:** Todas · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Registrar una mascota número N+1 en un plan con límite N se rechaza con un mensaje que nombra el límite.
  - Las mascotas dadas de baja lógica no cuentan para el límite.
  - Al bajar a un plan con límite menor que el uso actual, los datos existentes se conservan; solo se bloquean las altas nuevas.

### PLA-004 · Habilitar funciones según el plan
- **EARS:** Donde el plan de la clínica no incluya una función de plan, el sistema deberá ocultar esa función en la interfaz y rechazar en la API cualquier operación de esa función.
- **Actor:** Sistema · **Superficie:** Todas · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Ocultar en la interfaz no basta: una llamada directa a la API de una función no incluida responde con un error de función no disponible en el plan.
  - La función se verifica en el servidor en cada petición, nunca por lo que informe la app.
  - Los datos creados mientras la función estuvo activa se conservan si después se desactiva, y vuelven a verse si se reactiva.
  - Si una función deja de estar incluida, también dejan de estarlo las que la requieren (apagar `ventas` apaga `turno_caja` e `inventario`).

### PLA-005 · Consultar el plan y el uso de la clínica
- **EARS:** Cuando el `clinica_admin` abra la sección de plan, el sistema deberá mostrar el plan de su clínica, las funciones incluidas, la fecha de vigencia y el uso actual frente a cada límite.
- **Actor:** `clinica_admin` · **Superficie:** `WEB-CLI` · **Prioridad:** Debería
- **Criterios de aceptación:**
  - Muestra, por ejemplo, "Mascotas: 180 de 200".
  - La clínica no puede cambiar su propio plan; solo lo ve.

### PLA-006 · Registrar el pago y extender la vigencia
- **EARS:** Cuando el `super_admin` registre el pago de una clínica con su periodo, el sistema deberá extender la vigencia de su plan hasta la fecha que corresponda.
- **Actor:** `super_admin` · **Superficie:** `WEB-PLA` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Cada pago registrado guarda fecha, monto, periodo cubierto y autor.
  - Si la clínica estaba suspendida por vencimiento, queda activa al registrarse el pago.
  - Si el pago se registra antes del vencimiento, el nuevo periodo se suma a la vigencia actual.

### PLA-007 · Suspender la clínica al vencer el plan
- **EARS:** Cuando venza la vigencia del plan de una clínica y pasen 3 días de gracia sin que se registre un pago, el sistema deberá suspender la clínica.
- **Actor:** Sistema · **Superficie:** `WEB-PLA` · **Prioridad:** Debe
- **Criterios de aceptación:**
  - Durante la gracia, el `clinica_admin` ve un aviso de plan vencido y todo sigue funcionando.
  - La suspensión por vencimiento es la de CLI-004: los datos no se tocan y el acceso se restablece con PLA-006.
  - El `super_admin` ve en el listado de clínicas las que están por vencer y las vencidas.
