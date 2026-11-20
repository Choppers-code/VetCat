# Plan de implementación: [FUNCIONALIDAD]

**Rama**: `[###-nombre-de-la-funcionalidad]` | **Fecha**: [FECHA] | **Especificación**: [enlace]

**Entrada**: Especificación de la funcionalidad en `/specs/[###-funcionalidad]/spec.md`

**Nota**: Esta plantilla la completa el comando `/speckit-plan` (en OpenCode, `/speckit.plan`); su definición describe el flujo de ejecución.

## Resumen

[Extrae de la especificación: el requisito principal y el enfoque técnico de la investigación]

## Contexto técnico

<!--
  ACCIÓN REQUERIDA: Reemplaza el contenido de esta sección con los detalles técnicos
  del proyecto. La estructura se presenta solo como guía para la iteración.
  En VetCat el contexto base está en docs/Tecnologias_y_patrones.md y en los ADR.
-->

**Lenguaje/Versión**: [por ejemplo, Java 25 o Dart/Flutter, o NEEDS CLARIFICATION]

**Dependencias principales**: [por ejemplo, Spring Boot, Spring Security o NEEDS CLARIFICATION]

**Almacenamiento**: [si aplica, por ejemplo PostgreSQL 17 (esquema de clínica o `catalogo`) o N/A]

**Pruebas**: [por ejemplo, JUnit 5 y Testcontainers, flutter test o NEEDS CLARIFICATION]

**Plataforma de destino**: [por ejemplo, API REST, app Android, consola web o NEEDS CLARIFICATION]

**Tipo de proyecto**: [por ejemplo, backend, frontend, ambos o NEEDS CLARIFICATION]

**Metas de rendimiento**: [propias del dominio o NEEDS CLARIFICATION]

**Restricciones**: [propias del dominio, por ejemplo aislamiento por clínica, función de plan requerida o NEEDS CLARIFICATION]

**Escala/Alcance**: [propios del dominio o NEEDS CLARIFICATION]

## Verificación de la constitución

*PUERTA: debe pasar antes de la investigación de la Fase 0. Se vuelve a verificar tras el diseño de la Fase 1.*

[Las puertas se determinan a partir de `.specify/memory/constitution.md`]

## Estructura del proyecto

### Documentación (esta funcionalidad)

```text
specs/[###-funcionalidad]/
├── plan.md              # Este archivo (salida del comando /speckit-plan)
├── research.md          # Salida de la Fase 0 (comando /speckit-plan)
├── data-model.md        # Salida de la Fase 1 (comando /speckit-plan)
├── quickstart.md        # Salida de la Fase 1 (comando /speckit-plan)
├── contracts/           # Salida de la Fase 1 (comando /speckit-plan)
└── tasks.md             # Salida de la Fase 2 (comando /speckit-tasks, NO lo crea /speckit-plan)
```

### Código fuente (raíz del repositorio)
<!--
  ACCIÓN REQUERIDA: Reemplaza el árbol de ejemplo con la estructura concreta
  de esta funcionalidad, con rutas reales (por ejemplo
  backend/src/main/java/com/vetcat/citas/ o frontend/apps/vetcat_app/lib/features/).
  Respeta docs/Estructura_proyecto.md. El plan entregado no debe incluir
  etiquetas de "Opción".
-->

```text
# [QUITAR SI NO SE USA] Backend (paquete por módulo, bajo com.vetcat)
backend/
└── src/
    ├── main/java/com/vetcat/[modulo]/
    │   ├── controller/
    │   ├── service/
    │   ├── repository/
    │   ├── entity/
    │   └── dto/
    └── test/java/com/vetcat/[modulo]/

# [QUITAR SI NO SE USA] Migraciones
database/
└── migrations/
    ├── catalogo/
    └── tenant/

# [QUITAR SI NO SE USA] Frontend Flutter
frontend/
├── apps/
│   ├── vetcat_app/
│   └── vetcat_admin/
└── packages/
    ├── core/
    └── ui/
```

**Decisión de estructura**: [Documenta la estructura elegida y referencia los
directorios reales capturados arriba]

## Seguimiento de complejidad

> **Completar SOLO si la Verificación de la constitución tiene violaciones que deban justificarse**

| Violación | Por qué es necesaria | Alternativa más simple descartada porque |
|-----------|----------------------|------------------------------------------|
| [por ejemplo, una librería nueva] | [necesidad actual] | [por qué no basta lo que ya hay en docs/] |
| [por ejemplo, un patrón nuevo] | [problema concreto] | [por qué no basta el patrón existente] |
