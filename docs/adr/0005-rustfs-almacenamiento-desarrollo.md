# ADR-0005 · RustFS como almacenamiento S3 en desarrollo

| | |
|---|---|
| **Estado** | Aceptada |
| **Fecha** | 2026-11-12 |
| **Área** | 5 · Infraestructura, con el área 1 |
| **Issue** | #49 |
| **Decide** | En el entorno local los adjuntos van a RustFS (`rustfs/rustfs:1.0.5`) en lugar de MinIO; el backend solo conoce la API S3 |

## Contexto

Los documentos eligieron MinIO como almacenamiento de adjuntos en desarrollo (`Tecnologias_y_patrones.md` §1, `Despliegue_y_operacion.md` §3 y §4), y el #19 lo incluye en el compose de la fase 1.

Esa elección ya no se puede cumplir:

- Desde octubre de 2025 la edición comunitaria de MinIO se distribuye solo como código fuente. No hay binarios ni imágenes oficiales nuevas.
- `minio/minio` ya no existe en Docker Hub: `docker pull minio/minio` falla.
- La copia de quay.io quedó congelada en la última imagen de 2025 y no recibe parches de seguridad.
- Las alternativas que quedan son compilar la imagen nosotros o depender de copias publicadas por terceros, que nadie del equipo puede auditar.

El compose del #19 no puede cerrar el hito H1 con una imagen que no se puede descargar.

Lo que no cambia es la regla de fondo: el almacenamiento vive detrás de una interfaz (`Despliegue_y_operacion.md` §4, regla 5) y en nube será un servicio compatible con S3. Lo que se elige aquí es solo qué programa habla S3 en la máquina del desarrollador.

## Opciones consideradas

| Opción | Ventaja | Costo |
|---|---|---|
| **RustFS** (`rustfs/rustfs`, versión fijada) | Imagen oficial mantenida, para amd64 y arm64; compatible con S3; licencia Apache 2.0; usa los mismos puertos que MinIO (9000 la API, 9001 la consola) | Proyecto joven: la línea 1.0 es estable desde septiembre de 2026 y el equipo no lo conoce |
| MinIO congelado (última imagen de 2025) | Los documentos no cambian | Sin actualizaciones ni parches, y depende de copias de terceros |
| Garage o SeaweedFS | Maduros y compatibles con S3 | Más configuración inicial de la que necesita un entorno de desarrollo de un solo nodo |

## Decisión

1. **En desarrollo, RustFS** con la imagen oficial **`rustfs/rustfs:1.0.5`**, la versión estable publicada al tomar esta decisión. La etiqueta se escribe completa en el compose; nunca `latest`.
2. **El backend solo habla la API S3** a través de la interfaz de almacenamiento (patrón Adapter, `Tecnologias_y_patrones.md` §2). No usa APIs propias de RustFS. Endpoint, bucket y credenciales llegan por variables de entorno.
3. **En nube no cambia nada:** sigue un servicio compatible con S3, y el proveedor sigue siendo una decisión abierta (`Tecnologias_y_patrones.md` §6).
4. **La versión se actualiza como las demás:** dentro de la línea 1.0 cuando haya un parche de seguridad, en un PR propio de `infrastructure/`, y se registra en la tabla de versiones.

## Consecuencias

### Se gana

- Una imagen que se puede descargar y que recibe parches.
- El cambio no toca código: la interfaz S3 es la misma con MinIO, con RustFS y con S3 en nube.
- La licencia Apache 2.0 no impone obligaciones al código de VetCat.

### Se paga

- Menos madurez que MinIO y nadie del equipo tiene horas con RustFS.
- La imagen corre con un usuario sin privilegios (UID/GID `10001`). Si el compose monta una carpeta del host en lugar de un volumen con nombre, esa carpeta tiene que poder escribirla ese usuario; si no, el contenedor no arranca.
- La compatibilidad de las URL firmadas de vida corta (`Decisiones_tecnicas.md` §6) se comprueba contra RustFS cuando exista el módulo de adjuntos, no se da por hecha.

### Queda prohibido a partir de ahora

- Usar `minio/minio` o copias de terceros de MinIO.
- Usar la etiqueta `latest` o una versión preliminar (`preview`, `rc`) de RustFS.
- Llamar desde el backend a APIs propias de RustFS en vez de a la API S3.

## Cómo se hace cumplir

| Mecanismo | Qué garantiza | Estado |
|---|---|---|
| Compose del #19 con `rustfs/rustfs:1.0.5` | Todo el equipo usa la misma versión | Fase 1 |
| Tabla de versiones en `Tecnologias_y_patrones.md` | La versión está escrita en un solo sitio | Activo |
| Revisión de PR | Nadie cambia de imagen dentro de un PR de funcionalidad | Activo |

## Referencias

- `docs/Tecnologias_y_patrones.md` §1, §Versiones y §6.
- `docs/Despliegue_y_operacion.md` §3 y §4.
- Issue #19: compose esqueleto de la fase 1.
- RustFS, release 1.0.5 e imagen oficial `rustfs/rustfs` en Docker Hub.
