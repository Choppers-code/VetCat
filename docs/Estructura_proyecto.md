# Estructura del proyecto

Qué hay en cada carpeta y de quién es. La regla de fondo: **un módulo es un paquete, un paquete tiene un dueño**. El detalle de responsabilidades está en `Equipo_responsabilidades.md`; aquí solo se explica dónde va cada cosa.

## Árbol

```
VetCat/
├─ .github/
│  ├─ CODEOWNERS                  quién revisa qué, por ruta
│  ├─ ISSUE_TEMPLATE/             funcionalidad, ADR, error, mantenimiento
│  ├─ pull_request_template.md    checklist de Definition of Done
│  └─ workflows/                  nombre de rama; compilación en la fase 1
├─ docs/
│  ├─ Alcance.md  Roadmap.md  Decisiones_tecnicas.md
│  ├─ Tecnologias_y_patrones.md  Equipo_responsabilidades.md
│  ├─ Despliegue_y_operacion.md  Estructura_proyecto.md
│  ├─ adr/                        una decisión por archivo, numeradas
│  ├─ api/v1/openapi.yaml         contrato publicado
│  └─ historico/                  documentos v1, obsoletos
├─ scripts/
│  ├─ verificar-entorno.ps1       git, gh, docker, java y flutter
│  ├─ nueva-rama.ps1              crea feature/<issue>-<slug> desde develop
│  └─ terminar-rama.ps1           sube, abre el PR y fusiona con squash
├─ backend/
│  ├─ src/main/java/com/vetcat/
│  │  ├─ security/                Spring Security, filtros, `@PreAuthorize`   (área 1)
│  │  ├─ tenancy/                 resolver, provider, contexto de tenant     (área 1)
│  │  ├─ catalogo/                persistencia del esquema global            (área 1)
│  │  ├─ plataforma/              administración de clínicas                 (área 1)
│  │  ├─ shared/                  sobre de respuesta, errores, auditoría
│  │  ├─ propietarios/            (área 2)
│  │  ├─ mascotas/                (área 2)
│  │  ├─ expediente/              (área 2)
│  │  ├─ portal/                  backend del portal del dueño               (área 2)
│  │  ├─ citas/                   (área 3)
│  │  ├─ vacunas/                 (área 3)
│  │  └─ facturacion/             (área 3)
│  ├─ src/main/resources/         `application.yml`, sin SQL
│  └─ src/test/java/com/vetcat/   pruebas de módulo y transversales
├─ frontend/
│  ├─ apps/vetcat_app/            app Android (iOS después)                  (área 4)
│  ├─ apps/vetcat_admin/          consola web de administración              (área 4)
│  └─ packages/
│     ├─ core/                    modelos, cliente HTTP, sobre de respuesta
│     └─ ui/                      design system
├─ database/
│  ├─ migrations/catalogo/        historial del esquema global
│  ├─ migrations/tenant/          historial replicado en cada clínica
│  └─ seeds/                      datos de prueba, nunca reales
└─ infrastructure/
   ├─ docker/                     compose y Dockerfile
   └─ keycloak/                   export del realm, sin secretos ni usuarios
```

## Reglas

1. **Cada módulo repite las mismas capas**: `controller → service → repository → entity/dto`. No se salta ninguna.
2. **Un módulo no usa el repositorio de otro.** Se comunica por su facade. Una prueba de ArchUnit lo vigila desde la fase 2.
3. **Lo transversal vive en `security/`, `tenancy/` o `shared/`**, y solo lo tocan las áreas 1 y 5.
4. **Las migraciones no van en el jar.** Viven en `database/` porque las aplica un contenedor de Flyway antes de arrancar la API.
5. **`docs/` es la fuente de verdad.** Si el código contradice un documento, se corrige uno de los dos en el mismo PR.
6. **Nada de código en la raíz.** La raíz es del repositorio, no del producto.

## Dónde va cada cosa

| Quiero agregar… | Va en |
|---|---|
| Una entidad de negocio | `backend/.../com/vetcat/<módulo>/entity` |
| Un endpoint | `backend/.../com/vetcat/<módulo>/controller` y su contrato en `docs/api/v1` |
| Una pantalla | `frontend/apps/vetcat_app/lib/features/<módulo>` |
| Un widget reutilizable | `frontend/packages/ui` |
| Una llamada a la API | `frontend/packages/core` |
| Un cambio de esquema | `database/migrations/<catalogo|tenant>/V<n>__<descripcion>.sql` |
| Un servicio del compose | `infrastructure/docker/compose.yaml` |
| Un client o rol de Keycloak | `infrastructure/keycloak/realm-vetcat.json` |
| Una decisión de arquitectura | `docs/adr/NNNN-titulo.md` |
| Un script de ayuda del repositorio | `scripts/` |

## Carpetas con `.gitkeep`

Las carpetas vacías llevan un `.gitkeep` para que git las conserve antes de que exista código. Se borra el día que la carpeta tenga su primer archivo real.
