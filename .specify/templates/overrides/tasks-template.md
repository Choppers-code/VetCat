---

description: "Plantilla de lista de tareas para implementar una funcionalidad"
---

# Tareas: [NOMBRE DE LA FUNCIONALIDAD]

**Entrada**: Documentos de dise├▒o en `/specs/[###-funcionalidad]/`

**Requisitos previos**: plan.md (obligatorio), spec.md (obligatorio para las historias de usuario), research.md, data-model.md, contracts/

**Pruebas**: Los ejemplos de abajo incluyen tareas de prueba. En VetCat las pruebas son obligatorias (constituci├│n, principio IV): aislamiento entre cl├¡nicas, autorizaci├│n por rol y concurrencia van primero.

**Organizaci├│n**: Las tareas se agrupan por historia de usuario para poder implementar y probar cada una de forma independiente.

## Formato: `[ID] [P?] [Historia] Descripci├│n`

- **[P]**: Se puede ejecutar en paralelo (archivos distintos, sin dependencias)
- **[Historia]**: A qu├® historia de usuario pertenece la tarea (por ejemplo, US1, US2, US3)
- Incluye las rutas exactas de los archivos en las descripciones

## Convenciones de rutas

- **Backend**: `backend/src/main/java/com/vetcat/<modulo>/` y `backend/src/test/java/com/vetcat/<modulo>/`
- **Migraciones**: `database/migrations/catalogo/` y `database/migrations/tenant/`
- **Frontend**: `frontend/apps/` y `frontend/packages/`
- Ajusta las rutas seg├║n la estructura de plan.md y de `docs/Estructura_proyecto.md`

<!--
  ============================================================================
  IMPORTANTE: Las tareas de abajo son EJEMPLOS ilustrativos.

  El comando /speckit-tasks DEBE reemplazarlas con tareas reales seg├║n:
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

## Fase 1: Preparaci├│n (infraestructura compartida)

**Prop├│sito**: Inicializaci├│n del trabajo y estructura b├ísica

- [ ] T001 Crear la estructura del m├│dulo seg├║n el plan de implementaci├│n
- [ ] T002 Crear la rama de trabajo con `scripts/nueva-rama.ps1` y enlazar el issue
- [ ] T003 [P] Configurar las reglas de an├ílisis y formato del m├│dulo

---

## Fase 2: Fundamentos (requisitos bloqueantes)

**Prop├│sito**: Infraestructura central que DEBE estar completa antes de implementar CUALQUIER historia de usuario

**ÔÜá´©Å CR├ìTICO**: Ninguna historia de usuario puede empezar hasta terminar esta fase

Ejemplos de tareas de fundamentos (ajusta seg├║n el m├│dulo):

- [ ] T004 Escribir la migraci├│n Flyway del esquema (cat├ílogo o cl├¡nica)
- [ ] T005 [P] Aplicar `@PreAuthorize` y la verificaci├│n de funci├│n de plan
- [ ] T006 [P] Definir el contrato OpenAPI del m├│dulo en `docs/api/v1/`
- [ ] T007 Crear las entidades base de las que dependen todas las historias
- [ ] T008 Configurar el manejo de errores con el sobre `{"success", "message", "data"}`

**Punto de control**: Fundamentos listos; ya puede empezar la implementaci├│n de las historias

---

## Fase 3: Historia de usuario 1 - [T├¡tulo] (Prioridad: P1) ­ƒÄ»

**Objetivo**: [Descripci├│n breve de lo que entrega esta historia]

**Prueba independiente**: [C├│mo verificar que esta historia funciona por s├¡ sola]

### Pruebas de la historia de usuario 1 ÔÜá´©Å

> **NOTA: Escribe estas pruebas PRIMERO y comprueba que FALLAN antes de implementar**

- [ ] T010 [P] [US1] Prueba de aislamiento entre dos cl├¡nicas en backend/src/test/java/com/vetcat/[modulo]/[Nombre]AislamientoTest.java
- [ ] T011 [P] [US1] Prueba de autorizaci├│n por rol (401 sin token, 403 con rol equivocado) en backend/src/test/java/com/vetcat/[modulo]/[Nombre]AutorizacionTest.java

### Implementaci├│n de la historia de usuario 1

- [ ] T012 [P] [US1] Crear la entidad [Entidad1] en backend/src/main/java/com/vetcat/[modulo]/entity/[Entidad1].java
- [ ] T013 [P] [US1] Crear los DTO [Entidad1]Request y [Entidad1]Response en backend/src/main/java/com/vetcat/[modulo]/dto/
- [ ] T014 [US1] Implementar [Servicio] en backend/src/main/java/com/vetcat/[modulo]/service/ (depende de T012, T013)
- [ ] T015 [US1] Implementar el controlador en backend/src/main/java/com/vetcat/[modulo]/controller/
- [ ] T016 [US1] Agregar validaci├│n de entrada y manejo de errores

**Punto de control**: Aqu├¡ la historia de usuario 1 debe funcionar por completo y poder probarse de forma independiente

---

## Fase 4: Historia de usuario 2 - [T├¡tulo] (Prioridad: P2)

**Objetivo**: [Descripci├│n breve de lo que entrega esta historia]

**Prueba independiente**: [C├│mo verificar que esta historia funciona por s├¡ sola]

### Pruebas de la historia de usuario 2 ÔÜá´©Å

- [ ] T018 [P] [US2] Prueba de contrato de [endpoint] en backend/src/test/java/com/vetcat/[modulo]/
- [ ] T019 [P] [US2] Prueba de integraci├│n del recorrido en backend/src/test/java/com/vetcat/[modulo]/

### Implementaci├│n de la historia de usuario 2

- [ ] T020 [P] [US2] Crear la entidad [Entidad] en backend/src/main/java/com/vetcat/[modulo]/entity/
- [ ] T021 [US2] Implementar [Servicio] en backend/src/main/java/com/vetcat/[modulo]/service/
- [ ] T022 [US2] Implementar el endpoint en backend/src/main/java/com/vetcat/[modulo]/controller/
- [ ] T023 [US2] Integrar con los componentes de la historia 1 (si hace falta)

**Punto de control**: Aqu├¡ las historias 1 Y 2 deben funcionar de forma independiente

---

## Fase 5: Historia de usuario 3 - [T├¡tulo] (Prioridad: P3)

**Objetivo**: [Descripci├│n breve de lo que entrega esta historia]

**Prueba independiente**: [C├│mo verificar que esta historia funciona por s├¡ sola]

### Pruebas de la historia de usuario 3 ÔÜá´©Å

- [ ] T024 [P] [US3] Prueba de contrato de [endpoint] en backend/src/test/java/com/vetcat/[modulo]/
- [ ] T025 [P] [US3] Prueba de integraci├│n del recorrido en backend/src/test/java/com/vetcat/[modulo]/

### Implementaci├│n de la historia de usuario 3

- [ ] T026 [P] [US3] Crear la entidad [Entidad] en backend/src/main/java/com/vetcat/[modulo]/entity/
- [ ] T027 [US3] Implementar [Servicio] en backend/src/main/java/com/vetcat/[modulo]/service/
- [ ] T028 [US3] Implementar el endpoint en backend/src/main/java/com/vetcat/[modulo]/controller/

**Punto de control**: Todas las historias deben funcionar ya de forma independiente

---

[Agrega m├ís fases de historias de usuario seg├║n haga falta, con el mismo patr├│n]

---

## Fase N: Acabado y asuntos transversales

**Prop├│sito**: Mejoras que afectan a varias historias de usuario

- [ ] TXXX [P] Actualizar la documentaci├│n en docs/
- [ ] TXXX Limpieza y refactorizaci├│n del c├│digo
- [ ] TXXX [P] Pruebas unitarias adicionales en backend/src/test/java/com/vetcat/[modulo]/
- [ ] TXXX Endurecimiento de seguridad
- [ ] TXXX Ejecutar la validaci├│n de quickstart.md

---

## Dependencias y orden de ejecuci├│n

### Dependencias entre fases

- **Preparaci├│n (Fase 1)**: Sin dependencias; puede empezar de inmediato
- **Fundamentos (Fase 2)**: Depende de terminar la Preparaci├│n y BLOQUEA todas las historias
- **Historias de usuario (Fase 3 en adelante)**: Todas dependen de terminar los Fundamentos
  - Despu├®s, las historias pueden avanzar en paralelo (si hay personal)
  - O de forma secuencial por prioridad (P1 ÔåÆ P2 ÔåÆ P3)
- **Acabado (├║ltima fase)**: Depende de terminar las historias deseadas

### Dependencias entre historias de usuario

- **Historia 1 (P1)**: Puede empezar tras los Fundamentos (Fase 2); no depende de otras historias
- **Historia 2 (P2)**: Puede empezar tras los Fundamentos (Fase 2); puede integrarse con la historia 1, pero debe poder probarse por s├¡ sola
- **Historia 3 (P3)**: Puede empezar tras los Fundamentos (Fase 2); puede integrarse con las historias 1 y 2, pero debe poder probarse por s├¡ sola

### Dentro de cada historia de usuario

- Las pruebas DEBEN escribirse y FALLAR antes de implementar
- Entidades antes que servicios
- Servicios antes que endpoints
- Implementaci├│n central antes que integraci├│n
- Termina la historia antes de pasar a la siguiente prioridad

### Oportunidades de paralelismo

- Todas las tareas de Preparaci├│n marcadas [P] pueden ejecutarse en paralelo
- Todas las tareas de Fundamentos marcadas [P] pueden ejecutarse en paralelo (dentro de la Fase 2)
- Al terminar los Fundamentos, todas las historias pueden empezar en paralelo (si la capacidad del equipo lo permite)
- Todas las pruebas de una historia marcadas [P] pueden ejecutarse en paralelo
- Las entidades de una historia marcadas [P] pueden ejecutarse en paralelo
- Distintas historias pueden trabajarlas distintas personas del equipo

---

## Ejemplo de paralelismo: Historia de usuario 1

```bash
# Lanzar juntas todas las pruebas de la historia de usuario 1:
Tarea: "Prueba de aislamiento entre dos cl├¡nicas en backend/src/test/java/..."
Tarea: "Prueba de autorizaci├│n por rol en backend/src/test/java/..."

# Lanzar juntas todas las entidades de la historia de usuario 1:
Tarea: "Crear la entidad [Entidad1] en backend/src/main/java/..."
Tarea: "Crear los DTO [Entidad1]Request y [Entidad1]Response en backend/src/main/java/..."
```

---

## Estrategia de implementaci├│n

### Primero la historia P1

1. Completar la Fase 1: Preparaci├│n
2. Completar la Fase 2: Fundamentos (CR├ìTICO: bloquea todas las historias)
3. Completar la Fase 3: Historia de usuario 1
4. **DETENERSE Y VALIDAR**: probar la historia 1 de forma independiente
5. Abrir el PR y mostrarla si est├í lista

### Entrega incremental

1. Completar Preparaci├│n y Fundamentos ÔåÆ base lista
2. Agregar la historia 1 ÔåÆ probar de forma independiente ÔåÆ PR
3. Agregar la historia 2 ÔåÆ probar de forma independiente ÔåÆ PR
4. Agregar la historia 3 ÔåÆ probar de forma independiente ÔåÆ PR
5. Cada historia aporta valor sin romper las anteriores

### Estrategia para varias personas

Con varias personas desarrollando:

1. El equipo completa juntos la Preparaci├│n y los Fundamentos
2. Una vez listos los Fundamentos:
   - Persona A: historia de usuario 1
   - Persona B: historia de usuario 2
   - Persona C: historia de usuario 3
3. Las historias se completan e integran de forma independiente. Cada PR respeta el tope de 400 l├¡neas efectivas.

---

## Notas

- Las tareas [P] son de archivos distintos y sin dependencias
- La etiqueta [Historia] asocia la tarea con una historia concreta para trazabilidad
- Cada historia debe poder completarse y probarse de forma independiente
- Verifica que las pruebas fallen antes de implementar
- Haz commit despu├®s de cada tarea o grupo l├│gico, con el pie `Refs #N`
- Detente en cualquier punto de control para validar la historia de forma independiente
- Evita: tareas vagas, conflictos de archivos y dependencias entre historias que rompan su independencia
