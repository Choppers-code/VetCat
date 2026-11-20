# AGENTS.md

## Repo state (read this first)

Planning stage. The repo is a git repository on GitHub (`Choppers-code/VetCat`) with `main` and `develop` (default) branches, the planned folder scaffold (empty folders with `.gitkeep`), `.github/` (CODEOWNERS, issue/PR templates, branch-name CI in `workflows/ramas.yml`) and PowerShell scripts in `scripts/` (`verificar-entorno.ps1`, `nueva-rama.ps1`, `terminar-rama.ps1`). There is still no application code and no build config.

Branches are created with `scripts/nueva-rama.ps1 -Issue <n> -Slug <slug>` (`feature/<issue>-<slug>`) and finished with a PR; never `git flow feature finish` (see `docs/adr/0001-git-flow-sin-finish.md`).

Read, in this order:
1. `docs/requerimientos/README.md` — the functional and non-functional requirements; they win over every other document.
2. `docs/Alcance.md` — what is built and what is explicitly not.
3. `docs/Roadmap.md` — phase order and hard dependencies.
4. `docs/Decisiones_tecnicas.md` — architecture and data-model rules.
5. `docs/Tecnologias_y_patrones.md` — stack, patterns and forbidden anti-patterns.
6. `docs/Equipo_responsabilidades.md` — who owns what, and the anti-overlap rules.
7. `docs/Despliegue_y_operacion.md` — environments, migrations, backups, monitoring.
8. `docs/Estructura_proyecto.md` — folder layout and where each kind of code goes.
9. `docs/Flujo_de_trabajo.md` — branches, commits, PRs, releases and the project board.

`docs/historico/Propuesta_VetCat.md` and `docs/historico/Plan_inicial.md` are the original v1 documents and are **not** kept up to date. Where they conflict with the documents above, the documents above win.

Docs are written in Spanish. Keep new docs in Spanish under `docs/`; keep the planned layout (`backend/`, `frontend/`, `database/`, `infrastructure/{docker,keycloak}`, `docs/`, `scripts/`) when scaffolding.

## Planned stack (nothing built yet)

One Android app for `veterinario` and `cliente` (Android first, mobile-first) + Flutter Web console with a clinic area and a platform area → Keycloak (OIDC public client + PKCE, one realm `vetcat`) → Spring Boot 4.1 REST → PostgreSQL 17, orchestrated with Docker Compose. Roles: `super_admin`, `clinica_admin`, `veterinario`, `recepcionista`, `cliente` (a user may hold several roles in one clinic). Desktop and iOS are out of the first scope.

## Non-negotiable architecture rules

- Hybrid tenancy: shared app + **schema per clinic** (`catalogo` global, `clinica_xxx` per tenant). Multi-tenancy is blocking work — it must be implemented and tested before functional modules.
- `catalogo` and the tenant schemas need **separate persistence units**; the catalog one is not multi-tenant.
- `tenant_id` comes **only** from the signed JWT, validated against the user↔clinic relation (`catalogo.usuario_clinica`). Never from a request param, body, or header.
- Resolve the tenant per connection/transaction (`CurrentTenantIdentifierResolver` + `MultiTenantConnectionProvider`). Never cache it in a bean field — singleton beans (Spring's default) must stay stateless.
- Appointment overlap must be blocked by a PostgreSQL `EXCLUDE USING gist` constraint (`btree_gist`, `tstzrange(inicio, fin, '[)')`, `WHERE estado <> 'CANCELADA'`), not just a `SELECT` pre-check. Same constraint for `consultorio_id`.
- Enforce authorization in the backend with `@PreAuthorize`; hiding Flutter screens is not authorization. The `cliente` role must re-verify pet ownership on every query. Plan features are checked on the server on every request.
- Money as `NUMERIC(12,2)` or integer minor units, never floating point. Ticket folios are sequential per clinic and gap-free.
- Migrations via Flyway, applied per tenant schema. No secrets or `.env` in the repo.
- Published API contracts are not broken: installed mobile clients cannot be updated instantly.
- Nothing assumes the developer machine: config comes from environment variables, logs go to stdout, no state outside volumes, and migrations run as a one-off job — the same stack must run in the cloud unchanged.

## Conventions

- Spanish domain vocabulary in code, DB, and API: `propietario`, `mascota`, `cita`, `consulta`, `vacuna_aplicada`, `cliente`. Packages by module under `com.vetcat`, layers `controller → service → repository → entity/dto`.
- API: `/api/v1/...` with a uniform envelope — `{"success": bool, "message": str, "data": ...}` for both success and errors.
- Git Flow: `main` and `develop` are protected; work on `feature/*` or `bugfix/*` branched from `develop`, merge by PR. `release/x.y.z`, `hotfix/*` from `main`.
- Definition of Done: compiles, validates input, has tests, respects role permissions and tenant isolation, merged by PR, documented.
- Backend layers are one-way: `controller → service → repository`. Controllers take and return DTOs, never entities; input is `@Valid`; no business logic in the controller.
- DTOs are Java records named `<Entidad>Request` / `<Entidad>Response`, in Spanish, with no `Dto` suffix.
- Errors: a single `@RestControllerAdvice` renders every exception as `{"success": false, "message": ..., "data": null}` with the right HTTP status. Never leak stack traces, SQL or internal class names.
- Tests: JUnit 6 + Testcontainers 2 against real PostgreSQL, one class per module. A bug fix starts with a failing test.
- Do not add a library, a pattern or a naming scheme these docs don't have. If it is needed, open an issue for the architect — never decide it inside a feature PR.
- Shared files have one owner and are never touched "while I'm here": `AGENTS.md`, `README.md`, `.github/`, build files, `infrastructure/docker/compose.yaml`, `database/`, `infrastructure/`. Change them in their own PR.
- One PR, one issue, at most 400 effective lines (added + removed). More than that means it is not a single change.

## Scope guard

In scope: clinics, plans and plan-gated features, agenda (with client booking and cancellation requests), owners & pets, clinical records, vaccines, client portal, notifications (in-app and push), sales/cash/tickets (cash only, optional per plan), simple inventory (optional per plan), reports, platform administration. Mobile app is the primary client.

Out of scope: desktop app, iOS, offline mode, WhatsApp/SMS, fiscal invoicing (CFDI or equivalent), online plan payments and bank data, photo/study attachments, multi-branch, third-party API/webhooks, white-label. Sales are **internal tickets**, cash only. Offline mode is decided: out of this delivery, so no offline writes, only a read cache and retry.

## Environment

- Stack runs via `docker compose up -d` / `docker compose down`. Never `docker compose down -v` — it deletes persistent data.
- pgAdmin is dev/admin only, never publicly exposed.
- Windows + PowerShell workspace.

## Test priority (order matters)

1. Tenant isolation (two clinics, cross-access denied)
2. Role authorization (401 without JWT, 403 wrong role)
3. Concurrent appointment booking (only one wins)
4. Clinical record integrity
5. Vaccine next-dose calculation
6. UI
