-- Roles y bases del stack de desarrollo.
--
-- Se ejecuta como superusuario contra la base `postgres`, una vez por arranque,
-- desde el servicio `postgres-init`. Es idempotente: el contenedor puede
-- ejecutarse con un volumen que ya tiene datos.
--
-- Tres roles, con permisos separados a propósito:
--   * el superusuario (`POSTGRES_SUPERUSER`) solo administra;
--   * `vetcat` es dueño de la base de la aplicación y el único que migra;
--   * `keycloak` es dueño de la base del realm.
-- Compartir un solo rol convertiría un error de migración en un incidente de
-- autenticación (docs/Despliegue_y_operacion.md §7).

\set ON_ERROR_STOP on

-- Rol y base de la aplicación.
SELECT format('CREATE ROLE %I LOGIN', :'vetcat_user')
WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = :'vetcat_user')
\gexec

ALTER ROLE :"vetcat_user" WITH LOGIN PASSWORD :'vetcat_password';

SELECT format('CREATE DATABASE %I OWNER %I', :'vetcat_db', :'vetcat_user')
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = :'vetcat_db')
\gexec

-- Si la base ya existía con otro dueño, se corrige: el rol `vetcat` tiene que
-- poder crear esquemas en ella aunque el volumen venga de un compose anterior.
ALTER DATABASE :"vetcat_db" OWNER TO :"vetcat_user";

-- Rol y base de Keycloak: los usuarios del realm también forman parte del
-- respaldo de una clínica, así que no viven en el contenedor.
SELECT format('CREATE ROLE %I LOGIN', :'keycloak_user')
WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = :'keycloak_user')
\gexec

ALTER ROLE :"keycloak_user" WITH LOGIN PASSWORD :'keycloak_password';

SELECT format('CREATE DATABASE %I OWNER %I', :'keycloak_db', :'keycloak_user')
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = :'keycloak_db')
\gexec

ALTER DATABASE :"keycloak_db" OWNER TO :"keycloak_user";
