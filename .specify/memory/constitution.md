# Constituci├│n de VetCat

## Principios fundamentales

### I. Los requerimientos mandan
Los requerimientos de `docs/requerimientos/` son la base del proyecto. La especificaci├│n, el plan, las tareas y el c├│digo se derivan de ellos. Si otro documento los contradice, se corrige el documento, no el requerimiento. Cada especificaci├│n cita los IDs de los RF que cubre (por ejemplo `AGE-001`).

### II. El aislamiento entre cl├¡nicas es innegociable
Aplicaci├│n compartida con un esquema de PostgreSQL por cl├¡nica (`catalogo` global y `clinica_xxx` por tenant). El `tenant_id` sale **solo** del JWT firmado, validado contra la pertenencia usuario-cl├¡nica; nunca de un par├ímetro, cuerpo o encabezado. Se resuelve por conexi├│n o transacci├│n y jam├ís se guarda en un campo de un bean. El aislamiento se implementa y se prueba con dos cl├¡nicas antes de cualquier m├│dulo funcional.

### III. El backend autoriza
La autorizaci├│n vive en el servidor con `@PreAuthorize`; ocultar pantallas en Flutter no es seguridad. Las funciones y los l├¡mites del plan se verifican en el servidor en cada petici├│n. El rol `cliente` vuelve a verificar la propiedad de la mascota en cada consulta.

### IV. Pruebas primero, en este orden de prioridad (NO NEGOCIABLE)
1. Aislamiento entre cl├¡nicas (dos cl├¡nicas, acceso cruzado denegado).
2. Autorizaci├│n por rol (401 sin JWT, 403 con rol equivocado).
3. Reservas concurrentes de citas (solo una gana).
4. Integridad del expediente cl├¡nico.
5. C├ílculo de la pr├│xima dosis de vacunas.
6. Interfaz.

Las pruebas usan JUnit 5 y Testcontainers contra PostgreSQL real, una clase por m├│dulo. Un error corregido empieza con una prueba que falla.

### V. Contrato antes que c├│digo, y los contratos no se rompen
El due├▒o del m├│dulo escribe el contrato OpenAPI antes de la pantalla. La API vive en `/api/v1/...` con un sobre uniforme `{"success": bool, "message": str, "data": ...}` tanto en ├®xito como en error. Un contrato publicado no se rompe: los clientes m├│viles instalados no se actualizan al instante.

### VI. Cambios peque├▒os y trazables
Sin issue no hay rama, sin rama no hay PR. Un PR atiende un solo issue y no supera 400 l├¡neas efectivas (a├▒adidas m├ís borradas). Si pasa de ah├¡, se parte. Una decisi├│n de arquitectura nueva se registra en un ADR antes de implementarse.

### VII. Nada nuevo sin decidirlo
No se a├▒ade una librer├¡a, un patr├│n ni un esquema de nombres que los documentos de `docs/` no contemplen. Si hace falta, se abre un issue para arquitectura; no se decide dentro de un PR de funcionalidad.

## Restricciones t├®cnicas

- **Stack:** Spring Boot, PostgreSQL 17, Keycloak (un realm `vetcat`, cliente p├║blico OIDC con PKCE), Flutter (app Android ├║nica para `veterinario` y `cliente`; consola web con ├írea de cl├¡nica y ├írea de plataforma). Las versiones est├ín en `docs/Tecnologias_y_patrones.md` y en los ADR.
- **Roles:** `super_admin`, `clinica_admin`, `veterinario`, `recepcionista` y `cliente`. Un usuario puede tener varios roles en una cl├¡nica; el permiso es la uni├│n de sus roles en esa cl├¡nica.
- **Citas:** el anti-empalme lo garantiza una restricci├│n `EXCLUDE USING gist` de PostgreSQL (`tstzrange(inicio, fin, '[)')`, `WHERE estado <> 'CANCELADA'`), no solo una consulta previa. Lo mismo para el consultorio.
- **Dinero:** `NUMERIC(12,2)` o enteros en unidades menores, nunca punto flotante. Los folios de ticket son consecutivos y sin huecos por cl├¡nica.
- **Migraciones:** Flyway, aplicadas por esquema de cl├¡nica y ejecutadas como tarea aparte; la API nunca migra. Sin secretos ni `.env` en el repositorio.
- **Portabilidad:** nada asume la m├íquina del desarrollador. La configuraci├│n sale de variables de entorno, los registros van a la salida est├índar y no hay estado fuera de los vol├║menes.
- **Capas del backend:** `controller ÔåÆ service ÔåÆ repository ÔåÆ entity/dto`, en un solo sentido. Los controladores reciben y devuelven DTO, nunca entidades. Los DTO son `record` llamados `<Entidad>Request` y `<Entidad>Response`. Un ├║nico `@RestControllerAdvice` convierte las excepciones al sobre de error, sin filtrar trazas, SQL ni nombres internos.
- **Vocabulario:** t├®rminos de dominio en espa├▒ol en c├│digo, base de datos y API (`propietario`, `mascota`, `cita`, `consulta`, `vacuna_aplicada`, `cliente`).

## Flujo de trabajo

- **Ramas:** `main` y `develop` est├ín protegidas. El trabajo va en `feature/<issue>-<slug>` o `bugfix/<issue>-<slug>` desde `develop`, y se fusiona por PR con *squash* tras la aprobaci├│n del due├▒o del m├│dulo.
- **Commits:** `tipo(alcance): descripci├│n en espa├▒ol, en infinitivo y en min├║sculas`, con el pie `Refs #N`.
- **Definici├│n de terminado:** compila, valida la entrada, tiene pruebas, respeta los permisos por rol y el aislamiento entre cl├¡nicas, se fusion├│ por PR y est├í documentado.
- **Spec Kit dentro de cada issue:** especificar, planear y dividir en tareas antes de programar. La carpeta de la funcionalidad es `specs/NNN-slug/`, donde `NNN` es el n├║mero del issue con tres cifras (por ejemplo `specs/066-agenda-citas/`).
- **Sprints:** de una semana. El sprint es un periodo de trabajo; el release se corta cuando su milestone est├í al 100 %. El plan est├í en `docs/Plan_de_sprints.md`.

## Gobernanza

Esta constituci├│n prevalece sobre las pr├ícticas informales. Una enmienda se propone por PR, la aprueba el ├írea de arquitectura y actualiza la versi├│n de este archivo. Toda revisi├│n de PR verifica el cumplimiento de estos principios, y una complejidad a├▒adida debe justificarse en el plan (secci├│n "Seguimiento de complejidad"). La gu├¡a diaria de trabajo est├í en `docs/Spec_Kit.md`.

**Versi├│n**: 1.0.0 | **Ratificada**: 2026-11-20 | **├Ültima enmienda**: 2026-11-20
