# ADR-0009 · sin almacenamiento de objetos

| | |
|---|---|
| **Estado** | Aceptada |
| **Fecha** | 2026-11-20 |
| **Área** | 1 · Arquitectura, con el área 5 |
| **Issue** | #76 |
| **Decide** | Se retira RustFS y el almacenamiento compatible con S3 del alcance y de los documentos de despliegue, porque ya no hay adjuntos |

## Contexto

El [ADR-0005](0005-rustfs-almacenamiento-desarrollo.md) eligió RustFS para guardar los adjuntos del expediente en desarrollo. Los requerimientos cambiaron esa premisa:

- **No hay adjuntos.** El alcance excluye adjuntar fotografías o estudios a la consulta (el análisis es presencial).
- **Los PDF se generan al vuelo** (receta y comprobante de cita) y no se almacenan.
- **El logo de la clínica** es una imagen PNG o JPG de hasta 500 KB, guardada en la base de datos.

Sin adjuntos, el almacenamiento de objetos no sirve a ninguna función, y mantenerlo cuesta un servicio más en el compose, un adaptador, credenciales y una pieza más en los respaldos.

## Opciones consideradas

| Opción | Ventaja | Costo |
|---|---|---|
| **Retirar el almacenamiento de objetos** | Un servicio, un adaptador, un secreto y una pieza de respaldo menos | Si más adelante aparecen archivos grandes, habrá que volver a decidirlo |
| Dejarlo por si acaso | Nada que cambiar hoy | Se mantiene infraestructura sin uso y una superficie de ataque innecesaria |

## Decisión

1. **El almacenamiento de objetos sale del alcance.** No hay bucket, ni URL firmadas, ni adaptador de almacenamiento.
2. El logo vive en la base de datos, con tope de 500 KB y solo PNG o JPG.
3. Este ADR **reemplaza** al ADR-0005, que conserva su texto como historia.
4. **Retirar RustFS del compose, de `.env.example` y de la tabla de versiones es un cambio de infraestructura aparte**, con su propio issue y PR del área 5. Hasta entonces el compose sigue levantando RustFS sin que ningún módulo lo use.
5. Las variables `RUSTFS_*` y `S3_*` y el servicio `rustfs-init` se retiran en ese cambio.

## Consecuencias

### Se gana

- Menos infraestructura y menos secretos.
- Los respaldos de una clínica pasan de cuatro piezas a tres: esquema, catálogo y usuarios de Keycloak.

### Se paga

- Si más adelante hay archivos que no caben en la base, se necesita un ADR nuevo.
- Mientras el compose no se limpie, hay un servicio sin uso.

### Queda prohibido a partir de ahora

- Añadir almacenamiento de objetos, adjuntos o URL firmadas sin un ADR que lo decida.
- Guardar PDF generados: se producen al vuelo.

## Cómo se hace cumplir

| Mecanismo | Qué garantiza | Estado |
|---|---|---|
| Issue de infraestructura para retirar RustFS | Que el compose y `.env.example` queden limpios | Pendiente |
| Revisión de PR | Que nadie añade adjuntos o almacenamiento de objetos sin ADR | Activo |

## Referencias

- [ADR-0005](0005-rustfs-almacenamiento-desarrollo.md), que este ADR reemplaza.
- `docs/Alcance.md`, sección "Fuera de alcance".
- `docs/Decisiones_tecnicas.md` §6 y §11.
