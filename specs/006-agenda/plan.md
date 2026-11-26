# Plan de implementación: Agenda

**Rama**: `feature/203-spec-agenda` | **Fecha**: 2026-11-24 | **Especificación**: [spec.md](spec.md)

**Entrada**: Issue #203 y el plan de reescritura de #65.

## Resumen

La agenda vive en el esquema de cada clínica y el empalme lo impide PostgreSQL con `EXCLUDE USING gist`. La API expone citas, consultorios, horarios, bloqueos y la vista de día y semana. El calendario en la app es otra entrega.

## Contexto técnico

**Lenguaje/Versión**: Java 25, Spring Boot 4.1.0.

**Dependencias**: Spring Data JPA (unidad `tenant`), Spring Security y `@PreAuthorize`, Flyway. La prueba de concurrencia usa Testcontainers 2.

**Almacenamiento**: `tenant/V2` en cada esquema de clínica; `btree_gist` ya está en `ext` desde `catalogo/V1`.

**Pruebas**: JUnit 6 + Testcontainers contra PostgreSQL real, con dos clínicas y dos consultorios.

## Verificación de la constitución

| Principio | Cumple | Cómo |
|---|---|---|
| I. Los requerimientos mandan | Sí | AGE-001 a AGE-018 citados; lo no escrito va como supuesto |
| II. Aislamiento entre clínicas | Sí | Entidades en la unidad `tenant`; prueba de dos clínicas |
| III. El backend autoriza | Sí | `@PreAuthorize` por rol; el veterinario limitado a sus citas |
| IV. Pruebas primero | Sí | Concurrencia y autorización antes de la lógica en `tasks.md` |
| V. Contrato antes que código | Sí | Paths en `docs/api/v1/openapi.yaml` junto a la migración |
| VI. Cambios pequeños | Sí | Cuatro cortes vivos; el resto aparcado por el corte del viernes |
| VII. Nada nuevo sin decidirlo | Sí | `EXCLUDE` y zona horaria ya decididos en #65 |

## Modelo de datos (`tenant/V2`)

| Tabla | Reglas |
|---|---|
| `consultorio` | nombre, activo; baja lógica |
| `horario_consultorio` | día de la semana, rango; excepción por fecha |
| `bloqueo_agenda` | veterinario **o** consultorio, rango, motivo |
| `cita` | mascota, veterinario, consultorio o dirección, `inicio`/`fin`, estado, `reprogramada_de` |

Constraints que sostienen la regla:

```sql
EXCLUDE USING gist (consultorio_id WITH =, tstzrange(inicio, fin, '[)') WITH &&)
  WHERE (estado <> 'CANCELADA')
EXCLUDE USING gist (veterinario_id WITH =, tstzrange(inicio, fin, '[)') WITH &&)
  WHERE (estado <> 'CANCELADA')
```

Estados: `PENDIENTE`, `CONFIRMADA`, `ATENDIDA`, `NO_ASISTIO`, `CANCELADA`, con `varchar` y CHECK.

## Contrato (resumen)

| Método y ruta | Roles | Resultado |
|---|---|---|
| `POST /api/v1/citas` | recepción, admin, veterinario | 201; 409 por empalme; 422 por horario, bloqueo o plan |
| `GET /api/v1/citas?desde=&hasta=` | los tres | agenda del rango, en la zona de la clínica |
| `POST /api/v1/citas/{id}/confirmar`, `/atender`, `/no-asistio`, `/cancelar` | según matriz | 200; 409 transición inválida |
| `POST /api/v1/citas/{id}/reprogramar` | recepción, admin | 201 con la cita nueva |
| `POST /api/v1/citas/solicitudes` | cliente | 201; límite de 3 pendientes |
| `POST /api/v1/citas/{id}/solicitud-cancelacion` | cliente | 200 si hay 5 horas |
| `GET/POST/PUT /api/v1/consultorios` y `/horarios` | admin; lectura para recepción y veterinario | catálogo |
| `GET/POST /api/v1/bloqueos` | veterinario (los suyos), admin | bloqueos |

## Decisiones de diseño

- **El empalme no se comprueba con `SELECT`**: se captura el `23P01` y se responde 409. Un `SELECT` previo deja una ventana de carrera (decisión de #65).
- **Zona horaria de la clínica** en horarios, bloqueos y agenda; el cliente no la envía.
- **Reprogramar** crea una cita nueva: no se edita la anterior.
- **El veterinario ve la agenda de la clínica en lectura**, pero solo crea, reprograma, cancela y bloquea sobre la suya (`Matriz_roles.md`); el filtro se aplica en la consulta, no en la app.

## Plan de PR

| PR | Contenido | Issue | Estado viernes |
|---|---|---|---|
| 1 | Contrato, `tenant/V2` y entidades | #189 | Entra |
| 2 | Consultorios y horarios | #191 | Entra |
| 3 | Bloqueos | #193 | Entra |
| 4 | Crear cita y 409 de empalme | #195 | Entra |
| 5 | Estados, reprogramación y no asistencia | #188 | Aparcado |
| 6 | Solicitudes del cliente y cancelación | #190, #192, #194 | Aparcado |
| 7 | Calendario en la app | #202, #196 | Aparcado |
| 8 | Pruebas de concurrencia y autorización | #199 | Entra la de concurrencia |
