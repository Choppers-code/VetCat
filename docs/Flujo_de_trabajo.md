# Flujo de trabajo

Cómo se mueve el trabajo en VetCat: del issue a la rama, de la rama al PR y del PR al release. Es la guía operativa del día a día; las reglas de arquitectura están en los otros documentos.

## 1. Las ramas

| Rama | Nace de | Se fusiona a | Vida |
|---|---|---|---|
| `main` | — | — | Permanente. Solo recibe releases y hotfixes |
| `develop` | `main` | — | Permanente. Integración de todo el trabajo |
| `feature/<issue>-<slug>` | `develop` | `develop` | Hasta el merge |
| `release/x.y.z` | `develop` | `main` y `develop` | Durante el corte de versión |
| `hotfix/<issue>-<slug>` | `main` | `main` y `develop` | Hasta el parche |

La rama por defecto del repositorio es **`develop`**. Un PR abierto desde la web apunta ahí, no a `main`. Es intencional: llegar a `main` exige pasar por un release.

El porqué de este mapa, y por qué `git flow` se usa solo para crear ramas y no para fusionarlas, está en [`adr/ADR-001-git-flow-sin-finish.md`](adr/ADR-001-git-flow-sin-finish.md).

## 2. El ciclo de un issue

1. El issue existe en el tablero con `Área`, `Fase` y **un solo** responsable.
2. Al empezar, se mueve a *En curso* y se crea la rama desde `develop`.
3. Los commits siguen la convención y referencian el issue.
4. Se abre el PR contra `develop` con `Closes #N`. El tablero lo mueve a *En revisión*.
5. CODEOWNERS pide revisión al equipo del área. Alguien distinto del autor aprueba.
6. Merge con *squash*. El issue se cierra y la tarjeta pasa a *Hecho*.

```powershell
git switch develop
git pull --ff-only
git switch -c feature/23-empalme-consultorio

# trabajo

git add .
git commit -m "feat(citas): bloquear empalme por consultorio" -m "Refs #23"
git push -u origin feature/23-empalme-consultorio

gh pr create --base develop `
  --title "feat(citas): bloquear empalme por consultorio" `
  --body "Closes #23"
```

## 3. Commits

Formato: `tipo(scope): descripción en español`.

| Tipo | Cuándo |
|---|---|
| `feat` | Funcionalidad nueva |
| `fix` | Corrección de un error |
| `docs` | Solo documentación |
| `refactor` | Cambio interno sin cambiar el comportamiento |
| `test` | Pruebas |
| `chore` | Configuración, dependencias, infraestructura |
| `perf` | Mejora de rendimiento |

El `scope` es el módulo: `citas`, `pacientes`, `tenancy`, `facturacion`, `frontend`, `docker`.

El pie `Refs #23` enlaza el commit con el issue. `Closes #23`, que va en el PR, es lo que lo cierra al fusionar.

## 4. Pull requests

- **Un PR, un issue.** Si toca dos áreas, probablemente sean dos PRs.
- El título repite el formato del commit; con squash, se convierte en el mensaje final.
- El cuerpo completa la plantilla: qué cambia, issue, checklist de Definition of Done y riesgo.
- No se fusiona con el CI en rojo ni con la revisión pedida pendiente.
- Método de merge: **squash**. Deja un commit por issue y evita el ruido de las ramas.
- **Tope de 400 líneas efectivas** — añadidas más borradas, sin contar archivos generados ni migraciones. Un PR más grande no se revisa de verdad: se parte antes de abrirlo.
- **Nada de refactores de paso.** Si un archivo no aparece en el issue, no va en el PR. Un diff que toca cosas que nadie pidió se devuelve.

## 5. Lo que el repositorio impide

Estas reglas están activas y sin excepciones: el ruleset no tiene bypass, ni siquiera para administradores.

| Acción | Resultado |
|---|---|
| `git push` directo a `develop` o `main` | Rechazado por el servidor |
| `git push --force` sobre esas ramas | Rechazado |
| Borrar la rama en el remoto | Rechazado |
| Fusionar un PR sin aprobación | Bloqueado |
| Fusionar sin la aprobación del equipo dueño | Bloqueado |
| Aprobar tu propio PR | Imposible: GitHub no lo permite |
| Subir commits después de la aprobación | La revisión se descarta y hay que volver a pedirla |

## 6. Releases

Un release no se corta por calendario, sino cuando su milestone está al 100 %.

| Milestone | Contenido |
|---|---|
| `v1.0` | Fases 4 a 7 |
| `v1.1` | Fase 8 |
| `v1.2` | Fase 9 |
| `v1.3` | Fase 10 |
| `v2.0` | Fase 11 |

```powershell
git switch develop
git pull --ff-only
git switch -c release/1.0.0

# solo correcciones: nada de funcionalidad nueva

git push -u origin release/1.0.0
gh pr create --base main --title "release: v1.0.0" --body "Milestone v1.0 completo"
# tras aprobar y fusionar en main:
git switch main
git pull --ff-only
git tag -a v1.0.0 -m "v1.0 — agenda, mascotas, expediente y vacunas"
git push origin v1.0.0
# devolver el corte a integración:
git switch develop
git merge --no-ff release/1.0.0
git push origin develop
```

El tag se crea en `main`, nunca en `develop`. Después se cierra el milestone.

## 7. Hotfix

Sale de `main`, no de `develop`, porque lo que hay que arreglar está en producción.

```powershell
git switch main
git pull --ff-only
git switch -c hotfix/87-folio-duplicado
# corrección
git push -u origin hotfix/87-folio-duplicado
gh pr create --base main --title "fix(facturacion): folio duplicado" --body "Closes #87"
# tras fusionar:
git tag -a v1.0.1 -m "v1.0.1 — folio duplicado"
git push origin v1.0.1
git switch develop
git merge --no-ff main
git push origin develop
```

## 8. El tablero

| Campo | Uso |
|---|---|
| Status | Backlog → Listo → En curso → En revisión → Hecho |
| Área | El equipo responsable: 1 Arquitectura, 2 Pacientes, 3 Operación, 4 Frontend, 5 Infraestructura |
| Fase | La fase del roadmap a la que pertenece |
| Tamaño | S, M, L. Una tarjeta `L` se divide antes de empezar |

- *Listo* significa criterios de aceptación escritos y, si toca la API, contrato definido. No significa "me toca".
- El responsable se asigna **a una sola persona**. Los apoyos se piden como revisores del PR, no como co-asignados.
- Los issues de una fase cuelgan de su épica como sub-issues; la épica se cierra cuando se cierran todas.

Vistas: `Tablero` (por estado), `Por persona` (carga real, filtrada a lo no cerrado) y `Por fase`.

## 9. Quién aprueba

| Área | Equipo | Miembros |
|---|---|---|
| 1 | `arquitectura` | `Anton-code-sp`, `Raul-Rolu` |
| 2 | `pacientes` | `Raul-Rolu`, `Anton-code-sp` |
| 3 | `operacion` | `ToNyyF1`, `Anton-code-sp` |
| 4 | `frontend` | `Nacho-Nacho3`, `Anton-code-sp` |
| 5 | `infraestructura` | `Carlosss1819`, `Anton-code-sp` |

Cada área tiene dos miembros justamente para que nadie necesite aprobar su propio PR. Si el dueño no está, aprueba el otro miembro del equipo.

**Suplencia.** Hoy solo el área 5 tiene suplente declarado: la persona 1 entra si la tarjeta lleva 7 días en *En curso* sin PR ni avance, o 5 días en *Listo* sin arrancar cuando bloquea la fase. Mientras dure, la revisión va en sentido inverso — si escribe la persona 1, aprueba la 5, y al revés. Las reglas completas están en `Equipo_responsabilidades.md`.

Un PR que toque dos áreas necesita las dos aprobaciones. Si eso ocurre con frecuencia, el cambio está mal separado.

### Qué mira una revisión

Cuatro puntos, en este orden. Si uno falla, el PR se devuelve con el motivo escrito; no se arregla en un comentario al pasar.

| Punto | Qué se comprueba |
|---|---|
| Contrato | El endpoint, el DTO o la migración dicen lo que dice el issue y lo que dice el OpenAPI del módulo |
| Aislamiento y permisos | El `tenant_id` no sale de la petición, el `@PreAuthorize` está puesto y la prueba de un rol ajeno falla como debe |
| Pruebas | Hay una prueba que falla sin el cambio. "No aplica" solo vale en documentación |
| Nombres y sobre | Vocabulario en español, `{success, message, data}`, dinero sin `float`, sin lógica de negocio en el controlador |

No se revisa el formato del código — eso lo hace el formateador — ni el estilo personal. Si para aprobar hay que reescribir el PR, el PR estaba mal separado.

## 10. Errores comunes

| Síntoma | Causa | Solución |
|---|---|---|
| `protected branch hook declined` al hacer push | Intentaste subir directo a `develop` | Crea una rama y abre un PR |
| El PR dice *Waiting for code owner review* y nadie aparece | El área no tiene a nadie en su equipo | Avisa al área 1 para añadir al miembro |
| El issue sigue abierto tras el merge | Falta `Closes #N` en el cuerpo del PR | Ciérralo a mano y no olvidarlo la próxima |
| Los cambios aparecen en `develop` local sin querer | Commiteaste antes de crear la rama | `git switch -c feature/...` arrastra los cambios sin perderlos |
| El PR tiene conflictos | `develop` avanzó mientras trabajabas | `git switch develop; git pull --ff-only; git switch -; git rebase develop` |
| El PR tiene 900 líneas y nadie lo revisa | Se acumuló trabajo de varios días en una sola rama | Parte el issue antes de empezar; el tope es 400 líneas |

## 11. Antes de empezar el día

```powershell
git switch develop
git pull --ff-only
git branch -a
```

Si trabajas en una rama vieja, rebasa sobre `develop` antes de abrir el PR. Un PR con conflictos no se revisa.

## 12. Regla de oro

**Sin issue no hay rama, sin rama no hay PR, sin PR no hay merge.** Todo lo demás en este documento son detalles de cómo cumplir esa frase.
