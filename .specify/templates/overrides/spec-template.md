# Especificaci├│n de funcionalidad: [NOMBRE DE LA FUNCIONALIDAD]

**Rama de la funcionalidad**: `[###-nombre-de-la-funcionalidad]`

**Creada**: [FECHA]

**Estado**: Borrador

**Entrada**: Descripci├│n del usuario: "$ARGUMENTS"

**Requerimientos que cubre**: [IDs de `docs/requerimientos/`, por ejemplo AGE-001, AGE-002]

## Escenarios de usuario y pruebas *(obligatorio)*

<!--
  IMPORTANTE: Las historias de usuario deben PRIORIZARSE como recorridos ordenados por importancia.
  Cada historia debe poder PROBARSE DE FORMA INDEPENDIENTE: si se implementa solo UNA de ellas,
  debe seguir entregando valor.

  Asigna prioridades (P1, P2, P3, etc.): P1 es la m├ís cr├¡tica.
  Piensa en cada historia como una porci├│n aut├│noma que se puede:
  - Desarrollar de forma independiente
  - Probar de forma independiente
  - Desplegar de forma independiente
  - Demostrar de forma independiente
-->

### Historia de usuario 1 - [T├¡tulo breve] (Prioridad: P1)

Como [rol], quiero [acci├│n] para [beneficio].

**Por qu├® esta prioridad**: [Explica el valor y por qu├® tiene este nivel de prioridad]

**Prueba independiente**: [Describe c├│mo probarla de forma independiente, por ejemplo "Se prueba completa con [acci├│n concreta] y entrega [valor concreto]"]

**Escenarios de aceptaci├│n**:

1. **Dado** [estado inicial], **Cuando** [acci├│n], **Entonces** [resultado esperado]
2. **Dado** [estado inicial], **Cuando** [acci├│n], **Entonces** [resultado esperado]

---

### Historia de usuario 2 - [T├¡tulo breve] (Prioridad: P2)

Como [rol], quiero [acci├│n] para [beneficio].

**Por qu├® esta prioridad**: [Explica el valor y por qu├® tiene este nivel de prioridad]

**Prueba independiente**: [Describe c├│mo probarla de forma independiente]

**Escenarios de aceptaci├│n**:

1. **Dado** [estado inicial], **Cuando** [acci├│n], **Entonces** [resultado esperado]

---

### Historia de usuario 3 - [T├¡tulo breve] (Prioridad: P3)

Como [rol], quiero [acci├│n] para [beneficio].

**Por qu├® esta prioridad**: [Explica el valor y por qu├® tiene este nivel de prioridad]

**Prueba independiente**: [Describe c├│mo probarla de forma independiente]

**Escenarios de aceptaci├│n**:

1. **Dado** [estado inicial], **Cuando** [acci├│n], **Entonces** [resultado esperado]

---

[Agrega m├ís historias de usuario seg├║n haga falta, cada una con su prioridad]

### Casos l├¡mite

<!--
  ACCI├ôN REQUERIDA: El contenido de esta secci├│n son marcadores de posici├│n.
  Reempl├ízalos con los casos l├¡mite reales. Considera siempre: acceso de otra cl├¡nica,
  rol equivocado, funci├│n que el plan de la cl├¡nica no incluye y concurrencia.
-->

- ┬┐Qu├® pasa cuando [condici├│n l├¡mite]?
- ┬┐C├│mo responde el sistema ante [escenario de error]?

## Requisitos *(obligatorio)*

<!--
  ACCI├ôN REQUERIDA: El contenido de esta secci├│n son marcadores de posici├│n.
  Reempl├ízalos con los requisitos funcionales reales.
-->

### Requisitos funcionales

- **FR-001**: El sistema DEBE [capacidad espec├¡fica, por ejemplo "permitir crear una cita"]
- **FR-002**: El sistema DEBE [capacidad espec├¡fica, por ejemplo "rechazar una cita fuera del horario"]
- **FR-003**: El usuario DEBE poder [interacci├│n clave, por ejemplo "cancelar su cita"]
- **FR-004**: El sistema DEBE [requisito de datos, por ejemplo "conservar la versi├│n anterior del expediente"]
- **FR-005**: El sistema DEBE [comportamiento, por ejemplo "registrar en auditor├¡a cada lectura"]

*Ejemplo de c├│mo marcar requisitos poco claros:*

- **FR-006**: El sistema DEBE notificar al cliente mediante [NEEDS CLARIFICATION: canal no especificado]
- **FR-007**: El sistema DEBE conservar los datos durante [NEEDS CLARIFICATION: periodo de retenci├│n no especificado]

### Entidades clave *(incluir si la funcionalidad maneja datos)*

- **[Entidad 1]**: [Qu├® representa, atributos clave sin detalles de implementaci├│n]
- **[Entidad 2]**: [Qu├® representa, relaciones con otras entidades]

## Criterios de ├®xito *(obligatorio)*

<!--
  ACCI├ôN REQUERIDA: Define criterios de ├®xito medibles.
  Deben ser independientes de la tecnolog├¡a y medibles.
-->

### Resultados medibles

- **SC-001**: [M├®trica medible, por ejemplo "Una recepcionista crea una cita en menos de 1 minuto"]
- **SC-002**: [M├®trica medible, por ejemplo "Dos reservas simult├íneas del mismo horario: solo una se acepta"]
- **SC-003**: [M├®trica de satisfacci├│n, por ejemplo "El 90 % completa la tarea principal al primer intento"]
- **SC-004**: [M├®trica de negocio, por ejemplo "Se reducen las citas sin asistencia en un 20 %"]

## Supuestos

<!--
  ACCI├ôN REQUERIDA: El contenido de esta secci├│n son marcadores de posici├│n.
  Reempl├ízalos con los supuestos correctos, basados en valores razonables
  elegidos cuando la descripci├│n no especific├│ ciertos detalles.
-->

- [Supuesto sobre los usuarios, por ejemplo "El veterinario tiene conexi├│n estable durante la consulta"]
- [Supuesto sobre los l├¡mites del alcance, por ejemplo "iOS queda fuera de esta entrega"]
- [Supuesto sobre datos o entorno, por ejemplo "Se reutiliza la autenticaci├│n de Keycloak"]
- [Dependencia de otro sistema o fase, por ejemplo "Requiere el aislamiento por cl├¡nica de la fase 2"]
