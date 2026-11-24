# Plan de implementación: Planes y funciones habilitadas

**Rama**: `feature/212-spec-planes` | **Fecha**: 2026-11-24 | **Especificación**: [spec.md](spec.md)

**Entrada**: Especificación de la funcionalidad en `specs/002-planes/spec.md`

## Resumen

El plan enciende funciones y fija límites sin duplicar esquemas. El esquema ya está en `catalogo/V5` (#163); este plan cubre lo que falta: la verificación por petición, la vigencia con pagos y la suspensión, más sus pruebas.

## Contexto técnico

**Lenguaje/Versión**: Java 25, Spring Boot 4.1.0.

**Dependencias**: Spring Data JPA (unidad `catalogo`), `JdbcClient` para las lecturas de conteo, Spring Security y `@PreAuthorize` (fase 3). Ninguna librería nueva.

**Almacenamiento**: PostgreSQL 17, esquema `catalogo`. Los límites se cuentan contra tablas de clínica, así que la verificación necesita la unidad `tenant` además de la del catálogo.

**Pruebas**: JUnit 6 y Testcontainers 2 contra PostgreSQL real, una clase por módulo, con dos clínicas y dos planes.

## Verificación de la constitución

| Principio | Cumple | Cómo |
|---|---|---|
| I. Los requerimientos mandan | Sí | Cita PLA-001 a PLA-007; los supuestos van marcados |
| II. Aislamiento entre clínicas | Sí | El plan se lee por la clínica del token; los conteos van por el esquema activo |
| III. El backend autoriza | Sí | `super_admin` para catálogo y pagos; `clinica_admin` solo lectura |
| IV. Pruebas primero | Sí | La prueba de función no incluida va antes que la guarda |
| V. Contrato antes que código | Sí | Endpoints de plan y pagos antes de la lógica |
| VI. Cambios pequeños | Sí | Cuatro cortes de 400 líneas o menos |
| VII. Nada nuevo sin decidirlo | Sí | La vigencia derivada de pagos se decide aquí, no en un PR |

## Estructura del proyecto

```text
backend/src/main/java/com/vetcat/planes/
├── {controller,service,repository,entity,dto}/
backend/src/test/java/com/vetcat/planes/
database/migrations/catalogo/        # V5 ya aplicada; cambios aditivos si hacen falta
docs/api/v1/openapi.yaml             # paths de planes, asignación y pagos
```

## Modelo de datos (ya aplicado en `V5`)

| Tabla | Reglas |
|---|---|
| `plan` | nombre único, activo; un plan en uso no se borra |
| `plan_funcion` | siete claves cerradas con CHECK; los requisitos entre funciones son regla de servicio |
| `plan_limite` | `usuarios`, `veterinarios`, `mascotas`; sin fila = sin límite |
| `pago_plan` | `monto NUMERIC(12,2)`, periodo válido, autor obligatorio |
| `clinica.plan_id` | anulable hoy; el aprovisionamiento lo fija (ADR-0010) |

**Vigencia sin columna nueva**: `max(pago_plan.periodo_hasta)` por clínica. Se indexa `(clinica_id, periodo_hasta DESC)`. Si se prefiere desnormalizar, se decide antes de #158.

## Contrato (resumen)

| Método y ruta | Rol | Resultado |
|---|---|---|
| `GET /api/v1/plataforma/planes` y `POST`/`PUT` | `super_admin` | catálogo |
| `PUT /api/v1/plataforma/clinicas/{id}/plan` | `super_admin` | asigna o cambia, con auditoría |
| `POST /api/v1/plataforma/clinicas/{id}/pagos` | `super_admin` | extiende vigencia y reactiva si estaba suspendida |
| `GET /api/v1/clinica/plan` | `clinica_admin` | plan, funciones, vigencia y uso |

## Decisiones de diseño

- **Funciones como datos**: la lista cerrada vive en el CHECK y en un enum de Java; una función nueva exige migración.
- **Requisitos entre funciones** en el servicio: `turno_caja` e `inventario` requieren `ventas`; apagar una apaga las dependientes.
- **Uso por conteo real** en cada petición, sin contadores guardados; los conteos van por la unidad `tenant` de la clínica activa.
- **La verificación no es un `@PreAuthorize`**: es una guarda que corre en cada petición y responde con el error de función no disponible, para no confundir plan con rol.
- **Suspensión** por proceso programado, idempotente, con la gracia de 3 días; el cambio de estado se audita.

## Seguimiento de complejidad

| Violación | Por qué es necesaria | Alternativa descartada porque |
|---|---|---|
| Conteo real por petición | El límite no puede quedar desfasado por un contador | Un contador guardado se rompe en concurrencia y con bajas |
| Vigencia derivada de pagos | Una sola fuente de verdad | Una columna duplicada se desincroniza con cada pago |

## Plan de PR (400 líneas efectivas o menos)

| PR | Contenido | Issue |
|---|---|---|
| 1 | Modelo de planes (`catalogo/V5`) | #154 — **fusionado** (#163) |
| 2 | Verificación de funciones y límites en cada petición | #156 |
| 3 | Vigencia, pagos y suspensión con gracia | #158 |
| 4 | Pruebas de función no incluida y de límite | #157 |
