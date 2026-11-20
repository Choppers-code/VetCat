# Plan de implementaci├│n: [FUNCIONALIDAD]

**Rama**: `[###-nombre-de-la-funcionalidad]` | **Fecha**: [FECHA] | **Especificaci├│n**: [enlace]

**Entrada**: Especificaci├│n de la funcionalidad en `/specs/[###-funcionalidad]/spec.md`

**Nota**: Esta plantilla la completa el comando `/speckit-plan` (en OpenCode, `/speckit.plan`); su definici├│n describe el flujo de ejecuci├│n.

## Resumen

[Extrae de la especificaci├│n: el requisito principal y el enfoque t├®cnico de la investigaci├│n]

## Contexto t├®cnico

<!--
  ACCI├ôN REQUERIDA: Reemplaza el contenido de esta secci├│n con los detalles t├®cnicos
  del proyecto. La estructura se presenta solo como gu├¡a para la iteraci├│n.
  En VetCat el contexto base est├í en docs/Tecnologias_y_patrones.md y en los ADR.
-->

**Lenguaje/Versi├│n**: [por ejemplo, Java 25 o Dart/Flutter, o NEEDS CLARIFICATION]

**Dependencias principales**: [por ejemplo, Spring Boot, Spring Security o NEEDS CLARIFICATION]

**Almacenamiento**: [si aplica, por ejemplo PostgreSQL 17 (esquema de cl├¡nica o `catalogo`) o N/A]

**Pruebas**: [por ejemplo, JUnit 5 y Testcontainers, flutter test o NEEDS CLARIFICATION]

**Plataforma de destino**: [por ejemplo, API REST, app Android, consola web o NEEDS CLARIFICATION]

**Tipo de proyecto**: [por ejemplo, backend, frontend, ambos o NEEDS CLARIFICATION]

**Metas de rendimiento**: [propias del dominio o NEEDS CLARIFICATION]

**Restricciones**: [propias del dominio, por ejemplo aislamiento por cl├¡nica, funci├│n de plan requerida o NEEDS CLARIFICATION]

**Escala/Alcance**: [propios del dominio o NEEDS CLARIFICATION]

## Verificaci├│n de la constituci├│n

*PUERTA: debe pasar antes de la investigaci├│n de la Fase 0. Se vuelve a verificar tras el dise├▒o de la Fase 1.*

[Las puertas se determinan a partir de `.specify/memory/constitution.md`]

## Estructura del proyecto

### Documentaci├│n (esta funcionalidad)

```text
specs/[###-funcionalidad]/
Ôö£ÔöÇÔöÇ plan.md              # Este archivo (salida del comando /speckit-plan)
Ôö£ÔöÇÔöÇ research.md          # Salida de la Fase 0 (comando /speckit-plan)
Ôö£ÔöÇÔöÇ data-model.md        # Salida de la Fase 1 (comando /speckit-plan)
Ôö£ÔöÇÔöÇ quickstart.md        # Salida de la Fase 1 (comando /speckit-plan)
Ôö£ÔöÇÔöÇ contracts/           # Salida de la Fase 1 (comando /speckit-plan)
ÔööÔöÇÔöÇ tasks.md             # Salida de la Fase 2 (comando /speckit-tasks, NO lo crea /speckit-plan)
```

### C├│digo fuente (ra├¡z del repositorio)
<!--
  ACCI├ôN REQUERIDA: Reemplaza el ├írbol de ejemplo con la estructura concreta
  de esta funcionalidad, con rutas reales (por ejemplo
  backend/src/main/java/com/vetcat/citas/ o frontend/apps/vetcat_app/lib/features/).
  Respeta docs/Estructura_proyecto.md. El plan entregado no debe incluir
  etiquetas de "Opci├│n".
-->

```text
# [QUITAR SI NO SE USA] Backend (paquete por m├│dulo, bajo com.vetcat)
backend/
ÔööÔöÇÔöÇ src/
    Ôö£ÔöÇÔöÇ main/java/com/vetcat/[modulo]/
    Ôöé   Ôö£ÔöÇÔöÇ controller/
    Ôöé   Ôö£ÔöÇÔöÇ service/
    Ôöé   Ôö£ÔöÇÔöÇ repository/
    Ôöé   Ôö£ÔöÇÔöÇ entity/
    Ôöé   ÔööÔöÇÔöÇ dto/
    ÔööÔöÇÔöÇ test/java/com/vetcat/[modulo]/

# [QUITAR SI NO SE USA] Migraciones
database/
ÔööÔöÇÔöÇ migrations/
    Ôö£ÔöÇÔöÇ catalogo/
    ÔööÔöÇÔöÇ tenant/

# [QUITAR SI NO SE USA] Frontend Flutter
frontend/
Ôö£ÔöÇÔöÇ apps/
Ôöé   Ôö£ÔöÇÔöÇ vetcat_app/
Ôöé   ÔööÔöÇÔöÇ vetcat_admin/
ÔööÔöÇÔöÇ packages/
    Ôö£ÔöÇÔöÇ core/
    ÔööÔöÇÔöÇ ui/
```

**Decisi├│n de estructura**: [Documenta la estructura elegida y referencia los
directorios reales capturados arriba]

## Seguimiento de complejidad

> **Completar SOLO si la Verificaci├│n de la constituci├│n tiene violaciones que deban justificarse**

| Violaci├│n | Por qu├® es necesaria | Alternativa m├ís simple descartada porque |
|-----------|----------------------|------------------------------------------|
| [por ejemplo, una librer├¡a nueva] | [necesidad actual] | [por qu├® no basta lo que ya hay en docs/] |
| [por ejemplo, un patr├│n nuevo] | [problema concreto] | [por qu├® no basta el patr├│n existente] |
