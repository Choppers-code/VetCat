# Especificación de funcionalidad: Planes y funciones habilitadas

**Rama de la funcionalidad**: `feature/212-spec-planes`

**Creada**: 2026-11-24

**Estado**: Borrador

**Entrada**: Issue #212 (spec de deuda de la Fase 3) y épica #4.

**Requerimientos que cubre**: PLA-001 a PLA-007 (`docs/requerimientos/02_PLA_planes.md`).

**Nota de deuda**: el modelo de datos ya está en `develop` (`catalogo/V5`, #163) y la verificación llega en #156. Esta especificación llega después del esquema, a propósito y dicho: fija las reglas para la verificación, la vigencia y las pruebas.

## Escenarios de usuario y pruebas *(obligatorio)*

Regla general: hay **un solo modelo de datos**. El plan enciende funciones y fija límites; no duplica esquemas ni se escribe en el código. Nombres, precios y límites son datos. Toda lectura de la clínica activa sale del token, nunca de la petición.

### Historia de usuario 1 - Mantener el catálogo de planes (Prioridad: P1)

Como `super_admin`, quiero crear y editar planes con sus límites y funciones para vender lo que cada clínica necesita.

**Por qué esta prioridad**: sin catálogo no hay plan que asignar y ninguna clínica puede operar.

**Prueba independiente**: se prueba creando dos planes con límites y funciones distintas y editándolos.

**Escenarios de aceptación**:

1. **Dado** un plan nuevo, **Cuando** el `super_admin` lo guarda con nombre y funciones, **Entonces** queda disponible para asignar.
2. **Dado** un plan con funciones `ventas` e `inventario`, **Cuando** se intenta guardar `inventario` sin `ventas`, **Entonces** se rechaza nombrando el requisito.
3. **Dado** un plan en uso por una clínica, **Cuando** se intenta borrar, **Entonces** se rechaza; se marca inactivo y deja de ofrecerse.
4. **Dado** un límite vacío, **Cuando** se guarda el plan, **Entonces** ese recurso queda sin límite.

---

### Historia de usuario 2 - Asignar un plan a una clínica (Prioridad: P1)

Como `super_admin`, quiero asignar o cambiar el plan de una clínica para que sus límites y funciones se apliquen de inmediato.

**Por qué esta prioridad**: es lo que conecta el catálogo con el negocio de cada clínica.

**Prueba independiente**: se prueba asignando un plan a una clínica y comprobando que una función inhabilitada se rechaza en la siguiente petición.

**Escenarios de aceptación**:

1. **Dado** una clínica sin plan, **Cuando** se le asigna uno, **Entonces** opera con sus funciones y límites.
2. **Dado** un cambio de plan, **Cuando** se guarda, **Entonces** queda en la auditoría de plataforma con plan anterior, nuevo y autor.
3. **Dado** un plan cambiado, **Cuando** la clínica hace la siguiente petición, **Entonces** ve las funciones nuevas sin reiniciar nada.

---

### Historia de usuario 3 - Aplicar límites (Prioridad: P1)

Como sistema, debo rechazar el alta que supere un límite para que una clínica no use más de lo que paga.

**Prueba independiente**: se prueba con un plan de 3 mascotas y una clínica que ya tiene 3 activas.

**Escenarios de aceptación**:

1. **Dado** un plan con límite de 3 mascotas y 3 activas, **Cuando** se registra la cuarta, **Entonces** se rechaza nombrando el límite y el plan.
2. **Dado** una mascota dada de baja, **Cuando** se cuenta, **Entonces** no cuenta para el límite.
3. **Dado** un cambio a un plan con límite menor que el uso actual, **Cuando** se aplica, **Entonces** no se borra nada y solo se bloquean las altas.

---

### Historia de usuario 4 - Habilitar funciones según el plan (Prioridad: P1)

Como sistema, debo rechazar en la API cualquier operación de una función no incluida, aunque ocultar en la interfaz ya lo haga.

**Prueba independiente**: se prueba llamando directo a la API de una función no incluida.

**Escenarios de aceptación**:

1. **Dado** una clínica sin `ventas`, **Cuando** se llama a la API de tickets, **Entonces** se rechaza con el error de función no disponible, no con un 403 de rol.
2. **Dado** un plan que apaga `ventas`, **Cuando** se consulta `turno_caja` o `inventario`, **Entonces** también están apagadas.
3. **Dado** una función desactivada, **Cuando** se reactiva, **Entonces** los datos creados mientras estuvo activa vuelven a verse.

---

### Historia de usuario 5 - Consultar el plan y el uso (Prioridad: P2)

Como `clinica_admin`, quiero ver mi plan, su vigencia y el uso frente a cada límite para saber si me estoy acercando.

**Escenarios de aceptación**:

1. **Dado** una clínica con 180 de 200 mascotas, **Cuando** el `clinica_admin` abre su plan, **Entonces** ve "Mascotas: 180 de 200".
2. **Dado** un `clinica_admin`, **Cuando** intenta cambiar su plan, **Entonces** se rechaza: solo lo ve.

---

### Historia de usuario 6 - Registrar el pago y extender la vigencia (Prioridad: P1)

Como `super_admin`, quiero registrar el pago de una clínica para extender su vigencia.

**Escenarios de aceptación**:

1. **Dado** un pago registrado, **Cuando** se guarda, **Entonces** guarda fecha, monto, periodo y autor.
2. **Dado** una clínica suspendida por vencimiento, **Cuando** se registra un pago, **Entonces** vuelve a estar activa.
3. **Dado** un pago antes del vencimiento, **Cuando** se registra, **Entonces** el periodo nuevo se suma al actual.

---

### Historia de usuario 7 - Suspender al vencer (Prioridad: P1)

Como sistema, debo suspender una clínica tres días después del vencimiento sin pago.

**Escenarios de aceptación**:

1. **Dado** un plan vencido hace 2 días, **Cuando** la clínica opera, **Entonces** todo funciona y el `clinica_admin` ve el aviso.
2. **Dado** un plan vencido hace 4 días, **Cuando** pasa el proceso de suspensión, **Entonces** la clínica queda suspendida y no puede abrir sesión.
3. **Dado** una suspensión, **Cuando** se registra el pago, **Entonces** vuelve a operar con sus datos intactos.

---

### Casos límite

- Plan inexistente o inactivo en una asignación.
- Dos clínicas con el mismo plan: el cambio de plan del catálogo las afecta a las dos y eso se confirma antes de guardar.
- Función con requisito apagado y función dependiente encendida por datos viejos.
- Pago con periodo solapado o invertido.
- Clínica dada de baja por otra causa: el vencimiento no la revive.

## Requisitos *(obligatorio)*

### Requisitos funcionales

- **FR-001**: el plan guarda nombre, descripción, activo, funciones y límites (PLA-001).
- **FR-002**: una función con requisito dependiente no se puede incluir sin él (PLA-001).
- **FR-003**: un plan en uso no se borra; se marca inactivo (PLA-001).
- **FR-004**: cada clínica tiene exactamente un plan, con historial de cambios (PLA-002).
- **FR-005**: un límite superado rechaza el alta nombrando el límite y el plan (PLA-003).
- **FR-006**: las bajas lógicas no cuentan para el límite (PLA-003).
- **FR-007**: una función no incluida se rechaza en la API en cada petición (PLA-004).
- **FR-008**: apagar una función apaga las que la requieren (PLA-004).
- **FR-009**: el `clinica_admin` consulta plan, vigencia y uso, sin cambiarlo (PLA-005).
- **FR-010**: el pago guarda fecha, monto, periodo y autor, y extiende la vigencia (PLA-006).
- **FR-011**: vencida la vigencia, tres días de gracia y después suspensión (PLA-007).

### Entidades clave

- `catalogo.plan`, `catalogo.plan_funcion`, `catalogo.plan_limite`, `catalogo.pago_plan` — ya en `V5`.
- `catalogo.clinica.plan_id` — ya en `V5`; lo fija el aprovisionamiento.
- **Vigencia**: derivada del último `pago_plan.periodo_hasta` de la clínica, sin columna duplicada.

## Criterios de éxito *(obligatorio)*

- Una función no incluida se rechaza en el servidor en el 100 % de los endpoints que la usan.
- Un cambio de plan se aplica en la siguiente petición, sin reinicio ni caché.
- El uso de un límite se calcula sobre datos reales, no sobre contadores guardados.
- La suspensión por vencimiento es idempotente y no toca datos.

## Supuestos

- El cobro en línea está fuera de alcance: el pago es manual y lo registra el `super_admin`.
- La vigencia se deriva de los pagos; si el equipo prefiere una columna `vigente_hasta`, se cambia antes de #158.
- Las funciones y sus requisitos son una lista cerrada; añadir una exige migración y este documento.

## Fuera de alcance

- Precios y planes concretos del negocio: son datos, no código.
- Cobro en línea, facturación fiscal y datos bancarios.
- Cuotas de almacenamiento o de adjuntos.
