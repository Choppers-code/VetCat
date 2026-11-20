# Constitución de VetCat

## Principios fundamentales

### I. Los requerimientos mandan
Los requerimientos de `docs/requerimientos/` son la base del proyecto. La especificación, el plan, las tareas y el código se derivan de ellos. Si otro documento los contradice, se corrige el documento, no el requerimiento. Cada especificación cita los IDs de los RF que cubre (por ejemplo `AGE-001`).

### II. El aislamiento entre clínicas es innegociable
Aplicación compartida con un esquema de PostgreSQL por clínica (`catalogo` global y `clinica_xxx` por tenant). El `tenant_id` sale **solo** del JWT firmado, validado contra la pertenencia usuario-clínica; nunca de un parámetro, cuerpo o encabezado. Se resuelve por conexión o transacción y jamás se guarda en un campo de un bean. El aislamiento se implementa y se prueba con dos clínicas antes de cualquier módulo funcional.

### III. El backend autoriza
La autorización vive en el servidor con `@PreAuthorize`; ocultar pantallas en Flutter no es seguridad. Las funciones y los límites del plan se verifican en el servidor en cada petición. El rol `cliente` vuelve a verificar la propiedad de la mascota en cada consulta.

### IV. Pruebas primero, en este orden de prioridad (NO NEGOCIABLE)
1. Aislamiento entre clínicas (dos clínicas, acceso cruzado denegado).
2. Autorización por rol (401 sin JWT, 403 con rol equivocado).
3. Reservas concurrentes de citas (solo una gana).
4. Integridad del expediente clínico.
5. Cálculo de la próxima dosis de vacunas.
6. Interfaz.

Las pruebas usan JUnit 5 y Testcontainers contra PostgreSQL real, una clase por módulo. Un error corregido empieza con una prueba que falla.

### V. Contrato antes que código, y los contratos no se rompen
El dueño del módulo escribe el contrato OpenAPI antes de la pantalla. La API vive en `/api/v1/...` con un sobre uniforme `{"success": bool, "message": str, "data": ...}` tanto en éxito como en error. Un contrato publicado no se rompe: los clientes móviles instalados no se actualizan al instante.

### VI. Cambios pequeños y trazables
Sin issue no hay rama, sin rama no hay PR. Un PR atiende un solo issue y no supera 400 líneas efectivas (añadidas más borradas). Si pasa de ahí, se parte. Una decisión de arquitectura nueva se registra en un ADR antes de implementarse.

### VII. Nada nuevo sin decidirlo
No se añade una librería, un patrón ni un esquema de nombres que los documentos de `docs/` no contemplen. Si hace falta, se abre un issue para arquitectura; no se decide dentro de un PR de funcionalidad.

## Restricciones técnicas

- **Stack:** Spring Boot, PostgreSQL 17, Keycloak (un realm `vetcat`, cliente público OIDC con PKCE), Flutter (app Android única para `veterinario` y `cliente`; consola web con área de clínica y área de plataforma). Las versiones están en `docs/Tecnologias_y_patrones.md` y en los ADR.
- **Roles:** `super_admin`, `clinica_admin`, `veterinario`, `recepcionista` y `cliente`. Un usuario puede tener varios roles en una clínica; el permiso es la unión de sus roles en esa clínica.
- **Citas:** el anti-empalme lo garantiza una restricción `EXCLUDE USING gist` de PostgreSQL (`tstzrange(inicio, fin, '[)')`, `WHERE estado <> 'CANCELADA'`), no solo una consulta previa. Lo mismo para el consultorio.
- **Dinero:** `NUMERIC(12,2)` o enteros en unidades menores, nunca punto flotante. Los folios de ticket son consecutivos y sin huecos por clínica.
- **Migraciones:** Flyway, aplicadas por esquema de clínica y ejecutadas como tarea aparte; la API nunca migra. Sin secretos ni `.env` en el repositorio.
- **Portabilidad:** nada asume la máquina del desarrollador. La configuración sale de variables de entorno, los registros van a la salida estándar y no hay estado fuera de los volúmenes.
- **Capas del backend:** `controller → service → repository → entity/dto`, en un solo sentido. Los controladores reciben y devuelven DTO, nunca entidades. Los DTO son `record` llamados `<Entidad>Request` y `<Entidad>Response`. Un único `@RestControllerAdvice` convierte las excepciones al sobre de error, sin filtrar trazas, SQL ni nombres internos.
- **Vocabulario:** términos de dominio en español en código, base de datos y API (`propietario`, `mascota`, `cita`, `consulta`, `vacuna_aplicada`, `cliente`).

## Flujo de trabajo

- **Ramas:** `main` y `develop` están protegidas. El trabajo va en `feature/<issue>-<slug>` o `bugfix/<issue>-<slug>` desde `develop`, y se fusiona por PR con *squash* tras la aprobación del dueño del módulo.
- **Commits:** `tipo(alcance): descripción en español, en infinitivo y en minúsculas`, con el pie `Refs #N`.
- **Definición de terminado:** compila, valida la entrada, tiene pruebas, respeta los permisos por rol y el aislamiento entre clínicas, se fusionó por PR y está documentado.
- **Spec Kit dentro de cada issue:** especificar, planear y dividir en tareas antes de programar. La carpeta de la funcionalidad es `specs/NNN-slug/`, donde `NNN` es el número del issue con tres cifras (por ejemplo `specs/066-agenda-citas/`).
- **Sprints:** de una semana. El sprint es un periodo de trabajo; el release se corta cuando su milestone está al 100 %. El plan está en `docs/Plan_de_sprints.md`.

## Gobernanza

Esta constitución prevalece sobre las prácticas informales. Una enmienda se propone por PR, la aprueba el área de arquitectura y actualiza la versión de este archivo. Toda revisión de PR verifica el cumplimiento de estos principios, y una complejidad añadida debe justificarse en el plan (sección "Seguimiento de complejidad"). La guía diaria de trabajo está en `docs/Spec_Kit.md`.

**Versión**: 1.0.0 | **Ratificada**: 2026-11-20 | **Última enmienda**: 2026-11-20
