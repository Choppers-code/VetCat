# Roadmap

Cada fase termina con algo ejecutable y demostrable. Las versiones marcan cortes de entrega, no fechas.

| Fase | Entrega | Versión |
|---|---|---|
| 0 | Repositorio, Git Flow, contratos OpenAPI, ADRs, convenciones | — |
| 1 | Compose parametrizado por entorno: PostgreSQL 17, Keycloak, pgAdmin, almacenamiento de adjuntos. Primer entorno: máquina del desarrollador | — |
| 2 | Multi-tenancy: catálogo global, esquema por clínica, aprovisionamiento | — |
| 3 | Autenticación y autorización: Keycloak, roles, clínica activa | — |
| 4 | Propietarios y mascotas | v1.0 |
| 5 | Agenda: citas, consultorios, horarios, bloqueos, anti-empalme | v1.0 |
| 6 | Expediente clínico completo y auditoría | v1.0 |
| 7 | Vacunas y alertas dentro de la aplicación | v1.0 |
| 8 | Portal del dueño | v1.1 |
| 9 | Facturación y caja (catálogo, POS, comprobante) | v1.2 |
| 10 | Consola de administración de plataforma | v1.3 |
| 11 | Endurecimiento: respaldos, CI/CD, firma de APK y publicación en Play Store | v2.0 |

## Dependencias duras

1. **Multi-tenancy antes que cualquier módulo.** Un error de aislamiento compromete todos los datos de la plataforma. Se implementa y se prueba con dos clínicas antes de la fase 4.
2. **Contratos OpenAPI antes que pantallas.** El dueño del módulo escribe el contrato; la UI se construye contra él.
3. **Componentes antes que pantallas de módulo.** La persona 4 entrega design system, navegación, autenticación y cliente HTTP antes de que las personas 2 y 3 empiecen sus pantallas.
4. **Facturación después del expediente.** Necesita que existan consultas y conceptos que cobrar.
5. **Portal después del núcleo clínico.** Depende de mascotas, citas y vacunas.
6. **Nada asume la máquina local.** El primer entorno es local, pero el destino es nube: configuración por variables de entorno, migraciones como job y almacenamiento detrás de una interfaz desde la fase 1.

## Hitos

| Hito | Criterio |
|---|---|
| H1 | Todo el equipo levanta el stack con `docker compose up -d` e inicia sesión |
| H2 | Dos clínicas de prueba con datos aislados y acceso cruzado denegado |
| H3 | v1.0: agenda, propietarios, mascotas, expediente y vacunas funcionando en la app |
| H4 | v1.1: el dueño consulta sus mascotas, citas y vacunas |
| H5 | v1.2: una clínica cobra una consulta y emite comprobante con folio |
| H6 | v2.0: publicación en Play Store, respaldos y despliegue reproducible |

## Riesgos de secuencia

- Si la fase 2 se retrasa, se retrasa todo. Es preferible una implementación pequeña y probada que cinco módulos sobre una base insegura.
- Si la persona 4 no entrega los componentes a tiempo, las personas 2 y 3 inventan widgets propios y la interfaz se fragmenta.
