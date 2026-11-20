# Roadmap

Cada fase termina con algo ejecutable y demostrable. Las versiones marcan cortes de entrega, no fechas.

| Fase | Entrega | Versión |
|---|---|---|
| 0 | Repositorio, Git Flow, contratos OpenAPI, ADRs, convenciones y matriz de roles | — |
| 1 | Compose parametrizado por entorno: PostgreSQL 17, Keycloak, pgAdmin y buzón de correo de pruebas. Primer entorno: máquina del desarrollador | — |
| 2 | Multi-tenancy: catálogo global, esquema por clínica, aprovisionamiento | — |
| 3 | Autenticación y autorización: Keycloak, roles, varios roles por usuario, clínica activa, planes y funciones por plan | — |
| 4 | Propietarios y mascotas | v1.0 |
| 5 | Agenda: citas, consultorios, horarios, bloqueos, anti-empalme, solicitud de cita y de cancelación (backend) | v1.0 |
| 6 | Expediente clínico con versiones y auditoría | v1.0 |
| 7 | Vacunas con catálogo por clínica y alertas dentro de la aplicación | v1.0 |
| 8 | Portal del cliente, solicitud de cita (pantalla) y notificaciones (bandeja y push) | v1.1 |
| 9 | Ventas y caja opcionales (catálogo, POS, ticket), inventario y reportes | v1.2 |
| 10 | Consola de plataforma con planes, pagos y vigencia | v1.3 |
| 11 | Endurecimiento: respaldos, CI/CD, firma de APK y publicación en Play Store | v2.0 |

## Dependencias duras

1. **Multi-tenancy antes que cualquier módulo.** Un error de aislamiento compromete todos los datos de la plataforma. Se implementa y se prueba con dos clínicas antes de la fase 4.
2. **Contratos OpenAPI antes que pantallas.** El dueño del módulo escribe el contrato; la UI se construye contra él.
3. **Componentes antes que pantallas de módulo.** La persona 4 entrega design system, navegación, autenticación y cliente HTTP antes de que las personas 2 y 3 empiecen sus pantallas.
4. **Facturación después del expediente.** Necesita que existan consultas y conceptos que cobrar.
5. **Los planes y las funciones habilitadas antes que cualquier módulo opcional** (ventas, inventario, reportes, recordatorios).
6. **Portal después del núcleo clínico.** Depende de mascotas, citas y vacunas.
7. **Nada asume la máquina local.** El primer entorno es local, pero el destino es nube: configuración por variables de entorno y migraciones como job desde la fase 1.

## Hitos

| Hito | Criterio |
|---|---|
| H1a | Todo el equipo levanta el stack con `docker compose up -d` y ve los servicios en `healthy` (fase 1) |
| H1b | Un usuario del realm `vetcat` entra y obtiene token (fase 3) |
| H2 | Dos clínicas de prueba con datos aislados y acceso cruzado denegado |
| H3 | v1.0: agenda, propietarios, mascotas, expediente y vacunas funcionando en la app |
| H4 | v1.1: el cliente consulta sus mascotas, citas y vacunas, solicita citas y recibe notificaciones |
| H5 | v1.2: una clínica cobra una consulta con ticket y controla su inventario si su plan lo incluye |
| H6 | v2.0: publicación en Play Store, respaldos y despliegue reproducible |

## Riesgos de secuencia

- Si la fase 2 se retrasa, se retrasa todo. Es preferible una implementación pequeña y probada que cinco módulos sobre una base insegura.
- Si la persona 4 no entrega los componentes a tiempo, las personas 2 y 3 inventan widgets propios y la interfaz se fragmenta.
