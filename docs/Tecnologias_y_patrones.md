# Tecnologías y patrones de diseño

Qué se usa, por qué, y qué formas de escribir código están prohibidas. Las versiones exactas están en §Versiones y las decide el ADR-0002.

## 1. Stack

| Capa | Tecnología | Rol | Descartado y por qué |
|---|---|---|---|
| App móvil | Flutter (Dart) | Cliente principal en Android; iOS después | React Native: no reutiliza la consola web |
| Consola web | Flutter Web | Administración de plataforma y de clínica | — |
| Identidad | Keycloak, OIDC + PKCE | Login, JWT, roles | Autenticación propia: alto riesgo y mantenimiento |
| Backend | Spring Boot 4.1 + Java 25 | API REST | Node/Nest: sin soporte equivalente de multi-tenancy en Hibernate |
| Build | Gradle con Kotlin DSL y wrapper | Compilación reproducible sin depender de la máquina | Maven: válido, pero `build.gradle.kts` ya está en CODEOWNERS |
| Formato | Spotless con google-java-format; `dart format` | El formato lo aplica la herramienta, no el revisor | Revisar el estilo a mano |
| Persistencia | Spring Data JPA + Hibernate 7.1 | ORM y resolución de tenant | JDBC puro: demasiado trabajo manual |
| Base de datos | PostgreSQL 17 | Datos, `btree_gist`, restricciones de exclusión | MySQL: sin `EXCLUDE` |
| Migraciones | Flyway | Versionado por esquema | Liquibase: más pesado para lo que se necesita |
| Seguridad | Spring Security 7, OAuth2 Resource Server | Validación de JWT y `@PreAuthorize` | — |
| Contrato de API | springdoc OpenAPI | Documento que consumen app y consola | — |
| Adjuntos | Almacenamiento compatible con S3 (RustFS en desarrollo, [ADR-0005](adr/0005-rustfs-almacenamiento-desarrollo.md)) | Archivos fuera de las tablas | Binarios en la base de datos |
| Orquestación | Docker Compose | Stack local y despliegue inicial | Kubernetes: fuera de alcance |
| Observabilidad | Micrometer con Prometheus y Grafana | Métricas y paneles de `Despliegue_y_operacion.md` §8 | — |
| Arquitectura de código | ArchUnit | Una prueba vigila que las capas no se salten | Revisión manual |
| Pruebas | JUnit 6, Testcontainers 2, MockMvc, `flutter_test` | — | — |
| Distribución Android | APK firmado y Play Store | Entrega del cliente móvil | — |
| Administración de BD | pgAdmin, solo desarrollo | — | — |
| CI/CD | GitHub Actions | Build, pruebas, firma y publicación | — |

### Versiones

Decididas en el [ADR-0002](adr/0002-versiones-del-stack.md) y registradas también en el `README`. La regla de fondo: la versión la decide un ADR, nunca un PR de funcionalidad.

| Pieza | Versión |
|---|---|
| Java | 25 (Temurin), con toolchain en el build |
| Spring Boot | 4.1.x |
| Spring Framework | 7.x |
| Spring Security | 7.x |
| Hibernate ORM | 7.1.x |
| Jakarta EE | 11, Servlet 6.1 |
| Jackson | 3 (`tools.jackson`) |
| JUnit Jupiter | 6.x |
| Testcontainers | 2.x |
| springdoc | 3.x |
| PostgreSQL | 17 |
| Keycloak | la estable al arrancar la fase 1, fijada y registrada aquí |
| Flutter | la estable al arrancar la fase 3, fijada y registrada aquí |
| RustFS (solo desarrollo) | 1.0.5, imagen `rustfs/rustfs:1.0.5` ([ADR-0005](adr/0005-rustfs-almacenamiento-desarrollo.md)) |

- Las líneas de backend llegan con el BOM de Spring Boot 4.1; esta tabla recoge lo que hay que saber, no una lista de dependencias que se declare a mano.
- Dentro de una línea se actualiza cuando hay parche de seguridad; entre líneas, nunca a mitad de fase.
- Keycloak y Flutter se fijan igual, cada uno en su fase, y se registran en esta misma tabla.
- El primer alcance no necesita macOS ni Xcode: iOS queda fuera hasta que existan Mac y cuenta de Apple Developer.

## 2. Patrones de backend

| Patrón | Dónde se aplica | Regla |
|---|---|---|
| Singleton vía contenedor | Servicios, resolutores, configuración | Sin estado; el tenant nunca vive en un campo |
| Capas | `controller → service → repository → entity/dto` | El controller no contiene reglas de negocio |
| Facade por módulo | Comunicación entre módulos | Un módulo nunca usa el repositorio de otro |
| Repository + Specification | Consultas de agenda y filtros | Las consultas complejas se componen, no se concatenan |
| DTO + Mapper | Frontera de la API | Las entidades JPA no se serializan |
| Value Objects | `Dinero`, `RangoHorario`, `Correo` | Los invariantes viven en el tipo |
| Strategy | Próxima dosis, métodos de pago, almacenamiento | Una interfaz por variante, sin `if` por tipo |
| Adapter | Keycloak Admin API, S3, futuro CFDI | El dominio no depende de librerías externas |
| Eventos de dominio | Auditoría, alertas de vacunas | Los efectos secundarios se desacoplan del caso de uso |
| Unit of Work | Transacción por request | La conexión y el esquema se resuelven al abrir la transacción |
| Template Method | Aprovisionamiento de clínica | Pasos fijos: registrar, crear esquema, migrar, crear usuario, habilitar |
| Anti-corruption layer | `catalogo` ↔ esquemas de clínica | El catálogo no conoce entidades de negocio |

## 3. Patrones y librerías de la app

| Necesidad | Elección | Alternativa |
|---|---|---|
| Acceso a la API | Repository | Llamadas HTTP desde widgets |
| Estado | Riverpod | Bloc |
| Rutas | go_router | Navigator 2.0 a mano |
| HTTP | dio | `http` |
| Modelos y serialización | freezed + json_serializable | Escritura manual |
| Autenticación OIDC (móvil) | flutter_appauth | `openid_client`, que no abre el navegador del sistema como exige el flujo |
| Autenticación OIDC (consola web) | openid_client | Adaptador JS de Keycloak: script externo y globales |
| Almacenamiento de tokens (móvil) | flutter_secure_storage — Keychain y Keystore | `shared_preferences`, no recomendado |
| Almacenamiento de tokens (consola web) | memoria, sin persistencia: recargar reautentica | `localStorage`: cualquier XSS lo lee |

Reglas asociadas:

- La UI no llama HTTP directamente; siempre pasa por un repositorio.
- Un interceptor único adjunta el token, refresca la sesión y traduce el sobre `{success, message, data}`.
- Las guardas de ruta ocultan pantallas por rol, pero **no autorizan**: el backend decide.
- Un widget usado dos veces vive en el design system, no se copia.
- La app y la consola comparten la interfaz de sesión, no la implementación: `flutter_appauth` en móvil y `openid_client` en el navegador (ADR-0004).
- En el navegador los tokens viven en memoria y CORS se abre solo al origen de la consola, nunca con `*` ni con credenciales.

## 4. Reglas de datos

- El tenant se resuelve por conexión y transacción; nunca en un campo compartido.
- Flyway con dos ubicaciones: `catalogo` y `tenant`. Toda migración de tenant se aplica a cada esquema.
- Dinero en `NUMERIC(12,2)` o enteros en centavos; nunca coma flotante.
- Folio de factura consecutivo por clínica, sin huecos, con un contador por clínica actualizado dentro de la misma transacción y restricción única.
- Los adjuntos viven fuera de la base, con referencia y URL firmada.
- Toda modificación clínica relevante queda en auditoría.

## 5. Anti-patrones prohibidos

- Guardar el tenant en un campo de bean o en una variable global.
- Validar empalmes con un `SELECT` previo en lugar de la restricción de exclusión.
- Exponer entidades JPA en la API.
- Usar `float` o `double` para dinero.
- Delegar autorización a la app.
- Consultar datos de otro módulo a través de su repositorio.
- Guardar archivos binarios en las tablas principales.
- Duplicar la pertenencia usuario-clínica en Keycloak y en base de datos a la vez.

## 6. Decisiones abiertas

1. Versiones exactas del stack, a fijar en la Fase 0.
2. Proveedor de almacenamiento en producción (S3 u otro servicio compatible). En desarrollo ya está decidido: RustFS, ADR-0005.
3. Si la facturación fiscal requiere un proveedor externo de timbrado.
