# ADR-0001 · git flow para empezar, PR para terminar

| | |
|---|---|
| **Estado** | Aceptada |
| **Fecha** | 2026-11-12 |
| **Área** | 1 · Arquitectura |
| **Issue** | #26 |
| **Decide** | Se usa `git flow` para crear ramas; ninguna rama se termina con `git flow feature finish` |

## Contexto

VetCat usa Git Flow como mapa de ramas: `main` publica, `develop` integra, `feature/*` trabaja, `release/*` corta versión y `hotfix/*` parchea. Esa parte no se discute aquí.

La discusión es otra: **qué termina una rama**. La herramienta `git flow feature finish` fusiona en `develop` en la máquina de cada uno. El repositorio, en cambio, exige que todo cambio entre por un pull request aprobado, con un ruleset que no tiene bypass ni para administradores.

Las dos cosas no pueden ser ciertas a la vez.

## El problema, paso a paso

```powershell
git flow feature finish 24-reglas-de-colaboracion
```

1. Se cambia a `develop` y ejecuta `git merge --no-ff`.
2. Borra la rama local.
3. `develop` local queda con un commit que el servidor no tiene.
4. `git push origin develop` → rechazado por el ruleset.
5. Para salir del estado: `git reset --hard origin/develop`, y recuperar la rama desde el remoto o el reflog.

El repositorio remoto nunca corre riesgo: el ruleset lo impide. Lo que se rompe es la copia local, y el rescate se hace con `reset --hard`, el único comando de Git que borra trabajo sin preguntar.

## Por qué `finish` tenía sentido y ya no

Cuando Vincent Driessen publicó el modelo en 2010 no existían los pull requests. No había revisión obligatoria, ni CI por rama, ni protección de ramas. El merge **era** el momento de integración, y `develop` una rama compartida donde todos publicaban. En ese mundo `finish` era la única forma de integrar, y la disciplina era humana.

Cuatro cambios lo dejaron atrás:

1. **El PR existe.** La revisión dejó de ser una cortesía y pasó a tener estado: pedida, aprobada, pendiente.
2. **El CI corre antes del merge.** Con `finish` corre después: el código ya está en `develop` y el fallo es de todos.
3. **El squash existe.** Un commit por issue; `git log` se lee como un changelog y revertir es un comando.
4. **El volumen cambió.** Con agentes de IA el código se produce más rápido de lo que se lee. El cuello de botella se movió de escribir a revisar.

El propio autor añadió años después una nota a su artículo: para equipos que entregan software de forma continua, recomienda un flujo más simple que git-flow en vez de forzarlo. VetCat es una aplicación web que se despliega cuando está lista.

## Qué se pierde si `finish` entra en el flujo

| Situación | Con `finish` | Con PR |
|---|---|---|
| El CI falla | Ya está en `develop`; todos parten de una base rota | El PR se queda rojo y `develop` sigue verde |
| Un agente mete 600 líneas en `citas/` | Entra. Nadie lo leyó | Alguien lo lee, o se devuelve |
| Falta un `@PreAuthorize` | Se descubre en producción, con datos de dos clínicas mezclados | Lo ve el revisor o falla la prueba de autorización |
| Hay que revertir | `git revert -m 1`, que duele | `git revert <sha>` |
| Dos personas terminan a la vez | Conflicto resuelto por quien empuja último, sin revisión | El segundo PR se rebasa |

VetCat maneja expedientes clínicos, vacunas y dinero. Un fallo de aislamiento entre clínicas no es un bug de interfaz: es que una clínica ve los perros de otra. Eso no lo atrapa un merge local; lo atrapa una prueba que alguien escribió y un revisor que la buscó.

## Decisión

1. **`scripts/nueva-rama.ps1 -Issue <n> -Slug <slug>`** crea `feature/<n>-<slug>` desde `develop` actualizado. Es el camino por defecto porque viene en el clon: no hay que instalar nada. `git flow feature start <n>-<slug>` produce la misma rama y sigue siendo válido para quien lo tenga instalado.
2. **`scripts/terminar-rama.ps1 -Issue <n>`** termina la rama: la sube, abre el PR contra `develop` si no existe, fusiona con squash, borra la rama y deja `develop` actualizado. El `Closes #<n>` sale del nombre de la rama.
3. **`git flow feature finish` no se usa.** Tampoco `release finish` ni `hotfix finish`.
4. **El ruleset no se toca.**

El script se comporta como `finish` para quien lo usa —una orden, la rama desaparece, `develop` al día— pero el merge lo hace GitHub, con la aprobación y el CI delante.

Que las ramas se creen con un script y no con una herramienta instalada a mano es parte de la misma decisión: lo que vive en el repositorio se puede exigir y se puede comprobar; lo que vive en cada máquina, no.

## Alternativas consideradas

| Alternativa | Por qué se descartó |
|---|---|
| git flow completo, con `finish` y push a `develop` | Exige permitir push directo a `develop`, quitar la aprobación obligatoria y el «sin bypass». CODEOWNERS deja de ejecutarse y el repositorio público pierde su razón de ser: se hizo público para tener protección de ramas gratis |
| GitHub Flow puro, sin `develop` | Es válido para SaaS, pero cambia el modelo de releases por milestone que ya está documentado y en uso |
| Sin `git flow`: `git switch -c` a mano | Funciona, pero el nombre de la rama depende de la memoria de cada uno. El script fija el formato sin instalar nada |
| Un alias de git en vez de un script | Los alias viven en `.git/config`: no se versionan ni se comparten. El script sí |
| Dejar `finish` y arreglar después | El estado roto es local y silencioso; el que lo sufre no sabe por qué su `push` falla |

## Consecuencias

### Se gana

- `develop` siempre compila: nada entra sin CI en verde.
- Todo cambio tiene un revisor con nombre.
- Un commit por issue; revertir es un comando.
- El historial no depende de la disciplina de nadie.

### Se paga

- Terminar una rama espera una aprobación: minutos, no segundos.
- Quien venga de git flow clásico tiene que dejar de escribir `finish`.
- Hay que mantener un script propio.

### Si alguien ejecuta `finish` por error

```powershell
git switch develop
git reset --hard origin/develop
git branch feature/<issue>-<slug> origin/feature/<issue>-<slug>
```

Si la rama se subió antes, nada se pierde. Si nunca se subió, el reflog la conserva:

```powershell
git reflog | Select-String "feature/"
```

## Cómo se hace cumplir

| Mecanismo | Qué garantiza | Estado |
|---|---|---|
| Ruleset `ramas protegidas` | No hay push directo a `develop` ni a `main`, sin bypass | Activo |
| CODEOWNERS | El PR pide revisión al equipo del área | Activo |
| CI del nombre de rama | La rama se llama `feature/<issue>-<slug>` o el PR no pasa | Activo, `.github/workflows/ramas.yml` |

## Referencias

- `docs/Flujo_de_trabajo.md` — el flujo operativo.
- `docs/Equipo_responsabilidades.md` — quién aprueba y quién responde.
- Vincent Driessen, *A successful Git branching model* (2010), y su nota posterior sobre entrega continua.
