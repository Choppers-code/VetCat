# VetCat

SaaS multi-clínica para veterinarias pequeñas: agenda de citas, expediente clínico, control de vacunas, portal del dueño y facturación con caja. Una sola aplicación compartida por todas las clínicas, con los datos aislados por clínica mediante un esquema de PostgreSQL por tenant.

**Estado:** planificación. Todavía no hay código de negocio; el repositorio contiene la documentación del plan y la estructura de carpetas.

## Empezar

```powershell
git clone https://github.com/Choppers-code/VetCat.git
cd VetCat
gh auth login
.\scripts\verificar-entorno.ps1
.\scripts\nueva-rama.ps1 -Issue <número> -Slug <nombre-corto>
```

`verificar-entorno.ps1` comprueba que la máquina tenga lo necesario y dice qué falta. `nueva-rama.ps1` crea la rama del issue desde `develop`. Al terminar el trabajo:

```powershell
.\scripts\terminar-rama.ps1 -Issue <número>
```

Antes de empezar, elige una tarjeta en *Listo* del [tablero](https://github.com/orgs/Choppers-code/projects/1) y léela entera: los criterios de aceptación están ahí. Hace falta ser miembro de la organización `Choppers-code`; si no lo eres, pídelo al área 1.

## Requisitos

| Herramienta | Versión | Desde cuándo |
|---|---|---|
| Git | 2.40 o superior | Siempre |
| GitHub CLI | 2.40 o superior | Siempre: PR, issues y tablero |
| JDK | 25, Temurin | Fase 2, backend |
| Flutter | 3.x | Fase 3, app y consola web |
| Docker Desktop | con Compose v2 | Fase 1, stack completo |

`verificar-entorno.ps1` los comprueba todos y marca cuáles son obligatorios hoy.

## Documentación

| # | Documento | Contenido |
|---|---|---|
| 1 | [Alcance.md](docs/Alcance.md) | Qué se construye, qué no, y con qué clientes |
| 2 | [Roadmap.md](docs/Roadmap.md) | Fases, entregas, versiones y dependencias duras |
| 3 | [Decisiones_tecnicas.md](docs/Decisiones_tecnicas.md) | Reglas de arquitectura y modelo de datos |
| 4 | [Tecnologias_y_patrones.md](docs/Tecnologias_y_patrones.md) | Stack, patrones de diseño y anti-patrones |
| 5 | [Equipo_responsabilidades.md](docs/Equipo_responsabilidades.md) | Quién es dueño de qué y cómo se evita el solape |
| 6 | [Despliegue_y_operacion.md](docs/Despliegue_y_operacion.md) | Entornos, migraciones, respaldos, monitoreo e incidentes |
| 7 | [Estructura_proyecto.md](docs/Estructura_proyecto.md) | Qué hay en cada carpeta y dónde va cada cosa |
| 8 | [Flujo_de_trabajo.md](docs/Flujo_de_trabajo.md) | Ramas, commits, PRs, releases y uso del tablero |

## Cómo se trabaja

- **Git Flow**: `main` solo recibe releases y hotfixes; `develop` integra; el trabajo va en `feature/<issue>-<slug>`.
- **Las ramas se crean con un script**, no a mano: `.\scripts\nueva-rama.ps1 -Issue <n> -Slug <slug>`. El nombre sale siempre bien.
- **Un issue, una rama, un PR.** El número del issue es lo que une el tablero con el código.
- **CODEOWNERS decide quién revisa.** Cada paquete y cada carpeta tiene un dueño único.
- Los mensajes de commit siguen Conventional Commits, con el módulo como scope: `feat(agenda): ...`.

## Documentos originales

`docs/historico/Propuesta_VetCat.md` (v1.0) y `docs/historico/Plan_inicial.md` son la propuesta y el plan originales. Se conservan como referencia y **no se actualizan**. Donde contradigan a los ocho documentos de arriba, mandan los ocho de arriba.

## Licencia

Sin licencia. Todos los derechos reservados.
