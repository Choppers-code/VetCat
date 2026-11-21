# Tareas: Propietarios y mascotas

**Entrada**: Documentos de diseño en `specs/005-propietarios-mascotas/`

**Requisitos previos**: `plan.md` y `spec.md`

**Pruebas**: obligatorias (constitución, principio IV). En cada historia van primero el aislamiento entre clínicas y la autorización por rol, y la lógica después.

## Formato: `[ID] [P?] [Historia] Descripción`

- **[P]**: se puede ejecutar en paralelo (archivos distintos, sin dependencias)
- **[US1]**, **[US2]**…: historia de usuario a la que pertenece

## Convenciones de rutas

- **Backend**: `backend/src/main/java/com/vetcat/<modulo>/` y `backend/src/test/java/com/vetcat/<modulo>/`
- **Migraciones**: `database/migrations/catalogo/` y `database/migrations/tenant/`
- **Contrato**: `docs/api/v1/openapi.yaml`

---

## Fase 1: Preparación (PR 1)

- [ ] T001 Añadir al contrato OpenAPI los paths de propietarios, mascotas y búsqueda, con sus esquemas `Request` y `Response`, en `docs/api/v1/openapi.yaml`
- [ ] T002 [P] Crear `database/migrations/catalogo/V5__extension_unaccent.sql` con `CREATE EXTENSION IF NOT EXISTS unaccent SCHEMA ext`
- [ ] T003 [P] Crear `database/migrations/tenant/V1__propietario_y_mascota.sql` con las tablas, los CHECK y el índice del teléfono normalizado
- [ ] T004 Añadir al migrador de tenant de la prueba la migración `tenant/V1` real y probarla contra PostgreSQL (tablas y restricciones de especie, sexo, fecha y peso)

## Fase 2: Fundamentos (bloquean todas las historias)

- [ ] T005 Crear en la migración el trigger `BEFORE UPDATE` de `mascota` que impide cambiar `propietario_id`, y su prueba directa en SQL (SC-005)
- [ ] T006 [P] Crear las entidades `Propietario` y `Mascota` en `propietarios/entity` y `mascotas/entity`
- [ ] T007 [P] Crear los repositorios en `propietarios/repository` y `mascotas/repository`
- [ ] T008 [P] Crear las excepciones de negocio (duplicado, ya inactivo, citas futuras) y su traducción en el `@RestControllerAdvice` de `shared`
- [ ] T009 Definir la interfaz `ConsultaCitasFuturas` con una implementación que devuelve una lista vacía

## Fase 3: Historia de usuario 1 — Propietarios (P1) · PR 2

**Objetivo**: registrar, editar y consultar propietarios con aviso de teléfono repetido.

**Prueba independiente**: con dos clínicas, registrar y consultar sin cruce de datos.

### Pruebas de la historia 1

- [ ] T010 [US1] Prueba de aislamiento: un propietario de la clínica A es "no encontrado" desde la B, en `propietarios/` (SC-001)
- [ ] T011 [US1] Prueba de autorización: 401 sin token, 403 para `cliente`, 201 para los tres roles de personal
- [ ] T012 [US1] Prueba de duplicado: 409 sin confirmar y 201 confirmando; dos solicitudes simultáneas sin confirmar crean una sola

### Implementación de la historia 1

- [ ] T013 [US1] Crear `PropietarioRequest` y `PropietarioResponse` como `record`
- [ ] T014 [US1] Implementar `PropietarioService`: normalizar el teléfono, tomar el `pg_advisory_xact_lock` y comprobar duplicados
- [ ] T015 [US1] Implementar `PropietarioController` con `@PreAuthorize` y `@Valid`
- [ ] T016 [US1] Armar la ficha con las mascotas activas mediante la fachada de `mascotas`

## Fase 4: Historia de usuario 2 — Mascotas (P1) · PR 3

**Objetivo**: registrar y consultar mascotas ligadas a un propietario que no cambia.

### Pruebas de la historia 2

- [ ] T017 [US2] Prueba de aislamiento de mascotas entre dos clínicas
- [ ] T018 [US2] Prueba de autorización por rol en los endpoints de mascotas
- [ ] T019 [US2] Pruebas de validación: fecha futura, peso no positivo y especie fuera de la lista
- [ ] T020 [US2] Prueba de inmutabilidad: el servicio rechaza el cambio de propietario y el trigger lo rechaza en SQL

### Implementación de la historia 2

- [ ] T021 [US2] Crear `MascotaRequest`, `MascotaActualizacionRequest` (sin propietario) y `MascotaResponse`
- [ ] T022 [US2] Implementar `MascotaService` tomando el registro del propietario con `FOR SHARE`
- [ ] T023 [US2] Implementar `MascotaController` y la fachada que usa `propietarios`

**Punto de control**: las historias 1 y 2 funcionan por separado y juntas.

---

## Fase 5: Historia de usuario 3 — Búsqueda (P2) · PR 4

### Pruebas de la historia 3

- [ ] T024 [US3] Prueba de aislamiento de la búsqueda entre dos clínicas
- [ ] T025 [US3] Pruebas de acentos y mayúsculas, mínimo de 3 caracteres y comodines escapados
- [ ] T026 [US3] Prueba de `incluirInactivos`: permitido al `clinica_admin` y 403 a los demás roles

### Implementación de la historia 3

- [ ] T027 [US3] Implementar la consulta con `ext.unaccent(lower(...))` y el escape del texto
- [ ] T028 [US3] Implementar `GET /busqueda` con el propietario junto a cada mascota
- [ ] T029 [US3] Medir SC-004 con 5 000 propietarios y registrar el resultado en el PR

---

## Fase 6: Historia de usuario 4 — Baja y reactivación (P2) · PR 5

### Pruebas de la historia 4

- [ ] T030 [US4] Prueba de autorización: solo `clinica_admin` da de baja y reactiva; 403 para los demás
- [ ] T031 [US4] Prueba de baja en cascada: el propietario y sus mascotas quedan inactivos y se conservan sus datos
- [ ] T032 [US4] Prueba de concurrencia: dar de baja mientras se registra una mascota no deja una mascota activa bajo un propietario inactivo
- [ ] T033 [US4] Prueba con una `ConsultaCitasFuturas` que devuelve citas: lista las citas y no da de baja

### Implementación de la historia 4

- [ ] T034 [US4] Implementar baja y reactivación en los dos servicios y sus endpoints
- [ ] T035 [US4] Excluir inactivos de listados, búsquedas y registro de mascotas

---

## Fase 7: Historia de usuario 5 — Dirección (P3) · PR 6

**Depende de**: la verificación de funciones del plan (PLA-004).

- [ ] T036 [US5] Prueba con dos clínicas, una con `visitas_domicilio` y otra sin ella: acepta o rechaza la dirección
- [ ] T037 [US5] Aceptar y mostrar la dirección solo con la función, y ocultar el campo en el contrato

---

## Fase final: Acabado

- [ ] T038 Revisar que el contrato publicado coincide con la API y no rompe versiones instaladas
- [ ] T039 Ejecutar `/speckit-analyze` sobre estos tres documentos antes de empezar a programar

## Dependencias y orden

- **US3, US4 y US5** dependen de US1 y US2, y entre sí no.
- **Fuera de este issue**: el límite de mascotas del plan (sprint 11), la propiedad verificada del `cliente` (módulo POR) y las pantallas Flutter.
