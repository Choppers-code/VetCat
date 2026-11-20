# Scrum en VetCat

Cómo se encaja Scrum con lo que el repositorio ya tiene. No agrega procesos nuevos: le pone nombre de Scrum a lo que ya existe en [`Flujo_de_trabajo.md`](Flujo_de_trabajo.md) y en [`Roadmap.md`](Roadmap.md).

## 1. Equivalencias

| En Scrum | En VetCat |
|---|---|
| Épica | La **fase** del roadmap (un issue padre con sub-issues) |
| Elemento del backlog | Un **issue** del tablero, con un solo responsable |
| Sprint | Un periodo fijo de **una semana** con un conjunto de issues del tablero |
| Definición de terminado | La lista de la **plantilla de PR** (`.github/pull_request_template.md`) |
| Incremento | Lo que se fusiona en `develop` por PR |
| Revisión del sprint | Se hace con el tablero: lo que llegó a *Hecho* |
| Retrospectiva | Se hace con el tablero: lo que se quedó en *En curso* o *En revisión* y por qué |

## 2. Roles

| Rol | Persona |
|---|---|
| Product Owner | Raul Francisco Amador |
| Scrum Master | Luis Antonio De La Cruz Hernandez |
| Equipo de desarrollo | Las cinco áreas de [`Equipo_responsabilidades.md`](Equipo_responsabilidades.md) |

El Product Owner ordena el backlog y decide el alcance de cada sprint. El Scrum Master cuida que el proceso se cumpla y quita bloqueos. Ninguno de los dos reemplaza al dueño de un módulo, que sigue aprobando los cambios de su área.

## 3. El sprint

- **Duración:** una semana, de domingo a sábado.
- **Un sprint no es un release.** El sprint es un periodo de trabajo; el release se corta cuando su milestone está al 100 % (`Flujo_de_trabajo.md` §6).
- El campo *Sprint* del tablero indica a qué sprint pertenece cada issue.
- Al terminar el sprint, lo que no llegó a *Hecho* pasa al siguiente sprint con una nota que diga por qué.
- El plan de los sprints está en `Plan_de_sprints.md`.

## 4. Desarrollo guiado por especificaciones

Especificar, planear y dividir en tareas se hace **dentro de cada issue**, antes de programar, con Spec Kit. La guía está en `Spec_Kit.md`.

## 5. Lo que este documento no define

No se añaden ceremonias ni artefactos que el equipo no haya acordado. Si el equipo decide añadir reuniones diarias, planificación formal u otras, se documenta aquí en un PR.
