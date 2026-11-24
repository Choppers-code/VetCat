-- `unaccent` permite buscar propietarios y mascotas sin importar los acentos (PRO-009).
--
-- Se instala una sola vez en `ext`, igual que `btree_gist`: todas las conexiones de
-- clínica incluyen `ext` en su `search_path`, así que los esquemas de clínica la
-- heredan. La imagen oficial de PostgreSQL la trae y es una extensión de confianza.

CREATE EXTENSION IF NOT EXISTS unaccent SCHEMA ext;
