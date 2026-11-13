# ADR-0002 · versiones del stack

| | |
|---|---|
| **Estado** | Aceptada |
| **Fecha** | 2026-11-12 |
| **Área** | 1 · Arquitectura |
| **Issue** | #35 |
| **Decide** | Backend sobre Spring Boot 4.1 y Java 25; la versión se escribe aquí y no se mueve a mitad de fase |

## Contexto

`Tecnologias_y_patrones.md` fijó la forma del stack y dejó las versiones para la fase 0. La fase 0 sigue abierta, así que el único sitio donde había algo concreto era `README.md`, con JDK 21 —una decisión de facto que ningún documento normativo respaldaba—. Y "Java LTS" ya no identifica una versión: hoy hay dos vigentes, 21 y 25.

Mientras tanto cambió el calendario. Spring Boot 3.5, la última línea de 3.x, salió del soporte OSS el 30 de junio de 2026 y 3.5.16 fue su último release gratuito. De las líneas actuales, solo 4.0 recibe parches OSS hasta el 31 de diciembre de 2026 y 4.1 hasta el 31 de julio de 2027. El soporte comercial de 3.5 llega a 2032, pero es de pago.

Un proyecto sin una línea de código de aplicación es el momento más barato de la vida del proyecto para elegir esto: hoy el coste de arrancar en 4.x es cero, y en 2027 será una migración de plataforma con módulos encima.

## Opciones consideradas

| Opción | Ventaja | Costo |
|---|---|---|
| Spring Boot 3.5 + JDK 21 | Continuidad con lo ya escrito; ecosistema maduro; lo que el equipo probablemente conoce | Nace sin parches de seguridad gratuitos; una migración de plataforma garantizada en 2027 |
| Spring Boot 4.0 + JDK 21 | Estrena la generación nueva con el JDK ya elegido | Muere el 31 de diciembre de 2026: peor que 3.5, porque obliga a dos migraciones |
| **Spring Boot 4.1 + JDK 25** | Parches OSS hasta julio de 2027; JDK 25 es LTS; el techo de Java del soporte llega a 26 | Línea joven; el equipo no la conoce; arrastra cambios de plataforma en cadena |

## Decisión

1. **Spring Boot 4.1.x** sobre **Java 25** (Temurin), con toolchain declarado en el build. Se actualiza dentro de la línea 4.1 y nunca se salta de línea a mitad de fase.
2. **Gradle con Kotlin DSL** y wrapper versionado, que es lo que ya asume `.github/CODEOWNERS` y lo que `.gitattributes` contempla para `gradlew`.
3. **Spotless con google-java-format** en el build, y `dart format` en Flutter. El "formateador" del que habla `Flujo_de_trabajo.md` §4 pasa a tener nombre, porque hoy no lo tiene y la regla "el formato lo hace el formateador" no se puede cumplir.
4. **ArchUnit** y **Micrometer con Prometheus y Grafana** entran en la tabla del stack: ya se usaban en `Estructura_proyecto.md` y `Despliegue_y_operacion.md`, y la regla de no añadir herramientas que los documentos no tengan valía también contra los documentos.

La decisión queda **condicionada al spike #40**: si dos o más piezas del plan no tienen solución razonable en esa línea, se enmienda este ADR para volver a Boot 3.5 con soporte comercial. No se reinterpreta en silencio.

## Consecuencias

### Se gana

- Una línea con parches de seguridad gratuitos hasta julio de 2027, en vez de ninguna.
- `JsonMapper` inmutable de Jackson 3 y JUnit 6 alineado con la plataforma: menos sorpresas que arrastrar 5.x/1.x.
- La decisión de versión deja de ser una pregunta abierta en cada PR.

### Se paga

- Lo que trae la línea, y hay que asumirlo entero: Spring Framework 7, Spring Security 7, Hibernate ORM 7.1, Jakarta EE 11 con Servlet 6.1, Jackson 3 —paquete `tools.jackson` en vez de `com.fasterxml.jackson`—, JUnit Jupiter 6, Testcontainers 2 y springdoc 3.
- Spring Security 7 cambia comportamientos por defecto, y los starters de Boot 4 son modulares: no es un cambio de número en el build.
- Nadie del equipo tiene horas en la línea 4.x. El spike #40 es la parte barata de ese coste.

### Queda prohibido a partir de ahora

- Arrancar cualquier módulo sobre una línea sin soporte OSS.
- Cambiar de línea de Spring Boot dentro de una fase.
- Introducir una librería que no esté en `Tecnologias_y_patrones.md` sin un ADR como este.

## Cómo se hace cumplir

| Mecanismo | Qué garantiza | Estado |
|---|---|---|
| `build.gradle.kts` con toolchain 25 | La versión de Java no depende de la máquina de cada uno | Fase 1 |
| Spike #40 | Que la línea 4.1 sostiene las cuatro piezas del plan antes de comprometerse | Abierto |
| Revisión de PR | Que una versión no cambia dentro de un PR de funcionalidad | Activo |

## Referencias

- `docs/Tecnologias_y_patrones.md` §1 y §Versiones.
- `README.md` — requisitos de la máquina.
- `docs/Despliegue_y_operacion.md` §8 — observabilidad.
- Issue #40 — spike de verificación.
