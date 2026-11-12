> **Obsoleto.** Documento v1.0 conservado como referencia histórica. No describe el proyecto actual: la fuente de verdad está en `README.md` y en los documentos vigentes de `docs/`.

# VetCat — Propuesta de Sistema SaaS para Clínicas Veterinarias

> **Versión:** 1.0 · **Fecha:** 2026-11-06 · **Estado:** Propuesta técnica

---

## 1. Resumen ejecutivo

**VetCat** es una plataforma SaaS (Software as a Service) multi-clínica que digitaliza la operación diaria de las clínicas veterinarias pequeñas: agenda de citas, expediente clínico electrónico, control de vacunas y un portal para que los dueños de las mascotas consulten el estado de sus animales.

Cada clínica se registra como un **tenant** (inquilino) y obtiene su propio espacio de trabajo con datos aislados, mientras todas comparten la misma aplicación. El producto nace **web-first** y está pensado para extenderse a **móvil** y **escritorio** sin reescribir el cliente.

---

## 2. Problema

Muchas clínicas veterinarias pequeñas gestionan sus citas con cuadernos o WhatsApp y llevan los expedientes médicos en papel. Esta falta de un sistema formal y centralizado ocasiona:

| Problema | Consecuencia en la clínica | Cómo lo resuelve VetCat |
|---|---|---|
| **Citas empalmadas** | Dos pacientes a la misma hora, tiempos muertos, mala experiencia | Agenda centralizada con validación de disponibilidad por veterinario/consultorio |
| **Historiales perdidos** | Diagnósticos repetidos, riesgo clínico, pérdida de información | Expediente clínico digital por mascota, con historial cronológico y respaldo |
| **Vacunas olvidadas** | Mascotas desprotegidas, pérdida de ingresos recurrentes | Cartilla de vacunación digital con próximas dosis y alertas de vencimiento |
| **Dueños sin información** | Llamadas constantes, desconfianza, baja fidelización | Portal del dueño: citas, vacunas e historial de sus mascotas |

---

## 3. Objetivos y alcance

### 3.1 Objetivos
- Eliminar el empalme de citas.
- Centralizar y conservar el historial médico de cada mascota.
- Reducir el olvido de vacunas mediante un calendario y alertas.
- Dar transparencia al dueño con acceso a la información de su mascota.
- Permitir que **múltiples clínicas** usen la misma plataforma con **aislamiento total de datos**.

### 3.2 Alcance del MVP (Fase 1 — Web)

| Módulo | Funcionalidades |
|---|---|
| **Gestión de clínicas (multi-tenant)** | Alta de clínica, configuración (horarios, consultorios, servicios), usuarios y roles |
| **Agenda de citas** | Crear/reprogramar/cancelar, vista diaria/semanal, bloqueo de empalmes, estados (pendiente, confirmada, atendida, cancelada) |
| **Propietarios y mascotas** | Registro de dueños y mascotas (especie, raza, edad, peso, datos relevantes) |
| **Expediente clínico** | Consultas, diagnósticos, tratamientos, recetas, notas y archivos adjuntos |
| **Vacunas** | Cartilla de vacunación, aplicación de dosis, cálculo de próxima fecha, listado de vacunas vencidas/próximas |
| **Portal del dueño** | Acceso con su propia cuenta para ver sus mascotas, próximas citas, vacunas y resumen del historial |

### 3.3 Fuera de alcance del MVP (futuras fases)
- Aplicación **móvil** (Flutter).
- Aplicación de **escritorio** (Flutter).
- Facturación, inventario y punto de venta *(a evaluar)*.
- Recordatorios automáticos por WhatsApp/SMS *(a evaluar; en el MVP las alertas se muestran dentro del sistema)*.

---

## 4. Actores y roles

| Rol | Descripción | Permisos principales |
|---|---|---|
| **Administrador de plataforma** | Equipo VetCat | Alta/baja de clínicas, soporte, monitoreo |
| **Administrador de clínica** | Dueño/gerente de la veterinaria | Configura la clínica, gestiona personal, accede a todo su tenant |
| **Veterinario** | Profesional clínico | Gestiona citas, expedientes, vacunas y recetas |
| **Recepcionista** | Personal administrativo | Gestiona agenda, propietarios y mascotas; sin acceso a datos clínicos sensibles |
| **Dueño de mascota** | Cliente final | Solo lectura de **sus** mascotas, citas y vacunas |

Los roles se definen y aplican mediante **Keycloak** y se validan de nuevo en el backend.

---

## 5. Arquitectura propuesta

### 5.1 Arquitectura híbrida (por capas) — modelo *bridge*

Se adopta una **arquitectura híbrida** que combina los dos extremos clásicos del multi-tenant SaaS:

- **Pool (compartido):** *frontend* y *backend* son **una sola instancia compartida** por todas las clínicas. Esto reduce costos operativos, simplifica los despliegues y permite actualizar a todos los clientes a la vez.
- **Silo (aislado):** el **almacenamiento** se separa por clínica. Cada tenant tiene sus datos aislados de los demás, lo que protege información sensible y facilita respaldos, exportaciones y baja de un cliente.

```
                        ┌───────────────────────────────┐
                        │   Flutter Web (compartido)    │   ← POOL
                        └───────────────┬───────────────┘
                                        │ HTTPS + JWT
                        ┌───────────────▼───────────────┐
                        │  Keycloak (OAuth2 / OIDC)     │
                        └───────────────┬───────────────┘
                                        │ token con tenant_id y rol
                        ┌───────────────▼───────────────┐
                        │ Spring Boot — API REST        │   ← POOL
                        │ (resuelve tenant por request) │
                        └───────────────┬───────────────┘
                                        │
              ┌─────────────────────────▼─────────────────────────┐
              │                PostgreSQL 17                      │   ← SILO
              │  ┌──────────┐ ┌──────────────┐ ┌──────────────┐   │
              │  │ catálogo │ │ clinica_001  │ │ clinica_002  │...│
              │  │ (global) │ │ (esquema)    │ │ (esquema)    │   │
              │  └──────────┘ └──────────────┘ └──────────────┘   │
              └───────────────────────────────────────────────────┘
```

**Cómo se implementa el silo:** un **esquema de PostgreSQL por clínica** (`schema-per-tenant`), más un esquema global `catalogo` con el registro de tenants. El backend identifica el tenant a partir del *claim* `tenant_id` del token JWT y selecciona el esquema en cada petición (mecanismo *multi-tenancy* de Hibernate: `CurrentTenantIdentifierResolver` + `MultiTenantConnectionProvider`).

> **Evolución:** si una clínica grande o un plan premium requiere aislamiento total, el mismo diseño permite moverla a su **propia base de datos** sin cambiar el código de negocio, solo la resolución de la conexión.

### 5.2 Organización interna del backend (capas)

```
com.vetcat
 ├── config/            → Seguridad, multi-tenancy, CORS, OpenAPI
 ├── tenant/            → Resolución de tenant y aprovisionamiento de esquemas
 ├── citas/             ┐
 ├── propietarios/      │  Cada módulo con sus capas:
 ├── mascotas/          │  controller → service → repository → entity/dto
 ├── expedientes/       │
 └── vacunas/           ┘
```

- **Controller (API REST):** expone los endpoints y valida entradas.
- **Service (negocio):** reglas como "no permitir citas empalmadas" o "calcular próxima vacuna".
- **Repository (persistencia):** acceso a datos con Spring Data JPA.
- **Módulos por dominio:** facilitan el mantenimiento y permiten extraer servicios en el futuro si fuera necesario.

### 5.3 Patrón de diseño: Singleton

En VetCat el patrón **Singleton** se aplica a través del **contenedor de Spring**: por defecto, cada *bean* (`@Service`, `@Component`, `@Repository`, `@Configuration`) existe **una sola instancia por aplicación**.

**Dónde aplica:**
- Servicios de negocio (`CitaService`, `VacunaService`, `ExpedienteService`).
- Resolutor de tenant y proveedor de conexiones multi-tenant.
- Configuración de seguridad y clientes hacia Keycloak.

**Por qué esta forma y no un Singleton manual:** Spring gestiona el ciclo de vida, la inyección de dependencias y la seguridad en hilos de forma estándar y testeable (se pueden sustituir por *mocks* en pruebas), evitando el estado global oculto y los problemas de concurrencia del Singleton clásico.

> **Regla de diseño:** los *beans* singleton deben ser **sin estado** (*stateless*). Los datos específicos de cada petición (p. ej., el tenant actual) viajan en el contexto de la petición, nunca en campos del servicio.

---

## 6. Tecnologías y justificación

### 6.1 Frontend — Flutter (Web First)

**Por qué Flutter:**
- Una **única base de código** para web, móvil y escritorio. Como el roadmap incluye **móvil y desktop**, se reutiliza la mayor parte de la UI y la lógica, reduciendo tiempo y costo.
- Interfaces ricas y consistentes (calendarios, tablas, formularios complejos), adecuadas para una agenda de citas.
- Se enfoca en **Web First**: el SaaS se entrega por navegador, sin instalaciones en la clínica.

**Consideración:** Flutter Web no es ideal para SEO, pero VetCat es una aplicación autenticada (no un sitio de contenido), por lo que no es una limitación relevante. El sitio de marketing, si se requiere, se hace aparte.

### 6.2 Backend — Spring Boot (Java) y API REST

**Por qué Spring Boot:**
- Ecosistema maduro y estable para sistemas empresariales: seguridad (Spring Security), persistencia (Spring Data JPA), validación y documentación (OpenAPI).
- Soporte nativo para **OAuth2 Resource Server**, que valida los tokens emitidos por Keycloak con poca configuración.
- Buen soporte para **multi-tenancy** vía Hibernate.
- Su contenedor de inyección de dependencias materializa el patrón Singleton de forma natural.

**Por qué API REST:**
- Contrato claro y estándar, **independiente del cliente**: la misma API atiende hoy a Flutter Web y mañana a las apps móvil y desktop.
- Sin estado en el servidor (autenticación por token), lo que facilita escalar horizontalmente la capa compartida (*pool*).

### 6.3 Base de datos — PostgreSQL 17 (Alpine) y pgAdmin

**Por qué PostgreSQL:**
- Base relacional robusta para datos con integridad fuerte (citas, expedientes, vacunas).
- **Esquemas nativos**: base ideal para el aislamiento *schema-per-tenant* del modelo silo.
- Restricciones avanzadas: p. ej., un *exclusion constraint* sobre rangos de tiempo puede **impedir a nivel de base de datos** que se empalmen citas del mismo veterinario o consultorio.
- Soporte de `JSONB` para datos clínicos semiestructurados y de tipos de rango para horarios.

**Por qué la imagen Alpine:** imagen más ligera (menor tamaño, menor superficie de ataque, descargas y arranques más rápidos).

**Por qué pgAdmin:** herramienta de administración visual para desarrollo y soporte (consultas, revisión de esquemas por tenant, respaldos). Se usa **solo en entornos de desarrollo/administración**, nunca expuesta públicamente en producción.

### 6.4 Autenticación y roles — Keycloak (OAuth2 / OpenID Connect)

**Por qué Keycloak:**
- Evita construir y mantener un sistema de autenticación propio (un área de alto riesgo): login, recuperación de contraseña, MFA, bloqueo por intentos y gestión de sesiones vienen resueltos.
- Implementa estándares **OAuth2 / OpenID Connect**, compatibles con Spring Boot y Flutter, y reutilizables en móvil y desktop.
- Gestión centralizada de **roles** (administrador, veterinario, recepcionista, dueño) y **usuarios por clínica**.
- Emite **JWT** con claims personalizados (`tenant_id`, `roles`), que es el puente entre la identidad y el aislamiento de datos.

**Estrategia multi-tenant en Keycloak:** un único *realm* compartido (coherente con el modelo *pool*), donde cada usuario pertenece a una clínica mediante el atributo `tenant_id` (o la funcionalidad *Organizations* de Keycloak). Esto evita multiplicar realms al crecer el número de clínicas.

### 6.5 Contenedores — Docker y Docker Compose

**Por qué Docker:**
- Entornos **idénticos** en desarrollo, pruebas y producción ("funciona en mi máquina" deja de ser un problema).
- Cada componente (backend, base de datos, Keycloak, pgAdmin, frontend) se empaqueta con sus dependencias.

**Por qué Docker Compose:**
- Levanta **todo el stack con un solo comando** (`docker compose up`), ideal para onboarding de desarrolladores y para despliegues iniciales.
- Define redes, volúmenes (persistencia de PostgreSQL) y variables de entorno de forma declarativa.

### 6.6 Control de versiones — Git y Git Flow

**Por qué Git + Git Flow:**
- Trazabilidad completa y trabajo colaborativo ordenado.
- Git Flow estructura el ciclo de vida del software:

| Rama | Propósito |
|---|---|
| `main` | Código estable en producción (etiquetado por versión) |
| `develop` | Integración continua de funcionalidades |
| `feature/*` | Desarrollo de cada funcionalidad |
| `release/*` | Preparación y estabilización de una versión |
| `hotfix/*` | Correcciones urgentes sobre producción |

---

## 7. Cómo se complementan las tecnologías

```
 Usuario ──► Flutter Web ──(1) login──► Keycloak
                │                          │
                │◄────(2) JWT con ─────────┘
                │      tenant_id + rol
                │
                └──(3) API REST + JWT──► Spring Boot
                                            │ valida JWT (Keycloak)
                                            │ extrae tenant_id → elige esquema
                                            ▼
                                       PostgreSQL 17
                                       (esquema de la clínica)

 Todo el conjunto se empaqueta y orquesta con Docker Compose
 y el código se gestiona con Git / Git Flow.
```

**Flujo paso a paso:**

1. El usuario (veterinario, recepcionista o dueño) abre **Flutter Web** e inicia sesión.
2. Flutter lo redirige a **Keycloak** (flujo OAuth2/OIDC, Authorization Code + PKCE). Keycloak valida las credenciales y devuelve un **JWT** con el rol y el `tenant_id`.
3. Flutter llama a la **API REST de Spring Boot** adjuntando el JWT.
4. Spring Boot **valida la firma del token** contra Keycloak, aplica la autorización por rol y **resuelve el tenant**.
5. Hibernate dirige la consulta al **esquema de PostgreSQL** de esa clínica; los datos de otra clínica son inalcanzables.
6. La respuesta vuelve a Flutter y se muestra al usuario.

**Sinergias clave:**

| Combinación | Beneficio |
|---|---|
| Keycloak + Spring Boot | Autenticación estándar y delegada; el backend solo valida tokens |
| Keycloak + PostgreSQL (vía `tenant_id`) | La identidad determina el aislamiento de los datos |
| Spring Boot + PostgreSQL | Multi-tenancy por esquema con restricciones que evitan empalmes de citas |
| API REST + Flutter | Un solo backend para web hoy, móvil y desktop mañana |
| Docker Compose + todo el stack | Entorno reproducible y despliegue simple |
| Git Flow + Docker | Versiones etiquetadas que corresponden a imágenes desplegables |

---

## 8. Modelo de datos (alto nivel)

**Esquema global `catalogo`:**
- `clinica` (id, nombre, esquema, plan, estado, fecha_alta)

**Esquema por clínica (`clinica_xxx`):**
- `usuario_clinica` (id, keycloak_id, rol, datos del perfil)
- `propietario` (id, nombre, teléfono, correo, keycloak_id *opcional para el portal*)
- `mascota` (id, propietario_id, nombre, especie, raza, fecha_nacimiento, peso, notas)
- `cita` (id, mascota_id, veterinario_id, inicio, fin, motivo, estado)
- `consulta` (id, cita_id, mascota_id, diagnóstico, tratamiento, receta, notas)
- `adjunto` (id, consulta_id, ruta, tipo)
- `vacuna_catalogo` (id, nombre, especie, intervalo_dias)
- `vacuna_aplicada` (id, mascota_id, vacuna_id, fecha_aplicacion, proxima_dosis, lote)

> **Anti-empalme:** restricción de exclusión en `cita` sobre `(veterinario_id, rango_tiempo)` excluyendo citas canceladas, de modo que la base de datos garantice la integridad aunque dos usuarios reserven al mismo tiempo.

---

## 9. Seguridad y aislamiento de datos

- **Autenticación delegada** a Keycloak (OIDC, Authorization Code + PKCE).
- **Autorización por rol** en el backend (`@PreAuthorize`), no solo en la interfaz.
- **Aislamiento por esquema**: el tenant se obtiene **exclusivamente del token firmado**, nunca de parámetros enviados por el cliente.
- **Portal del dueño**: el rol *dueño* solo accede a mascotas ligadas a su propio `keycloak_id`.
- **HTTPS** obligatorio en producción y secretos fuera del repositorio (variables de entorno).
- **Respaldos** por esquema, que permiten restaurar o entregar los datos de una sola clínica.
- Cumplimiento de buenas prácticas de **protección de datos personales** de propietarios (aviso de privacidad, mínimo dato necesario).

---

## 10. Estrategia de despliegue (Docker Compose)

Servicios previstos en `docker-compose.yml`:

| Servicio | Imagen / origen | Notas |
|---|---|---|
| `postgres` | `postgres:17-alpine` | Volumen persistente |
| `pgadmin` | `dpage/pgadmin4` | Solo dev/admin |
| `keycloak` | `quay.io/keycloak/keycloak` | Usa su propia base/esquema en PostgreSQL |
| `backend` | Imagen propia (Spring Boot) | Depende de `postgres` y `keycloak` |
| `frontend` | Imagen propia (Flutter Web servido con Nginx) | Expuesto por proxy inverso |

---

## 11. Flujo de trabajo con Git Flow

1. Cada historia de usuario → rama `feature/<nombre>` desde `develop`.
2. Revisión por *pull request* y merge a `develop`.
3. Al cerrar un conjunto de funcionalidades → `release/x.y.z`, pruebas y merge a `main` con etiqueta de versión.
4. Errores en producción → `hotfix/*` desde `main`, con merge a `main` y `develop`.

---

## 12. Roadmap

| Fase | Contenido |
|---|---|
| **Fase 0 — Fundamentos** | Repositorio, Git Flow, Docker Compose, Keycloak configurado, esqueleto de backend y frontend, aprovisionamiento de tenants |
| **Fase 1 — MVP Web** | Propietarios y mascotas, agenda sin empalmes, expediente clínico, vacunas |
| **Fase 2 — Portal del dueño** | Cuentas de dueño, vista de mascotas, citas, vacunas e historial |
| **Fase 3 — Endurecimiento** | Pruebas, respaldos, auditoría, monitoreo, documentación |
| **Fase 4 — Móvil** | App Flutter para Android/iOS reutilizando la API |
| **Fase 5 — Desktop** | App Flutter de escritorio |

---

## 13. Riesgos y mitigaciones

| Riesgo | Mitigación |
|---|---|
| Fuga de datos entre clínicas | Tenant tomado solo del JWT; pruebas automatizadas de aislamiento; esquema por tenant |
| Crecimiento del número de esquemas | Automatizar migraciones (Flyway/Liquibase) por esquema; plan de migración a BD dedicada |
| Complejidad operativa de Keycloak | Configuración como código (export de realm) versionada en Git |
| Adopción por personal poco técnico | UI simple, flujos cortos, capacitación y migración asistida de datos en papel |
| Flutter Web: rendimiento en equipos modestos | Optimizar renderizado y carga; pruebas en hardware típico de clínica |

---

## 14. Conclusión

VetCat combina tecnologías estándar y maduras con un enfoque de **SaaS híbrido (pool + silo)** que equilibra **costo bajo** (aplicación compartida) y **seguridad alta** (datos aislados por clínica). Su base en API REST, OAuth2/OIDC y Flutter permite crecer a móvil y escritorio sin rehacer la plataforma, resolviendo desde el primer día los problemas de citas empalmadas, historiales perdidos, vacunas olvidadas y falta de información para los dueños.

---

## 15. Supuestos a validar

- El aislamiento por **esquema de PostgreSQL** (silo) es suficiente para el MVP; la base de datos dedicada queda como opción para planes superiores.
- Keycloak usará **un solo realm** con `tenant_id` como atributo.
- Las alertas de vacunas en el MVP serán **dentro del sistema**; el envío por WhatsApp/correo/SMS queda para una fase posterior.
- El modelo de cobro (suscripción mensual por clínica/plan) se definirá por separado.
