# Matriz de funciones por rol

| RF | Función | SA | AC | V | R | C | Superficie |
|---|---|---|---|---|---|---|---|
| CLI-001…006 | Alta, edición, suspensión, reactivación y baja de clínicas; crear administrador | ✓ | — | — | — | — | WEB-PLA |
| CLI-007, 008 | Listar y ver el estado de las clínicas (solo datos administrativos) | ✓ | — | — | — | — | WEB-PLA |
| PLA-001, 002, 006, 007 | Planes, asignación, pagos y vigencia | ✓ | — | — | — | — | WEB-PLA |
| PLA-005 | Ver el plan y el uso propios | — | ✓ | — | — | — | WEB-CLI |
| IDA-001, 004, 005 | Iniciar y cerrar sesión; recuperar contraseña | ✓ | ✓ | ✓ | ✓ | ✓ | Todas |
| IDA-002, 003 | Elegir y cambiar de clínica | — | ✓ | ✓ | ✓ | ✓ | APP, WEB-CLI |
| IDA-006, 008 | Registrarse; vincularse con un código | — | — | — | — | ✓ | APP |
| IDA-007 | Invitar a un cliente | — | ✓ | — | ✓ | — | WEB-CLI |
| CON-001, 010 | Datos generales e identidad de la clínica | — | ✓ | — | — | — | WEB-CLI |
| CON-002…004 | Crear, editar y desactivar personal | — | ✓ | — | — | — | WEB-CLI |
| CON-005, 006 | Consultorios y horarios laborales | — | ✓ | — | — | — | WEB-CLI |
| CON-007…009 | Bloquear horarios y gestionar bloqueos | — | ✓ | ◐ | — | — | WEB-CLI, APP |
| PRO-001, 002, 005, 006, 009 | Registrar, editar, consultar y buscar propietarios y mascotas | — | ✓ | ✓ | ✓ | — | WEB-CLI, APP |
| PRO-003 | Dirección del propietario (plan) | — | ✓ | ✓ | ✓ | — | WEB-CLI, APP |
| PRO-004, 007 | Dar de baja propietarios y mascotas | — | ✓ | — | — | — | WEB-CLI |
| AGE-001, 006, 007 | Crear, reprogramar y cancelar citas | — | ✓ | ◐ | ✓ | — | WEB-CLI, APP |
| AGE-004, 010, 011 | Confirmar, marcar no asistió, atendida | — | — | ◐ | — | — | APP |
| AGE-017 | Solicitar una cita | — | — | — | — | ◐ | APP |
| AGE-008 | Solicitar la cancelación de una cita | — | — | — | — | ◐ | APP |
| AGE-009 | Aprobar o rechazar solicitudes de cancelación | — | — | ◐ | — | — | APP |
| AGE-012, 013 | Ver agenda y disponibilidad | — | ✓ | ◐ | ✓ | — | WEB-CLI, APP |
| AGE-014, 015 | Ver y descargar mis citas | — | — | — | — | ◐ | APP |
| EXP-001…006 | Registrar, cerrar y corregir consultas y recetas | — | — | ✓ | — | — | APP |
| EXP-007 | Ver el historial clínico | — | ✓ | ✓ | — | — | APP, WEB-CLI |
| EXP-009 | Descargar la receta en PDF | — | — | ✓ | — | ◐ | APP |
| VAC-001 | Mantener el catálogo de vacunas | — | ✓ | ✓ | — | — | WEB-CLI, APP |
| VAC-008 | Consultar el catálogo de vacunas | — | ✓ | ✓ | ✓ | — | WEB-CLI, APP |
| VAC-002…004, 006 | Registrar aplicación y ajustar próxima dosis | — | — | ✓ | — | — | APP |
| VAC-005, 007 | Ver carnet y alertas de vacunas | — | ✓ | ✓ | — | — | WEB-CLI, APP |
| POR-001…005 | Ver mascotas, citas, carnet y resumen de consultas | — | — | — | — | ◐ | APP |
| NOT-001, 002, 004, 006, 007 | Recibir notificaciones del cliente | — | — | — | — | ✓ | APP |
| NOT-003 | Recibir solicitudes del cliente | — | — | ✓ | — | — | APP |
| NOT-005, 008, 009 | Bandeja, push y dispositivos | — | — | ✓ | — | ✓ | APP |
| FAC-001 | Mantener el catálogo de conceptos | — | ✓ | — | — | — | WEB-CLI |
| FAC-002…005, 008 | Crear, emitir, cobrar, imprimir y consultar tickets | — | ✓ | — | ✓ | — | WEB-CLI |
| FAC-006, 007 | Cancelar un ticket | — | ✓ | — | — | — | WEB-CLI |
| FAC-009, 010 | Turno de caja | — | ◐ | — | ✓ | — | WEB-CLI |
| INV-001, 006…009 | Productos, ajustes, alertas e historial | — | ✓ | — | — | — | WEB-CLI |
| INV-002 | Registrar entradas de stock | — | ✓ | — | ✓ | — | WEB-CLI |
| REP-001…004 | Reportes | — | ✓ | — | — | — | WEB-CLI |
| RNF-AUD-003 | Consultar la auditoría | ◐ | ✓ | — | — | — | WEB-CLI, WEB-PLA |

**Notas de las condiciones**

- **CON-007…009 y AGE-001, 006, 007 (V).** Solo sobre su propia agenda.
- **AGE-004, 009, 010, 011 (V).** Solo sobre las citas donde es el veterinario asignado.
- **AGE-008, AGE-017 (C).** Solo para mascotas de las que es propietario.
- **AGE-012 (V).** Ve la agenda de la clínica en solo lectura y opera sobre la suya.
- **AGE-014, 015, POR y EXP-009 (C).** Propiedad verificada en cada petición (RNF-SEG-003) y solo si el plan incluye `portal_cliente`.
- **FAC-009, 010 (AC).** Consulta los turnos y sus diferencias; no abre ni cierra turnos.
- **RNF-AUD-003 (SA).** Solo la auditoría de la plataforma, no la de las clínicas.
- **Funciones de plan.** Las funciones de FAC, INV, REP, turno de caja, recordatorios, visitas a domicilio y portal solo existen si el plan de la clínica las incluye (PLA-004).
- **Lo que ningún rol ve.** La `recepcionista` no ve consultas, recetas, carnet de vacunas ni próximas dosis. El `super_admin` no ve nada clínico ni de pacientes.
