-- Esquema propio para las extensiones compartidas y `btree_gist`.
--
-- `btree_gist` es la que permite el `EXCLUDE USING gist` de la agenda sobre
-- `tstzrange(inicio, fin, '[)')`. Sin ella no hay anti-empalme real, solo un
-- `SELECT` previo que pierde la carrera.
--
-- Todas las conexiones de clínica incluyen `ext` en su `search_path`
-- (docs/Decisiones_tecnicas.md §5), así que se instala una sola vez, aquí, y
-- los esquemas de clínica la heredan por la ruta de búsqueda.

CREATE SCHEMA IF NOT EXISTS ext;

CREATE EXTENSION IF NOT EXISTS btree_gist SCHEMA ext;

COMMENT ON SCHEMA ext IS
    'Extensiones compartidas por catálogo y esquemas de clínica. Se instala una vez desde la migración de catálogo.';
