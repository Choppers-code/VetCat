# Despliegue y operación

## 1. Modelo de despliegue

- **Primer entorno: la máquina del desarrollador.** Todo el stack con `docker compose up -d` y una clínica de prueba. Es el entorno donde se construye y se demuestra cada fase.
- **Destino previsto: nube.** El mismo stack, con otro archivo de variables de entorno. Subir a nube no debe requerir cambios de código.
- **Fuera por ahora:** servidor instalado en la clínica, alta disponibilidad, multi-región y balanceo.

La regla que sostiene esto: nada del código asume `localhost`, rutas del host ni una sola clínica. Si algo se escribe en disco, se configura con variable de entorno; si algo se registra, va a `stdout`.

## 2. Entornos

| Entorno | Dónde | Propósito | Datos | Acceso |
|---|---|---|---|---|
| Local | Máquina del desarrollador | Construcción y demostración de cada fase | Sintéticos | HTTP en `localhost` |
| Staging | Nube, cuando exista | Validar antes de producción | Anonimizados | HTTPS, acceso restringido |
| Producción | Nube | Clínicas reales | Reales | HTTPS público |

Un cambio no se considera terminado si solo se probó en local. La Definition of Done incluye "probado en el entorno que corresponda".

## 3. Diferencias entre local y nube

| Aspecto | Local | Nube |
|---|---|---|
| Protocolo | HTTP | HTTPS con dominio real y renovación automática |
| `redirect_uri` de Keycloak | `http://localhost:PUERTO/*` | `https://dominio/*` |
| Adjuntos | MinIO en el compose | Servicio compatible con S3 |
| Migraciones | Contenedor one-shot antes de arrancar | El mismo contenedor, como job previo al despliegue |
| Logs | Consola | Recolectados y con retención |
| Secretos | `.env` local, nunca en el repo | Gestor de secretos del proveedor |
| App Android | APK de debug, URL `10.0.2.2` en emulador o IP de la LAN | APK firmado, URL pública |
| Respaldos | `pg_dump` manual a carpeta local | Base backup + WAL + dump por esquema |

## 4. Reglas de portabilidad

1. **Configuración por variables de entorno.** Ningún valor de conexión, dominio o credencial vive en el código.
2. **Sin estado en el contenedor.** Lo único persistente son los volúmenes de PostgreSQL y del almacenamiento de objetos.
3. **Logs a `stdout`.** Nada escribe archivos de log propios.
4. **Sin `localhost` en la configuración por defecto de producción.** Las URLs internas se resuelven por nombre de servicio del compose.
5. **Almacenamiento de adjuntos detrás de una interfaz.** MinIO local y S3 en nube comparten la misma abstracción.
6. **Migraciones como job, no al arrancar la API.** Con varias réplicas, `spring.flyway` al inicio duplica trabajo y compite.
7. **Healthchecks reales.** `liveness` y `readiness` separados, y `depends_on: condition: service_healthy` en compose.

## 5. Migraciones

- Un contenedor one-shot aplica Flyway antes de levantar la API. La API nunca migra.
- Regla **expand/contract**: toda migración debe ser compatible con la versión anterior de la app, porque durante el despliegue conviven ambas.
- El esquema de catálogo se migra una vez; los esquemas de clínica se migran en bucle, con una tabla de estado por tenant para saber cuáles van y reintentar los que fallen.
- Política de **roll-forward**: si una migración falla a mitad, se corrige y se vuelve a aplicar. El rollback de la imagen anterior solo es seguro si la migración fue expand-only.
- Prohibido cambiar el tipo del enum de estado con `ALTER TYPE` por esquema; se usa `varchar` con `CHECK`.

## 6. App Android

- **Primer alcance: solo Android.** iOS queda para cuando exista Mac, Xcode y cuenta de Apple Developer.
- Pruebas locales: APK de debug contra `10.0.2.2` (emulador) o la IP de la máquina en la LAN (dispositivo físico).
- El tráfico sin cifrar se permite **solo** en la configuración de debug; en release, HTTPS obligatorio.
- Firma: keystore como secreto, nunca en el repositorio.
- Distribución inicial: APK directo. Play Store en la fase de publicación.
- La app consulta un endpoint de versión mínima y bloquea el uso si quedó obsoleta.

## 7. Respaldos y restauración

Restaurar una clínica requiere **cuatro** piezas, no una:

1. El esquema `clinica_xxx`.
2. La fila correspondiente en `catalogo.clinica` y `catalogo.usuario_clinica`.
3. Los usuarios del realm en Keycloak.
4. Los objetos del almacenamiento bajo el prefijo de esa clínica.

| Entorno | Mecanismo |
|---|---|
| Local | `pg_dump` del esquema y copia del volumen de objetos, a una carpeta fuera del repo |
| Nube | Base backup + archivado de WAL para desastre total; dump lógico por esquema para exportar o restaurar una clínica |

- Los respaldos se cifran y no se guardan en el mismo host que la base.
- La restauración se ensaya al menos una vez por trimestre, con tiempo medido.
- RPO y RTO se fijan cuando exista producción; mientras tanto, el criterio es "restaurar una clínica en menos de una hora".

## 8. Monitoreo y alertas

- Métricas con Micrometer expuestas para Prometheus; paneles en Grafana.
- Logs estructurados con redacción de datos personales.
- Alertas mínimas: tasa de 5xx, latencia p95, conexiones a base de datos, disco, antigüedad del último respaldo, expiración del certificado y migración fallida por tenant.
- Un chequeo de disponibilidad externo para la API y para Keycloak.

## 9. Incidentes y soporte

- Runbook mínimo: base de datos caída, Keycloak caído, restauración de una clínica, migración fallida, certificado vencido.
- Un canal único de incidentes y un responsable de guardia, aunque sea informal.
- El acceso de soporte a datos clínicos, si se permite, es temporal, autorizado por la clínica, con motivo y registrado en auditoría.

## 10. Checklist para subir a nube

- [ ] Dominio y certificado con renovación automática.
- [ ] Servidor SMTP configurado en Keycloak (recuperación de contraseña e invitaciones).
- [ ] Secretos fuera del repositorio y con rotación.
- [ ] Registro de imágenes con tags inmutables por SHA.
- [ ] Migraciones como job y política de roll-forward documentada.
- [ ] Respaldos cifrados y una restauración ensayada.
- [ ] Monitoreo y alertas activos.
- [ ] Límites de tasa en login, subida de adjuntos y búsquedas.
- [ ] Límite de tamaño y tipo de archivo por clínica.
- [ ] Endpoint de versión mínima de la app funcionando.
- [ ] Ventana de mantenimiento declarada, o dos stacks con proxy para cambiar sin corte.

## 11. Riesgos

- **Asumir una sola clínica.** El primer entorno tiene una, pero el código y las migraciones deben soportar N desde la fase 2.
- **Asumir la máquina del desarrollador.** Rutas absolutas, `localhost` y datos semilla se convierten en deuda al subir a nube.
- **Disco único.** En local no hay redundancia; los respaldos deben salir del equipo.
- **Sin staging, el primer despliegue se estrena con clientes dentro.**

## 12. Repositorio público

El repositorio es público desde la fase 0. No es una preferencia estética: en el plan Free, la protección de ramas, la revisión obligatoria por CODEOWNERS y los checks de CI obligatorios solo existen en repositorios públicos. Las tres reglas que sostienen el control de responsabilidades dependen de esa decisión.

Lo que implica:

- **Todo lo que entra es visible y permanente.** Borrar un archivo no lo saca del historial ni de los clones que ya existen.
- **Secretos:** `.gitignore` aplicado antes del primer `add`, y *secret scanning* con *push protection* activos, que son gratuitos en repositorios públicos.
- **Export del realm:** solo clientes públicos, sin campo `secret` y sin usuarios. Un export completo incluye credenciales de las personas.
- **Datos:** `database/seeds` nunca lleva datos de una clínica real, ni capturas de pantalla con nombres de mascotas o propietarios.
- **Licencia:** el repositorio no lleva `LICENSE`. Sin licencia, el código queda con todos los derechos reservados; añadir una permisiva regala el producto.
- **PRs desde forks** no reciben secretos ni tokens del repositorio, lo cual es el comportamiento correcto y no hay que cambiarlo.

Si en el futuro el producto pasa a privado, hay que contratar GitHub Team el mismo día: cambiar la visibilidad desactiva las tres protecciones de golpe.
