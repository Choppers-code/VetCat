---

description: "Plantilla de lista de tareas para implementar una funcionalidad"
---

# Tareas: [NOMBRE DE LA FUNCIONALIDAD]

**Entrada**: Documentos de diseño en `/specs/[###-funcionalidad]/`

**Requisitos previos**: plan.md (obligatorio), spec.md (obligatorio para las historias de usuario), research.md, data-model.md, contracts/

**Pruebas**: Los ejemplos de abajo incluyen tareas de prueba. En VetCat las pruebas son obligatorias (constitución, principio IV): aislamiento entre clínicas, autorización por rol y concurrencia van primero.

**Organización**: Las tareas se agrupan por historia de usuario para poder implementar y probar cada una de forma independiente.

## Formato: `[ID] [P?] [Historia] Descripción`

- **[P]**: Se puede ejecutar en paralelo (archivos distintos, sin dependencias)
- **[Historia]**: A qué historia de usuario pertenece la tarea (por ejemplo, US1, US2, US3)
- Incluye las rutas exactas de los archivos en las descripciones

## Convenciones de rutas

- **Backend**: `backend/src/main/java/com/vetcat/<modulo>/` y `backend/src/test/java/com/vetcat/<modulo>/`
- **Migraciones**: `database/migrations/catalogo/` y `database/migrations/tenant/`
- **Frontend**: `frontend/apps/` y `frontend/packages/`
- Ajusta las rutas según la estructura de plan.md y de `docs/Estructura_proyecto.md`

<!--
  ============================================================================
  IMPORTANTE: Las tareas de abajo son EJEMPLOS ilustrativos.

  El comando /speckit-tasks DEBE reemplazarlas con tareas reales según:
  - Las historias de usuario de spec.md (con sus prioridades P1, P2, P3...)
  - Los requisitos de plan.md
  - Las entidades de data-model.md
  - Los endpoints de contracts/

  Las tareas DEBEN organizarse por historia de usuario para que cada una pueda:
  - Implementarse de forma independiente
  - Probarse de forma independiente
  - Entregarse como un incremento

  NO conserves estas tareas de ejemplo en el tasks.md generado.
  ============================================================================
-->

## Fase 1: Preparación (infraestructura compartida)

**Propósito**: Inicialización del trabajo y estructura básica

- [ ] T001 Crear la estructura del módulo según el plan de implementación
- [ ] T002 Crear la rama de trabajo con `scripts/nueva-rama.ps1` y enlazar el issue
- [ ] T003 [P] Configurar las reglas de análisis y formato del módulo

---

## Fase 2: Fundamentos (requisitos bloqueantes)

**Propósito**: Infraestructura central que DEBE estar completa antes de implementar CUALQUIER historia de usuario

**⚠️ CRÍTICO**: Ninguna historia de usuario puede empezar hasta terminar esta fase

Ejemplos de tareas de fundamentos (ajusta según el módulo):

- [ ] T004 Escribir la migración Flyway del esquema (catálogo o clínica)
- [ ] T005 [P] Aplicar `@PreAuthorize` y la verificación de función de plan
- [ ] T006 [P] Definir el contrato OpenAPI del módulo en `docs/api/v1/`
- [ ] T007 Crear las entidades base de las que dependen todas las historias
- [ ] T008 Configurar el manejo de errores con el sobre `{"success", "message", "data"}`

**Punto de control**: Fundamentos listos; ya puede empezar la implementación de las historias

---

## Fase 3: Historia de usuario 1 - [Título] (Prioridad: P1) 🎯

**Objetivo**: [Descripción breve de lo que entrega esta historia]

**Prueba independiente**: [Cómo verificar que esta historia funciona por sí sola]

### Pruebas de la historia de usuario 1 ⚠️

> **NOTA: Escribe estas pruebas PRIMERO y comprueba que FALLAN antes de implementar**

- [ ] T010 [P] [US1] Prueba de aislamiento entre dos clínicas en backend/src/test/java/com/vetcat/[modulo]/[Nombre]AislamientoTest.java
- [ ] T011 [P] [US1] Prueba de autorización por rol (401 sin token, 403 con rol equivocado) en backend/src/test/java/com/vetcat/[modulo]/[Nombre]AutorizacionTest.java

### Implementación de la historia de usuario 1

- [ ] T012 [P] [US1] Crear la entidad [Entidad1] en backend/src/main/java/com/vetcat/[modulo]/entity/[Entidad1].java
- [ ] T013 [P] [US1] Crear los DTO [Entidad1]Request y [Entidad1]Response en backend/src/main/java/com/vetcat/[modulo]/dto/
- [ ] T014 [US1] Implementar [Servicio] en backend/src/main/java/com/vetcat/[modulo]/service/ (depende de T012, T013)
- [ ] T015 [US1] Implementar el controlador en backend/src/main/java/com/vetcat/[modulo]/controller/
- [ ] T016 [US1] Agregar validación de entrada y manejo de errores

**Punto de control**: Aquí la historia de usuario 1 debe funcionar por completo y poder probarse de forma independiente

---

## Fase 4: Historia de usuario 2 - [Título] (Prioridad: P2)

**Objetivo**: [Descripción breve de lo que entrega esta historia]

**Prueba independiente**: [Cómo verificar que esta historia funciona por sí sola]

### Pruebas de la historia de usuario 2 ⚠️

- [ ] T018 [P] [US2] Prueba de contrato de [endpoint] en backend/src/test/java/com/vetcat/[modulo]/
- [ ] T019 [P] [US2] Prueba de integración del recorrido en backend/src/test/java/com/vetcat/[modulo]/

### Implementación de la historia de usuario 2

- [ ] T020 [P] [US2] Crear la entidad [Entidad] en backend/src/main/java/com/vetcat/[modulo]/entity/
- [ ] T021 [US2] Implementar [Servicio] en backend/src/main/java/com/vetcat/[modulo]/service/
- [ ] T022 [US2] Implementar el endpoint en backend/src/main/java/com/vetcat/[modulo]/controller/
- [ ] T023 [US2] Integrar con los componentes de la historia 1 (si hace falta)

**Punto de control**: Aquí las historias 1 Y 2 deben funcionar de forma independiente

---

## Fase 5: Historia de usuario 3 - [Título] (Prioridad: P3)

**Objetivo**: [Descripción breve de lo que entrega esta historia]

**Prueba independiente**: [Cómo verificar que esta historia funciona por sí sola]

### Pruebas de la historia de usuario 3 ⚠️

- [ ] T024 [P] [US3] Prueba de contrato de [endpoint] en backend/src/test/java/com/vetcat/[modulo]/
- [ ] T025 [P] [US3] Prueba de integración del recorrido en backend/src/test/java/com/vetcat/[modulo]/

### Implementación de la historia de usuario 3

- [ ] T026 [P] [US3] Crear la entidad [Entidad] en backend/src/main/java/com/vetcat/[modulo]/entity/
- [ ] T027 [US3] Implementar [Servicio] en backend/src/main/java/com/vetcat/[modulo]/service/
- [ ] T028 [US3] Implementar el endpoint en backend/src/main/java/com/vetcat/[modulo]/controller/

**Punto de control**: Todas las historias deben funcionar ya de forma independiente

---

[Agrega más fases de historias de usuario según haga falta, con el mismo patrón]

---

## Fase N: Acabado y asuntos transversales

**Propósito**: Mejoras que afectan a varias historias de usuario

- [ ] TXXX [P] Actualizar la documentación en docs/
- [ ] TXXX Limpieza y refactorización del código
- [ ] TXXX [P] Pruebas unitarias adicionales en backend/src/test/java/com/vetcat/[modulo]/
- [ ] TXXX Endurecimiento de seguridad
- [ ] TXXX Ejecutar la validación de quickstart.md

---

## Dependencias y orden de ejecución

### Dependencias entre fases

- **Preparación (Fase 1)**: Sin dependencias; puede empezar de inmediato
- **Fundamentos (Fase 2)**: Depende de terminar la Preparación y BLOQUEA todas las historias
- **Historias de usuario (Fase 3 en adelante)**: Todas dependen de terminar los Fundamentos
  - Después, las historias pueden avanzar en paralelo (si hay personal)
  - O de forma secuencial por prioridad (P1 → P2 → P3)
- **Acabado (última fase)**: Depende de terminar las historias deseadas

### Dependencias entre historias de usuario

- **Historia 1 (P1)**: Puede empezar tras los Fundamentos (Fase 2); no depende de otras historias
- **Historia 2 (P2)**: Puede empezar tras los Fundamentos (Fase 2); puede integrarse con la historia 1, pero debe poder probarse por sí sola
- **Historia 3 (P3)**: Puede empezar tras los Fundamentos (Fase 2); puede integrarse con las historias 1 y 2, pero debe poder probarse por sí sola

### Dentro de cada historia de usuario

- Las pruebas DEBEN escribirse y FALLAR antes de implementar
- Entidades antes que servicios
- Servicios antes que endpoints
- Implementación central antes que integración
- Termina la historia antes de pasar a la siguiente prioridad

### Oportunidades de paralelismo

- Todas las tareas de Preparación marcadas [P] pueden ejecutarse en paralelo
- Todas las tareas de Fundamentos marcadas [P] pueden ejecutarse en paralelo (dentro de la Fase 2)
- Al terminar los Fundamentos, todas las historias pueden empezar en paralelo (si la capacidad del equipo lo permite)
- Todas las pruebas de una historia marcadas [P] pueden ejecutarse en paralelo
- Las entidades de una historia marcadas [P] pueden ejecutarse en paralelo
- Distintas historias pueden trabajarlas distintas personas del equipo

---

## Ejemplo de paralelismo: Historia de usuario 1

```bash
# Lanzar juntas todas las pruebas de la historia de usuario 1:
Tarea: "Prueba de aislamiento entre dos clínicas en backend/src/test/java/..."
Tarea: "Prueba de autorización por rol en backend/src/test/java/..."

# Lanzar juntas todas las entidades de la historia de usuario 1:
Tarea: "Crear la entidad [Entidad1] en backend/src/main/java/..."
Tarea: "Crear los DTO [Entidad1]Request y [Entidad1]Response en backend/src/main/java/..."
```

---

## Estrategia de implementación

### Primero la historia P1

1. Completar la Fase 1: Preparación
2. Completar la Fase 2: Fundamentos (CRÍTICO: bloquea todas las historias)
3. Completar la Fase 3: Historia de usuario 1
4. **DETENERSE Y VALIDAR**: probar la historia 1 de forma independiente
5. Abrir el PR y mostrarla si está lista

### Entrega incremental

1. Completar Preparación y Fundamentos → base lista
2. Agregar la historia 1 → probar de forma independiente → PR
3. Agregar la historia 2 → probar de forma independiente → PR
4. Agregar la historia 3 → probar de forma independiente → PR
5. Cada historia aporta valor sin romper las anteriores

### Estrategia para varias personas

Con varias personas desarrollando:

1. El equipo completa juntos la Preparación y los Fundamentos
2. Una vez listos los Fundamentos:
   - Persona A: historia de usuario 1
   - Persona B: historia de usuario 2
   - Persona C: historia de usuario 3
3. Las historias se completan e integran de forma independiente. Cada PR respeta el tope de 400 líneas efectivas.

---

## Notas

- Las tareas [P] son de archivos distintos y sin dependencias
- La etiqueta [Historia] asocia la tarea con una historia concreta para trazabilidad
- Cada historia debe poder completarse y probarse de forma independiente
- Verifica que las pruebas fallen antes de implementar
- Haz commit después de cada tarea o grupo lógico, con el pie `Refs #N`
- Detente en cualquier punto de control para validar la historia de forma independiente
- Evita: tareas vagas, conflictos de archivos y dependencias entre historias que rompan su independencia
