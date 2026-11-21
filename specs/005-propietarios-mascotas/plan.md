# Plan de implementación: Propietarios y mascotas

**Rama**: `feature/5-propietarios-mascotas` | **Fecha**: 2026-11-20 | **Especificación**: [spec.md](spec.md)

**Entrada**: Especificación de la funcionalidad en `specs/005-propietarios-mascotas/spec.md`

## Resumen

Backend del módulo de propietarios y mascotas (PRO-001 a PRO-009) sobre el aislamiento por clínica de la fase 2. Dos entidades en el esquema de cada clínica, un contrato OpenAPI primero, y la regla "una mascota no cambia de propietario" garantizada también por la base de datos. La búsqueda sin acentos usa la extensión `unaccent`, instalada una vez en `ext`.

## Contexto técnico

**Lenguaje/Versión**: Java 25, Spring Boot 4.1.0

**Dependencias principales**: Spring Web, Spring Data JPA (unidad de persistencia `tenant`), Spring Security y `@PreAuthorize` (fase 3), Flyway. Ninguna librería nueva.

**Almacenamiento**: PostgreSQL 17, esquema de clínica `clinica_NNN` para los datos y `catalogo`/`ext` para la extensión `unaccent`.

**Pruebas**: JUnit 6 y Testcontainers 2 contra PostgreSQL real, una clase por módulo, con dos clínicas.

**Plataforma de destino**: API REST `/api/v1/propietarios`, `/api/v1/mascotas` y `/api/v1/busqueda`. Las pantallas Flutter son de otro issue.

**Metas de rendimiento**: búsqueda en menos de 1 s sobre 5 000 propietarios por clínica (SC-004).

**Restricciones**: la clínica sale solo del token; `@PreAuthorize` en cada endpoint; el `cliente` no usa el módulo; nada se borra.


## Verificación de la constitución

| Principio | Cumple | Cómo |
|---|---|---|
| I. Los requerimientos mandan | Sí | Cita PRO-001 a PRO-009; lo no escrito va como supuesto, no como requisito |
| II. Aislamiento entre clínicas | Sí | Entidades en la unidad `tenant`; prueba de dos clínicas por historia |
| III. El backend autoriza | Sí | `@PreAuthorize` por rol; baja y "incluir inactivos" solo `clinica_admin` |
| IV. Pruebas primero | Sí | Aislamiento y autorización van antes de la lógica en `tasks.md` |
| V. Contrato antes que código | Sí | Paths en `docs/api/v1/openapi.yaml` en el primer PR |
| VI. Cambios pequeños | Sí | Seis PR, cada uno de 400 líneas o menos (ver más abajo) |
| VII. Nada nuevo sin decidirlo | Con justificación | `unaccent` y el trigger de inmutabilidad, en la tabla de complejidad |

## Estructura del proyecto

### Documentación (esta funcionalidad)

```text
specs/005-propietarios-mascotas/
├── spec.md
├── plan.md              # Este archivo, con el modelo de datos y el contrato
└── tasks.md
```

### Código fuente

```text
backend/src/main/java/com/vetcat/
├── propietarios/{controller,service,repository,entity,dto}/
└── mascotas/{controller,service,repository,entity,dto}/
backend/src/test/java/com/vetcat/{propietarios,mascotas}/
database/migrations/
├── catalogo/V5__extension_unaccent.sql
└── tenant/V1__propietario_y_mascota.sql
docs/api/v1/openapi.yaml            # paths de propietarios, mascotas y búsqueda
```

**Decisión de estructura**: un paquete por módulo, con las mismas capas (`controller → service → repository → entity/dto`). La búsqueda vive en `propietarios`, que consulta a `mascotas` mediante su fachada, no su repositorio (regla 2 de `Estructura_proyecto.md`).

## Modelo de datos

Esquema de clínica, migración `tenant/V1`:

| Tabla | Columnas | Reglas |
|---|---|---|
| `propietario` | `id` identidad, `nombre` y `telefono` no nulos, `telefono_normalizado` generado, `correo`, `direccion`, `activo` (por defecto verdadero), `creado_en`, `actualizado_en` | Índice por `telefono_normalizado`; la dirección solo se acepta con la función del plan |
| `mascota` | `id` identidad, `propietario_id` no nulo con clave foránea, `nombre`, `especie`, `raza`, `sexo`, `fecha_nacimiento`, `peso_kg` `NUMERIC(6,2)`, `notas`, `activo`, `creado_en`, `actualizado_en` | `especie` con CHECK de la lista cerrada (`PERRO`, `GATO`, `AVE`, `ROEDOR`, `REPTIL`, `OTRA`); `sexo` con CHECK (`MACHO`, `HEMBRA`, `DESCONOCIDO`); `fecha_nacimiento <= CURRENT_DATE`; `peso_kg > 0` |

- **Inmutabilidad del propietario.** Un trigger `BEFORE UPDATE` en `mascota` lanza una excepción si cambia `propietario_id` (FR-010). El DTO de edición ni siquiera trae el campo; el trigger cubre un fallo de la aplicación.
- **Estados con `varchar` y CHECK**, nunca `CREATE TYPE` por esquema.
- **Búsqueda.** `ext.unaccent(lower(campo)) LIKE ...` con el texto escapado; sin índice especial en esta entrega por el tamaño por clínica. Si la meta de SC-004 no se cumple, se añade un índice sobre una función inmutable en un issue aparte.

## Contrato (resumen)

Todos bajo el sobre uniforme y paginados con `page` y `size`.

| Método y ruta | Roles | Resultado |
|---|---|---|
| `POST /propietarios` | `clinica_admin`, `recepcionista`, `veterinario` | 201; 409 si el teléfono ya existe y no trae `confirmarDuplicado` |
| `GET /propietarios/{id}` | los tres | ficha con mascotas activas; 404 si es de otra clínica |
| `PUT /propietarios/{id}` | los tres | 200; 422 si trae dirección sin la función del plan |
| `POST /propietarios/{id}/baja` y `/reactivar` | `clinica_admin` | 200; 409 con las citas futuras si las hay |
| `POST /mascotas` | los tres | 201; 422 por fecha futura, peso o especie inválidos |
| `GET` y `PUT /mascotas/{id}` | los tres | ficha de datos generales |
| `POST /mascotas/{id}/baja` y `/reactivar` | `clinica_admin` | como las del propietario |
| `GET /busqueda?q=&incluirInactivos=` | los tres; `incluirInactivos` solo `clinica_admin` | 422 con menos de 3 caracteres |

## Decisiones de diseño

- **Duplicados por teléfono.** La comprobación y la inserción corren bajo `pg_advisory_xact_lock` sobre el teléfono normalizado, para que dos solicitudes simultáneas sin confirmación no creen dos propietarios.
- **Baja y registro de mascota.** Registrar una mascota toma el registro del propietario con `FOR SHARE`; dar de baja lo actualiza. Así no puede nacer una mascota activa bajo un propietario inactivo.
- **Citas futuras.** Una interfaz `ConsultaCitasFuturas` devuelve, hoy, una lista vacía. La fase 5 aporta la implementación real sin tocar este módulo.

## Seguimiento de complejidad

| Violación | Por qué es necesaria | Alternativa más simple descartada porque |
|---|---|---|
| Extensión `unaccent` de PostgreSQL | PRO-009 exige ignorar acentos | Una columna normalizada duplica datos y hay que sincronizarla; se pide que arquitectura lo confirme |
| Trigger de inmutabilidad en `mascota` | PRO-008 y SC-005: el rechazo debe sostenerse aunque falle la aplicación | Solo validar en el servicio deja un camino abierto a un cambio directo |

## Plan de PR (400 líneas efectivas o menos cada uno)

| PR | Contenido | Historia |
|---|---|---|
| 1 | Contrato OpenAPI y migraciones (`catalogo/V5`, `tenant/V1`) | Base |
| 2 | Propietarios: registro, edición, ficha y duplicados | US1 |
| 3 | Mascotas: registro, edición, ficha e inmutabilidad | US2 |
| 4 | Búsqueda | US3 |
| 5 | Baja y reactivación de propietarios y mascotas | US4 |
| 6 | Dirección del propietario con la función del plan | US5 |
