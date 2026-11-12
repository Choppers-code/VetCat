> **Obsoleto.** Documento v1.0 conservado como referencia histórica. No describe el proyecto actual: la fuente de verdad está en `README.md` y en los documentos vigentes de `docs/`.

# Plan de implementación y distribución del trabajo — VetCat

Después de revisar `Propuesta_VetCat.md`, mi recomendación es desarrollar VetCat como un proyecto de equipo organizado en 5 responsabilidades técnicas, trabajando por fases y con integraciones frecuentes.

No recomiendo que cada integrante desarrolle una parte completamente independiente y después intenten unirla. Eso suele generar problemas de integración, incompatibilidad entre endpoints, modelos de datos diferentes y retrasos.

La estrategia será construir primero una base funcional compartida y después desarrollar los módulos de manera paralela.

## 1. ¿Qué vamos a construir y en qué orden?

VetCat tendrá cinco componentes principales:

# VetCat

SaaS multi-clínica

Aplicación web para clínicas veterinarias

Flutter Web

Interfaz de veterinarios, recepcionistas, administradores y propietarios

Keycloak

Autenticación, JWT y roles

Spring Boot — API REST

Reglas de negocio, autorización y resolución de tenants

PostgreSQL 17

Catálogo global y esquema independiente por clínica

Docker Compose

Levanta y conecta los servicios del proyecto.

La construcción debe seguir este orden:

| Orden | Fase                | Resultado                        |
| ----- | ------------------- | -------------------------------- |
| 1     | Fundamentos         | Repositorio y entorno funcional  |
| 2     | Infraestructura     | PostgreSQL, Keycloak y Docker    |
| 3     | Backend base        | API conectada y segura           |
| 4     | Multi-tenancy       | Clínicas y aislamiento de datos  |
| 5     | Módulos principales | Mascotas, propietarios y citas   |
| 6     | Módulos clínicos    | Expedientes y vacunas            |
| 7     | Frontend            | Interfaces conectadas a la API   |
| 8     | Portal del dueño    | Consulta de información personal |
| 9     | Integración final   | Pruebas, seguridad y despliegue  |

Una decisión importante: aunque el frontend se desarrollará desde las primeras fases, no conviene construir todas sus pantallas antes de definir los contratos de la API.

## 2. Distribución del trabajo para 5 personas

Propongo una distribución por especialidades, con un responsable principal de cada área. Todos deben conocer la arquitectura general, pero no todos necesitan implementar todos los componentes.

## Equipo de desarrollo

# 1

Líder técnico / Backend e integración

Responsabilidad transversal

Arquitectura, contratos, seguridad y coordinación

# 2

Backend — Gestión veterinaria

Propietarios, mascotas y expedientes

# 3

Backend — Agenda y vacunas

Citas, disponibilidad y vacunación

# 4

Frontend — Flutter Web

Interfaces, navegación y conexión con API

# 5

DevOps, QA y seguridad

Docker, pruebas, CI/CD y documentación técnica

Esta distribución evita que una sola persona tenga que desarrollar toda la API y permite que el frontend trabaje paralelamente.

### Persona 1. Líder técnico y responsable de integración

Objetivo: mantener la coherencia técnica del proyecto y asegurar que los componentes funcionen juntos.

Responsabilidades:

- Definir la estructura general del backend.
- Crear el repositorio y establecer Git Flow.
- Definir los estándares de código y convenciones.
- Configurar Spring Security y la integración con Keycloak.
- Definir el mecanismo de resolución del `tenant_id`.
- Coordinar los contratos de los endpoints.
- Revisar Pull Requests.
- Resolver conflictos de integración.
- Supervisar la implementación del aislamiento entre clínicas.

Entregables:

- Repositorio organizado.
- Arquitectura inicial del backend.
- Configuración de seguridad.
- Contrato de la API.
- Integración funcional entre los módulos.

Importante: el líder no debe convertirse en la persona que termina haciendo todo lo que los demás no alcanzaron. Su responsabilidad principal es integrar, revisar y desbloquear al equipo.

### Persona 2. Backend — Propietarios, mascotas y expedientes

Objetivo: implementar la gestión de información de los pacientes y sus propietarios.

Responsabilidades:

1\. Crear entidades y relaciones:

- Propietario.
- Mascota.
- Consulta.
- Diagnóstico.
- Tratamiento.
- Receta.
- Notas clínicas.
- Archivos adjuntos.

2. Implementar operaciones CRUD.
3. Crear DTOs y validaciones.
4. Implementar servicios y repositorios.
5. Relacionar las consultas con sus mascotas y propietarios.
6. Respetar el aislamiento por tenant.
7. Crear pruebas unitarias e integración.

Entregables:

- API de propietarios.
- API de mascotas.
- API de expedientes clínicos.
- Pruebas de los servicios.

### Persona 3. Backend — Agenda y vacunas

Objetivo: desarrollar los procesos de programación de citas y control de vacunación.

Responsabilidades:

Agenda:

- Registrar citas.
- Consultar disponibilidad.
- Reprogramar citas.
- Cancelar citas.
- Gestionar estados.
- Evitar empalmes por veterinario y consultorio.

Vacunas:

- Consultar catálogo de vacunas.
- Registrar aplicaciones.
- Calcular próximas dosis.
- Identificar vacunas vencidas.
- Identificar vacunas próximas a vencer.

Entregables:

- API de citas.
- API de vacunas.
- Validación de disponibilidad.
- Pruebas de concurrencia para evitar citas empalmadas.

### Persona 4. Frontend — Flutter Web

Objetivo: construir la interfaz que utilizarán los diferentes usuarios.

Responsabilidades:

- Crear estructura del proyecto Flutter.
- Diseñar componentes reutilizables.
- Implementar navegación.
- Crear formularios.
- Conectar con Spring Boot.
- Implementar autenticación con Keycloak mediante OIDC y PKCE.
- Manejar sesiones y errores HTTP.
- Implementar interfaces según los permisos de cada rol.

Pantallas principales:

| Pantalla         | Función                     |
| ---------------- | --------------------------- |
| Login            | Inicio de sesión            |
| Dashboard        | Resumen de la clínica       |
| Agenda           | Calendario de citas         |
| Propietarios     | Administración de clientes  |
| Mascotas         | Expedientes de pacientes    |
| Expediente       | Historial clínico           |
| Vacunas          | Cartilla y vencimientos     |
| Usuarios         | Gestión del personal        |
| Portal del dueño | Información de sus mascotas |

Entregables:

- Aplicación Flutter Web.
- Componentes reutilizables.
- Integración con API.
- Navegación por roles.

### Persona 5. DevOps, QA y seguridad

Objetivo: garantizar que el proyecto pueda ejecutarse correctamente, sea reproducible y cumpla los requisitos de seguridad.

Responsabilidades:

- Crear `docker-compose.yml`.
- Configurar PostgreSQL 17.
- Configurar pgAdmin.
- Configurar Keycloak.
- Crear volúmenes persistentes.
- Configurar redes y variables de entorno.
- Preparar migraciones de base de datos.
- Implementar pruebas de integración.
- Validar el aislamiento entre clínicas.
- Documentar instalación y ejecución.
- Preparar el despliegue.

Entregables:

- Docker Compose funcional.
- Configuración exportable de Keycloak.
- Migraciones de base de datos.
- Pruebas automatizadas.
- Manual de instalación.

Nota: esta persona no debe esperar a que los demás terminen para empezar. Desde el primer día debe proporcionar un entorno reproducible para todos.

## 3. Orden de implementación: desde cero

### Fase 0. Preparación del proyecto



Prioridad máxima

&#x20;Objetivo: que los cinco integrantes puedan trabajar desde un entorno común.



Antes de programar funcionalidades, deben preparar las herramientas y definir las reglas de trabajo.

Paso 1. Crear el repositorio

Crear un repositorio en GitHub:

```
VetCat/
├── backend/
├── frontend/
├── database/
├── infrastructure/
│   ├── docker/
│   ├── keycloak/
│   └── nginx/
├── docs/
│   ├── architecture/
│   ├── api/
│   ├── database/
│   └── manuals/
└── README.md
```

Cada directorio tendrá una responsabilidad:

- `backend`: Spring Boot.
- `frontend`: Flutter Web.
- `database`: scripts y migraciones.
- `infrastructure`: Docker, Keycloak y configuración de servicios.
- `docs`: documentación técnica y decisiones arquitectónicas.

Paso 2. Configurar Git Flow

Crear las ramas iniciales:

```
git init

git branch -M main

git checkout -b develop

git push -u origin main
git push -u origin develop
```

Las ramas de trabajo se crean desde `develop`:

```
git checkout develop
git pull origin develop

git checkout -b feature/backend-base
```

Convenciones recomendadas:

| Rama    | Ejemplo                     |
| ------- | --------------------------- |
| feature | `feature/registro-mascotas` |
| feature | `feature/agenda-citas`      |
| feature | `feature/login-keycloak`    |
| bugfix  | `bugfix/validacion-citas`   |
| release | `release/1.0.0`             |

Reglas:

1. Nadie trabaja directamente en `main`.
2. Nadie trabaja directamente en `develop`.
3. Cada funcionalidad tiene su propia rama.
4. Todo cambio se integra mediante Pull Request.
5. El código debe compilar y pasar sus pruebas antes de integrarse.
6. La persona 1 revisa las integraciones.

Paso 3. Establecer los contratos de comunicación

Antes de que los integrantes 2, 3 y 4 desarrollen sus módulos, deben acordar cómo se comunicarán.

Por ejemplo:

```
GET    /api/v1/mascotas
POST   /api/v1/mascotas
GET    /api/v1/mascotas/{id}
PUT    /api/v1/mascotas/{id}
DELETE /api/v1/mascotas/{id}
```

La API debe tener respuestas consistentes:

```
{
  "success": true,
  "message": "Mascota registrada correctamente",
  "data": {
    "id": 1,
    "nombre": "Michi",
    "especie": "Gato"
  }
}
```

Y errores consistentes:

```
{
  "success": false,
  "message": "No se encontró la mascota",
  "data": null
}
```

La estructura es ilustrativa: deben definir los DTOs reales y documentarlos con OpenAPI antes de implementar todas las pantallas.

Resultado de esta fase:

- Repositorio creado.
- Estructura de carpetas.
- Git Flow configurado.
- Responsabilidades asignadas.
- Contratos iniciales de API.
- Documento de arquitectura compartido.

### Fase 1. Infraestructura con Docker

Aquí comienza el trabajo técnico que habilita al resto del equipo.

Responsable principal: persona 5.

La primera versión de `docker-compose.yml` debe levantar:

| Contenedor      | Función               |
| --------------- | --------------------- |
| PostgreSQL 17   | Base de datos         |
| pgAdmin         | Administración visual |
| Keycloak        | Autenticación y roles |
| Spring Boot     | API REST              |
| Flutter + Nginx | Aplicación web        |

No es necesario desarrollar todo simultáneamente. Primero deben levantar PostgreSQL, pgAdmin y Keycloak; después integrar backend y frontend.

Orden de ejecución:

1. Crear el proyecto Docker.
2. Configurar PostgreSQL.
3. Crear usuario y base de datos.
4. Conectar pgAdmin.
5. Configurar Keycloak.
6. Crear el realm de VetCat.
7. Configurar el cliente de Flutter.
8. Exportar la configuración de Keycloak.
9. Incorporar los servicios de backend y frontend.

La estructura inicial:

```
infrastructure/
├── docker/
│   ├── docker-compose.yml
│   ├── .env.example
│   └── postgres/
├── keycloak/
│   └── vetcat-realm.json
└── nginx/
    └── default.conf
```

Comandos para levantar y detener el entorno:

```
docker compose up -d
```

```
docker compose down
```

Para detenerlo sin eliminar los datos persistentes, no deben utilizar `docker compose down -v`.

Resultado esperado: los cinco integrantes pueden clonar el repositorio y ejecutar el mismo entorno con un solo comando.

### Fase 2. Base de datos y multi-tenancy

Bloqueante para los módulos

Responsables: persona 1 y persona 5.

Esta fase es especialmente importante porque VetCat no es una aplicación para una sola veterinaria. Es un SaaS donde varias clínicas comparten la aplicación, pero no sus datos.

La propuesta establece un esquema global `catalogo` y un esquema por clínica.&#x20;

Propuesta_VetCat.md



Ejemplo:

Base de datos: `vetcat_db`

catalogo

Global

Registro de clínicas y configuración global

clinica_001

Propietarios, mascotas, citas, expedientes y vacunas.

clinica_002

Sus propios registros, completamente aislados.

Paso 1. Crear el catálogo global

Tabla `catalogo.clinica`:

| Campo      | Función              |
| ---------- | -------------------- |
| id         | Identificador        |
| nombre     | Nombre de la clínica |
| esquema    | Nombre del esquema   |
| plan       | Plan contratado      |
| estado     | Activa o inactiva    |
| fecha_alta | Fecha de registro    |

Paso 2. Crear el esquema de una clínica

Por ejemplo, `clinica_001`:

```
clinica_001/
├── usuario_clinica
├── propietario
├── mascota
├── cita
├── consulta
├── adjunto
├── vacuna_catalogo
└── vacuna_aplicada
```

Paso 3. Implementar el aprovisionamiento

Cuando una clínica se registre, el backend debe:

1. Validar los datos de la clínica.
2. Crear su registro en `catalogo.clinica`.
3. Generar un nombre de esquema seguro mediante un identificador interno.
4. Crear el esquema.
5. Ejecutar las migraciones de tablas.
6. Registrar al administrador de esa clínica en Keycloak.
7. Vincular su identidad con el tenant.
8. Habilitar el acceso.

No permitan que el usuario envíe directamente el nombre del esquema que quiere utilizar. El servidor debe generarlo y validarlo.

Paso 4. Implementar el resolver de tenant

La propuesta utiliza `CurrentTenantIdentifierResolver` y `MultiTenantConnectionProvider`.&#x20;

Propuesta_VetCat.md



El flujo será:

```
Petición HTTP
     |
     v
Validar JWT
     |
     v
Extraer tenant_id
     |
     v
Consultar relación usuario-clínica
     |
     v
Resolver esquema autorizado
     |
     v
Ejecutar consulta en PostgreSQL
```

El `tenant_id` debe obtenerse del JWT firmado y validarse contra la relación del usuario con la clínica. Nunca debe confiarse en un `tenant_id` enviado desde Flutter.

Importante: no implementen la selección de esquema guardando el tenant actual en una variable global o un campo de un servicio Singleton. En peticiones concurrentes podrían cruzarse los datos de dos clínicas. La selección debe ser segura por conexión y transacción.

También deben verificar la compatibilidad de las APIs de multi-tenancy con la versión de Hibernate que utilicen en Spring Boot 3. No conviene dejar esta integración para el final.

Resultado esperado: crear dos clínicas de prueba y demostrar que un usuario de una no puede consultar los registros de la otra.

### Fase 3. Autenticación y autorización

Responsables: persona 1 y persona 5.

Configurar Keycloak antes de implementar la seguridad completa de cada módulo.

Propuesta inicial:

```
Realm: vetcat

Roles:
├── administrador_plataforma
├── administrador_clinica
├── veterinario
├── recepcionista
└── dueno
```

Deben distinguir entre roles globales y roles de clínica. Por ejemplo, el administrador de plataforma no tiene por qué tener acceso clínico a todos los esquemas.

Paso 1. Crear los usuarios de prueba

Crear al menos:

- Un administrador de plataforma.
- Dos administradores de clínicas diferentes.
- Un veterinario.
- Un recepcionista.
- Un dueño de mascota.

Paso 2. Configurar el JWT

Ejemplo conceptual:

```
{
  "sub": "user-123",
  "tenant_id": "tenant-001",
  "realm_access": {
    "roles": [
      "veterinario"
    ]
  }
}
```

Paso 3. Configurar Spring Security

El backend debe:

- Validar la firma y vigencia del JWT.
- Validar emisor y audiencia cuando corresponda.
- Convertir los roles de Keycloak a autoridades de Spring.
- Proteger endpoints con `@PreAuthorize`.
- Resolver el tenant autenticado.

Paso 4. Probar los permisos

| Acción                | Veterinario | Recepcionista       | Dueño              |
| --------------------- | ----------- | ------------------- | ------------------ |
| Registrar citas       | Sí          | Sí                  | No                 |
| Ver agenda            | Sí          | Sí                  | Solo sus citas     |
| Registrar mascotas    | Sí          | Sí                  | No                 |
| Consultar expedientes | Sí          | No                  | Resumen autorizado |
| Registrar vacunas     | Sí          | No                  | No                 |
| Consultar vacunas     | Sí          | Sí, según necesidad | Solo sus mascotas  |

Los permisos de la tabla son una propuesta operativa que deben aprobar antes de implementarlos. En particular, el acceso a información clínica del dueño debe limitarse a los campos expresamente permitidos.

## 4. Desarrollo paralelo de los módulos funcionales

Una vez que estén listos los contratos de API, la base de datos y la autenticación inicial, pueden avanzar en paralelo.

### Orden recomendado de desarrollo

1. Módulo 1 — Propietarios y mascotas

   Persona 2 + persona 4
   - Registro de propietarios.
   - Registro de mascotas.
   - Edición y consulta.
   - Asociación mascota-propietario.

     Primero
2. Módulo 2 — Agenda de citas

   Persona 3 + persona 4
   - Calendario.
   - Disponibilidad.
   - Registro y reprogramación.
   - Validación de empalmes.
3. Módulo 3 — Expedientes clínicos

   Persona 2 + persona 4
   - Consultas.
   - Diagnósticos.
   - Tratamientos.
   - Recetas y notas.
4. Módulo 4 — Vacunación

   Persona 3 + persona 4
   - Catálogo de vacunas.
   - Registro de dosis.
   - Próximas aplicaciones.
   - Vencimientos.
5. Módulo 5 — Portal del dueño

   Persona 4 + persona 1 + personas 2 y 3
   - Acceso del propietario.
   - Sus mascotas.
   - Citas.
   - Vacunas.
   - Resumen clínico autorizado.

Este orden sigue las funcionalidades del MVP original, que contempla gestión de clínicas, agenda, propietarios, mascotas, expedientes, vacunas y portal del dueño.&#x20;

Propuesta_VetCat.md



### ¿Cómo debe trabajar cada módulo?

Por ejemplo, para registrar una mascota:

Persona 2 — Backend

```
MascotaController
       |
MascotaService
       |
MascotaRepository
       |
MascotaEntity
```

Endpoints:

```
POST   /api/v1/mascotas
GET    /api/v1/mascotas
GET    /api/v1/mascotas/{id}
PUT    /api/v1/mascotas/{id}
DELETE /api/v1/mascotas/{id}
```

Persona 4 — Frontend

Construye:

```
Mascotas/
├── mascotas_page.dart
├── mascota_form.dart
├── mascota_detail.dart
└── mascota_service.dart
```

Flujo de trabajo:

1. La persona 2 entrega el contrato del endpoint.
2. La persona 4 construye la interfaz usando ese contrato.
3. La persona 2 implementa el endpoint.
4. La persona 4 conecta la interfaz.
5. Ambos prueban el registro.
6. La persona 5 valida los casos de prueba.
7. La persona 1 revisa la integración.

Así, ambos trabajan simultáneamente sin depender de que uno termine por completo.

## 5. Cómo implementar correctamente los módulos

Aquí hay algunas reglas técnicas que conviene establecer desde el principio para evitar errores difíciles de corregir.

### 5.1 Propietarios y mascotas

Relación:

Propietario

id, nombre, teléfono, correo

1 a muchos

Mascotas

id, propietario_id, nombre, especie, raza, peso, fecha_nacimiento

Un propietario puede registrar varias mascotas.

Reglas de negocio:

- Una mascota debe pertenecer a un propietario.
- No se puede consultar una mascota de otro tenant.
- Al eliminar un propietario, no deben eliminarse automáticamente expedientes clínicos sin una política de conservación definida.
- El historial debe conservar la relación entre mascota y propietario.

### 5.2 Agenda de citas

Este módulo requiere especial atención porque uno de los objetivos principales es evitar citas empalmadas.

Una cita tendrá:

```
Cita
├── id
├── mascota_id
├── veterinario_id
├── consultorio_id
├── inicio
├── fin
├── motivo
└── estado
```

La validación debe contemplar:

1. El veterinario tiene disponibilidad.
2. El consultorio está disponible.
3. La cita está dentro del horario de atención.
4. No existe otra cita activa que se cruce con el horario solicitado.
5. La mascota y el veterinario pertenecen a la misma clínica.

No basta con validar el empalme en Flutter o mediante un `SELECT` previo. Dos recepcionistas podrían reservar simultáneamente.

La propuesta contempla restricciones de exclusión de PostgreSQL.&#x20;

Propuesta_VetCat.md



Para implementarlas necesitarán estudiar `tstzrange` y la extensión `btree_gist`, además de excluir las citas canceladas.

Ejemplo conceptual:

```
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE cita
ADD CONSTRAINT cita_sin_empalmes
EXCLUDE USING gist (
    veterinario_id WITH =,
    tstzrange(inicio, fin, '[)') WITH &&
)
WHERE (estado <> 'CANCELADA');
```

Si también quieren evitar que dos veterinarios utilicen el mismo consultorio al mismo tiempo, necesitarán otra restricción equivalente sobre `consultorio_id`.

### 5.3 Expediente clínico

El expediente debe funcionar como un historial cronológico de consultas.

Ejemplo:

```
Mascota: Michi
  |
  ├── Consulta 01
  │     ├── Fecha
  │     ├── Diagnóstico
  │     ├── Tratamiento
  │     └── Receta
  |
  ├── Consulta 02
  │     ├── Fecha
  │     ├── Diagnóstico
  │     ├── Tratamiento
  │     └── Receta
  |
  └── Consulta 03
        ├── Fecha
        ├── Diagnóstico
        └── Tratamiento
```

Recomendaciones:

- No sobrescribir consultas anteriores.
- Registrar qué veterinario realizó cada consulta.
- Guardar fechas y horas.
- Validar formatos de archivos adjuntos.
- Evitar que recepcionistas modifiquen información clínica.
- Registrar modificaciones importantes mediante auditoría.

### 5.4 Vacunas

Una vacuna aplicada debe guardar:

- Mascota.
- Tipo de vacuna.
- Fecha de aplicación.
- Próxima dosis.
- Lote.
- Veterinario responsable.

La próxima fecha se calcula usando el intervalo correspondiente, pero debe poder ajustarse por criterio veterinario.

Las alertas del MVP se muestran dentro del sistema; no necesitan desarrollar todavía notificaciones de WhatsApp o SMS.&#x20;

Propuesta_VetCat.md



### 5.5 Portal del dueño

Este módulo se debe implementar después de tener funcionando los anteriores.

El propietario inicia sesión y solamente puede consultar:

- Sus mascotas.
- Próximas citas.
- Cartillas de vacunación.
- Resumen de historial clínico autorizado.

Error que deben evitar: no basta con ocultar botones o pantallas en Flutter. Cada consulta del backend debe comprobar que la mascota realmente pertenece al propietario autenticado.

## 6. Plan de trabajo por sprints

Para organizar al equipo, recomiendo utilizar Scrum con sprints de dos semanas. El siguiente calendario es una estimación de planificación, no un plazo garantizado.

Propuesta de 12 semanas

| Sprint | Trabajo principal             | Resultado                 |
| ------ | ----------------------------- | ------------------------- |
| 0      | Preparación y arquitectura    | Repositorio y contratos   |
| 1      | Docker, PostgreSQL y Keycloak | Infraestructura funcional |
| 2      | Backend base y multi-tenancy  | API segura y dos tenants  |
| 3      | Propietarios y mascotas       | Primer módulo funcional   |
| 4      | Agenda de citas               | Reservas sin empalmes     |
| 5      | Expedientes y vacunas         | Gestión clínica           |
| 6      | Portal, integración y pruebas | MVP integrado             |

### Distribución por sprint

| Integrante           | Sprint 0–1               | Sprint 2–3              | Sprint 4–5              | Sprint 6                   |
| -------------------- | ------------------------ | ----------------------- | ----------------------- | -------------------------- |
| 1. Líder técnico     | Arquitectura y contratos | Seguridad y revisión    | Integración             | Estabilización             |
| 2. Backend clínico   | Modelo de datos          | Propietarios y mascotas | Expedientes             | Correcciones               |
| 3. Backend operativo | Modelo de citas          | Base de agenda          | Citas y vacunas         | Correcciones               |
| 4. Flutter           | Diseño y estructura      | Login y mascotas        | Agenda y clínica        | Portal                     |
| 5. DevOps/QA         | Docker y Keycloak        | Migraciones y pruebas   | Integración y seguridad | Despliegue y documentación |

No significa que los integrantes trabajen exclusivamente en esas tareas durante cada sprint. Es su responsabilidad principal.

### Qué debe ocurrir en cada sprint

Inicio del sprint

- Revisar pendientes.
- Seleccionar historias de usuario.
- Asignar responsables.
- Definir criterios de aceptación.

Durante el sprint

- Reunión diaria de 10–15 minutos.
- Desarrollo en ramas feature.
- Integración continua.
- Revisión de Pull Requests.
- Pruebas de las funcionalidades.

Cierre del sprint

- Demostración de funcionalidades.
- Validación de criterios de aceptación.
- Corrección de errores.
- Retrospectiva.
- Planificación del siguiente sprint.

Una regla importante: cada sprint debe terminar con algo que se pueda ejecutar y demostrar, no solamente con código escrito.

## 7. Cómo organizar las tareas y evitar conflictos

Recomiendo utilizar GitHub Projects para administrar las actividades del equipo.

Cada tarea debe tener:

- Identificador.
- Nombre.
- Descripción.
- Responsable.
- Prioridad.
- Sprint.
- Criterios de aceptación.
- Estado.

Ejemplo de tablero:

Pendiente

Por iniciar

Configurar DTOs

Diseñar login

En progreso

En desarrollo

API mascotas

Docker PostgreSQL

Terminado

Validado

Repositorio

Estructura inicial

Ejemplo ilustrativo del tablero de trabajo.

### Reglas de integración

1. Una persona no debe modificar directamente el código de otro módulo sin coordinarlo.
2. Los cambios en entidades compartidas se acuerdan entre los responsables.
3. Los DTOs y endpoints deben estar documentados.
4. Las migraciones de base de datos se versionan.
5. No se deben subir contraseñas, tokens ni archivos `.env` al repositorio.
6. No se acepta una funcionalidad como terminada si no tiene pruebas.

### Definition of Done

Una tarea solamente se considera terminada cuando:

- Cumple los requisitos.
- Compila correctamente.
- Tiene validaciones.
- Tiene pruebas.
- Respeta permisos y aislamiento por tenant.
- Está integrada mediante Pull Request.
- Está documentada cuando corresponde.

## 8. Pruebas que deben realizar antes de entregar

En VetCat, las pruebas de seguridad y aislamiento son tan importantes como las pruebas funcionales.

Recomiendo crear un conjunto de pruebas de aceptación desde el principio.

| Prueba                                                    | Resultado esperado                       |
| --------------------------------------------------------- | ---------------------------------------- |
| Registrar una clínica                                     | Se crea su tenant                        |
| Registrar dos clínicas                                    | Se crean dos esquemas                    |
| Iniciar sesión                                            | Keycloak devuelve JWT válido             |
| Acceder sin JWT                                           | HTTP 401                                 |
| Acceder con rol incorrecto                                | HTTP 403                                 |
| Consultar datos de otro tenant                            | Acceso denegado                          |
| Registrar una mascota                                     | Se guarda correctamente                  |
| Registrar dos citas simultáneas para el mismo veterinario | Solo una puede reservar el mismo horario |
| Registrar una vacuna                                      | Se calcula la próxima dosis              |
| Dueño consulta mascota ajena                              | Acceso denegado                          |
| Reiniciar Docker                                          | Los datos persisten                      |

Prioridad de pruebas:

1. Aislamiento de clínicas.
2. Autorización por roles.
3. Concurrencia de citas.
4. Integridad de expedientes.
5. Funcionalidad de vacunas.
6. Interfaz y experiencia de usuario.

No esperen hasta el último sprint para probar el multi-tenancy. Un error de aislamiento puede comprometer todos los datos de la plataforma.

## 9. Decisiones técnicas que corregiría o aclararía en la propuesta

La propuesta está bien encaminada, pero hay algunas decisiones que necesitan mayor definición antes de comenzar a programar.

| Aspecto           | Recomendación                                                                                      |
| ----------------- | -------------------------------------------------------------------------------------------------- |
| Multi-tenancy     | Implementarlo y probarlo antes de los módulos funcionales.                                         |
| Keycloak          | Definir cómo se representa la pertenencia de un usuario a una clínica y cómo se emite `tenant_id`. |
| Hibernate         | Verificar las interfaces de multi-tenancy de la versión seleccionada.                              |
| Citas             | Usar restricciones de PostgreSQL, no solamente validaciones en Java.                               |
| Archivos clínicos | Guardar archivos fuera de las tablas principales, con referencias y acceso protegido.              |
| Migraciones       | Utilizar Flyway para versionar y aplicar cambios por tenant.                                       |
| Singleton         | Mantener los servicios sin estado y utilizar inyección por constructor.                            |
| Pruebas           | Incorporar pruebas automatizadas desde los primeros módulos.                                       |

Hay tres puntos que considero especialmente importantes:

1\. No confundir autenticación con autorización.

Keycloak confirma quién es el usuario. Spring Boot debe decidir qué puede hacer ese usuario y a qué clínica pertenece. No deben delegar toda la seguridad en el frontend.

2\. No construir un sistema multi-tenant improvisado.

La resolución del esquema es una de las partes más delicadas de VetCat. Es mejor invertir tiempo en una implementación pequeña y probada que desarrollar cinco módulos sobre una base insegura.

3\. No desarrollar todo el alcance del SaaS desde el principio.

La propuesta ya delimita un MVP. No agregaría facturación, inventarios, WhatsApp, aplicación móvil ni escritorio en esta entrega.&#x20;

Propuesta_VetCat.md



## 10. ¿Por dónde comenzar exactamente?

Si mañana comenzaran a desarrollar VetCat, esta sería mi lista de actividades iniciales.

Checklist de arranque

Primera semana

0 de 22 actividades completadas

0%

Día 1

Organización

Crear repositorio de GitHub

Asignar los cinco responsables

Crear ramas main y develop

Crear tablero de GitHub Projects

Definir convenciones de código

Día 2

Diseño técnico

Definir arquitectura del backend

Diseñar modelo de datos inicial

Definir roles y permisos

Documentar contratos de API

Día 3

Infraestructura

Levantar PostgreSQL con Docker

Configurar pgAdmin

Levantar Keycloak

Crear realm y roles

Día 4

Estructura de aplicaciones

Crear proyecto Spring Boot

Crear proyecto Flutter

Configurar conexión a PostgreSQL

Conectar Spring Security con Keycloak

Día 5

Primera integración

Crear endpoint de prueba protegido

Iniciar sesión desde Flutter

Validar JWT en backend

Probar persistencia en PostgreSQL

Documentar el entorno de desarrollo

Reiniciar checklist

### 11. Recomendación final sobre la dirección del equipo

Yo organizaría el proyecto bajo estas reglas:

- Persona 1: responsable de que la arquitectura funcione como un solo sistema.
- Personas 2 y 3: responsables de las reglas de negocio y persistencia.
- Persona 4: responsable de que los usuarios puedan utilizar las funcionalidades.
- Persona 5: responsable de que todo se pueda ejecutar, probar y entregar correctamente.

Y establecería tres hitos obligatorios:

Hito 1

Infraestructura funcional

Todos pueden ejecutar VetCat localmente, iniciar sesión y conectarse a la base de datos.

Hito 2

Primera clínica operativa

Se pueden registrar propietarios, mascotas y citas, con autenticación y aislamiento de datos.

Hito 3

MVP completo

Se dispone de agenda, expedientes, vacunas y portal del dueño, con pruebas de seguridad y despliegue reproducible.

Una última recomendación: no asignaría simplemente cinco módulos a cinco personas. Asignaría cinco áreas de responsabilidad, pero establecería dependencias y entregables compartidos. La calidad del proyecto dependerá mucho más de cómo integren sus trabajos que de cuánto código escriba cada integrante.

## Siguiente paso recomendado

Lo que más conviene ahora es convertir esta propuesta en un plan de ejecución real para GitHub Projects, con historias de usuario, tareas técnicas, subtareas, responsables, prioridades, dependencias y criterios de aceptación para los cinco integrantes.

Así cada persona sabrá exactamente qué debe desarrollar, qué necesita recibir de otro integrante y cuándo debe entregar su trabajo.

También podemos establecer una revisión semanal de avances y bloqueos: