# Spec Kit en VetCat

Guía de uso de Spec Kit (desarrollo guiado por especificaciones) dentro del flujo de trabajo del repositorio. Spec Kit no reemplaza el flujo de [`Flujo_de_trabajo.md`](Flujo_de_trabajo.md): **vive dentro de cada issue**. Primero se especifica, se planea y se divide en tareas; después se programa.

La constitución del proyecto está en [`.specify/memory/constitution.md`](../.specify/memory/constitution.md). Las plantillas en español, en `.specify/templates/overrides/`. El plan de trabajo, en [`Plan_de_sprints.md`](Plan_de_sprints.md).

## 1. Qué hay en el repositorio

| Ruta | Para qué sirve | Quién la usa |
|---|---|---|
| `.specify/` | Constitución, plantillas, scripts y flujos de Spec Kit. Es lo compartido | Todos |
| `.specify/templates/overrides/` | Plantillas en español; tienen prioridad sobre las originales | Todos |
| `.claude/skills/speckit-*` | Comandos de Spec Kit para Claude Code | Quien use Claude Code |
| `.opencode/commands/speckit.*.md` | Comandos de Spec Kit para OpenCode | Quien use OpenCode |
| `specs/NNN-slug/` | Una carpeta por issue: `spec.md`, `plan.md`, `tasks.md`… | Todos |

Las dos carpetas de comandos conviven sin pisarse. Spec Kit no modifica `AGENTS.md`.

## 2. Preparar la máquina (una sola vez)

1. Instalar `uv` y la CLI `specify`. Este documento se verificó con la versión 1.0.13.
2. Permitir los scripts de PowerShell que usa Spec Kit. En Windows, la política por defecto los bloquea:

   ```powershell
   Set-ExecutionPolicy -Scope CurrentUser RemoteSigned
   ```

3. Comprobar que se ve el asistente con `specify check`.

No hace falta ejecutar `specify init`: el repositorio ya está inicializado con las integraciones de Claude Code y OpenCode. Para ver el estado: `specify integration list`.

## 3. Comandos según el asistente

Cada asistente invoca los mismos pasos con una sintaxis distinta:

| Paso | Claude Code | OpenCode |
|---|---|---|
| Constitución | `/speckit-constitution` | `/speckit.constitution` |
| Especificar | `/speckit-specify` | `/speckit.specify` |
| Aclarar (opcional) | `/speckit-clarify` | `/speckit.clarify` |
| Planear | `/speckit-plan` | `/speckit.plan` |
| Tareas | `/speckit-tasks` | `/speckit.tasks` |
| Analizar (opcional) | `/speckit-analyze` | `/speckit.analyze` |
| Lista de verificación (opcional) | `/speckit-checklist` | `/speckit.checklist` |
| Implementar | `/speckit-implement` | `/speckit.implement` |
| Crear issues desde las tareas | `/speckit-taskstoissues` | `/speckit.taskstoissues` |

**Integración por defecto.** El archivo `.specify/integration.json` guarda `claude` como integración por defecto. No hace falta cambiarla para trabajar con OpenCode: cada asistente usa su propia carpeta de comandos. Si por algún motivo cambias ese archivo en tu máquina, **no lo incluyas en el commit**.

## 4. El flujo de un issue

1. **Issue.** Existe en el tablero, con responsable, fase y sprint. Sin issue no hay rama.
2. **Rama.** `scripts/nueva-rama.ps1 -Issue <n> -Slug <slug>`. Spec Kit no crea la rama de git, solo la carpeta de la especificación.
3. **Especificar.** `/speckit-specify` (o `/speckit.specify`) con la descripción del issue y los IDs de los requerimientos que cubre. La carpeta debe llevar el número del issue (`specs/NNN-slug/`, con tres cifras). Spec Kit numera por su cuenta en orden secuencial; para forzar el número del issue, el script `create-new-feature.ps1` acepta `-Number`, y si la carpeta salió con otro número hay que renombrarla antes del commit.
4. **Aclarar.** Si la especificación trae marcas `[NEEDS CLARIFICATION]`, se resuelven con `/speckit-clarify` o preguntando al dueño del módulo. No se planea con dudas abiertas.
5. **Planear.** Genera `plan.md`, `research.md`, `data-model.md`, `quickstart.md` y `contracts/`. El plan pasa la **Verificación de la constitución**; una complejidad añadida se justifica en su tabla.
6. **Tareas.** Genera `tasks.md`, agrupado por historia de usuario. Las pruebas de aislamiento, autorización y concurrencia van primero.
7. **Analizar.** Opcional, pero recomendado en módulos de seguridad o dinero: revisa la coherencia entre especificación, plan y tareas.
8. **Implementar.** Se programa por tareas. Cada commit lleva el pie `Refs #N`.
9. **PR.** Un PR por issue, máximo 400 líneas efectivas, con `Closes #N`. Si el issue es mayor, se parte en varios issues desde el paso 3.

La carpeta `specs/NNN-slug/` se sube en el mismo PR que el código del issue, o en uno anterior si la especificación se revisa aparte. El tope de 400 líneas efectivas se aplica al PR completo; si la especificación lo desborda, va en su propio PR.

## 5. Reglas del equipo

- **La especificación se escribe en español.** Las historias de usuario van como "Como [rol], quiero [acción] para [beneficio]" y los escenarios con Dado, Cuando y Entonces. Los marcadores `[NEEDS CLARIFICATION]`, `[P]`, `[US1]` y los IDs `FR-001`, `SC-001` y `T001` no se traducen.
- **Los requerimientos mandan.** Cada especificación cita los IDs de `docs/requerimientos/`. Si algo que se necesita no está ahí, se pregunta; no se inventa.
- **El nombre de los roles** es el de los requerimientos: `super_admin`, `clinica_admin`, `veterinario`, `recepcionista` y `cliente`.
- **No se edita `.specify/` dentro de un PR de funcionalidad.** Las plantillas, la constitución y los scripts tienen un solo dueño (arquitectura) y se cambian en su propio PR.
- **No se commitea nada de configuración personal** de un asistente: ajustes locales, tokens ni preferencias.

## 6. Actualizar Spec Kit

Cuando salga una versión nueva de la CLI, se actualiza en un PR aparte, a cargo de arquitectura, con `specify integration upgrade`. Las plantillas en español están en `overrides/`, así que la actualización no las reemplaza; sí conviene comparar las originales con las nuestras para ver si cambiaron las secciones que los comandos esperan.
