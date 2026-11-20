# Especificación de funcionalidad: [NOMBRE DE LA FUNCIONALIDAD]

**Rama de la funcionalidad**: `[###-nombre-de-la-funcionalidad]`

**Creada**: [FECHA]

**Estado**: Borrador

**Entrada**: Descripción del usuario: "$ARGUMENTS"

**Requerimientos que cubre**: [IDs de `docs/requerimientos/`, por ejemplo AGE-001, AGE-002]

## Escenarios de usuario y pruebas *(obligatorio)*

<!--
  IMPORTANTE: Las historias de usuario deben PRIORIZARSE como recorridos ordenados por importancia.
  Cada historia debe poder PROBARSE DE FORMA INDEPENDIENTE: si se implementa solo UNA de ellas,
  debe seguir entregando valor.

  Asigna prioridades (P1, P2, P3, etc.): P1 es la más crítica.
  Piensa en cada historia como una porción autónoma que se puede:
  - Desarrollar de forma independiente
  - Probar de forma independiente
  - Desplegar de forma independiente
  - Demostrar de forma independiente
-->

### Historia de usuario 1 - [Título breve] (Prioridad: P1)

Como [rol], quiero [acción] para [beneficio].

**Por qué esta prioridad**: [Explica el valor y por qué tiene este nivel de prioridad]

**Prueba independiente**: [Describe cómo probarla de forma independiente, por ejemplo "Se prueba completa con [acción concreta] y entrega [valor concreto]"]

**Escenarios de aceptación**:

1. **Dado** [estado inicial], **Cuando** [acción], **Entonces** [resultado esperado]
2. **Dado** [estado inicial], **Cuando** [acción], **Entonces** [resultado esperado]

---

### Historia de usuario 2 - [Título breve] (Prioridad: P2)

Como [rol], quiero [acción] para [beneficio].

**Por qué esta prioridad**: [Explica el valor y por qué tiene este nivel de prioridad]

**Prueba independiente**: [Describe cómo probarla de forma independiente]

**Escenarios de aceptación**:

1. **Dado** [estado inicial], **Cuando** [acción], **Entonces** [resultado esperado]

---

### Historia de usuario 3 - [Título breve] (Prioridad: P3)

Como [rol], quiero [acción] para [beneficio].

**Por qué esta prioridad**: [Explica el valor y por qué tiene este nivel de prioridad]

**Prueba independiente**: [Describe cómo probarla de forma independiente]

**Escenarios de aceptación**:

1. **Dado** [estado inicial], **Cuando** [acción], **Entonces** [resultado esperado]

---

[Agrega más historias de usuario según haga falta, cada una con su prioridad]

### Casos límite

<!--
  ACCIÓN REQUERIDA: El contenido de esta sección son marcadores de posición.
  Reemplázalos con los casos límite reales. Considera siempre: acceso de otra clínica,
  rol equivocado, función que el plan de la clínica no incluye y concurrencia.
-->

- ¿Qué pasa cuando [condición límite]?
- ¿Cómo responde el sistema ante [escenario de error]?

## Requisitos *(obligatorio)*

<!--
  ACCIÓN REQUERIDA: El contenido de esta sección son marcadores de posición.
  Reemplázalos con los requisitos funcionales reales.
-->

### Requisitos funcionales

- **FR-001**: El sistema DEBE [capacidad específica, por ejemplo "permitir crear una cita"]
- **FR-002**: El sistema DEBE [capacidad específica, por ejemplo "rechazar una cita fuera del horario"]
- **FR-003**: El usuario DEBE poder [interacción clave, por ejemplo "cancelar su cita"]
- **FR-004**: El sistema DEBE [requisito de datos, por ejemplo "conservar la versión anterior del expediente"]
- **FR-005**: El sistema DEBE [comportamiento, por ejemplo "registrar en auditoría cada lectura"]

*Ejemplo de cómo marcar requisitos poco claros:*

- **FR-006**: El sistema DEBE notificar al cliente mediante [NEEDS CLARIFICATION: canal no especificado]
- **FR-007**: El sistema DEBE conservar los datos durante [NEEDS CLARIFICATION: periodo de retención no especificado]

### Entidades clave *(incluir si la funcionalidad maneja datos)*

- **[Entidad 1]**: [Qué representa, atributos clave sin detalles de implementación]
- **[Entidad 2]**: [Qué representa, relaciones con otras entidades]

## Criterios de éxito *(obligatorio)*

<!--
  ACCIÓN REQUERIDA: Define criterios de éxito medibles.
  Deben ser independientes de la tecnología y medibles.
-->

### Resultados medibles

- **SC-001**: [Métrica medible, por ejemplo "Una recepcionista crea una cita en menos de 1 minuto"]
- **SC-002**: [Métrica medible, por ejemplo "Dos reservas simultáneas del mismo horario: solo una se acepta"]
- **SC-003**: [Métrica de satisfacción, por ejemplo "El 90 % completa la tarea principal al primer intento"]
- **SC-004**: [Métrica de negocio, por ejemplo "Se reducen las citas sin asistencia en un 20 %"]

## Supuestos

<!--
  ACCIÓN REQUERIDA: El contenido de esta sección son marcadores de posición.
  Reemplázalos con los supuestos correctos, basados en valores razonables
  elegidos cuando la descripción no especificó ciertos detalles.
-->

- [Supuesto sobre los usuarios, por ejemplo "El veterinario tiene conexión estable durante la consulta"]
- [Supuesto sobre los límites del alcance, por ejemplo "iOS queda fuera de esta entrega"]
- [Supuesto sobre datos o entorno, por ejemplo "Se reutiliza la autenticación de Keycloak"]
- [Dependencia de otro sistema o fase, por ejemplo "Requiere el aislamiento por clínica de la fase 2"]
