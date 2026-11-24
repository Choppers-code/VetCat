-- Pertenencia usuario-clínica (ADR-0003 y ADR-0006; Decisiones_tecnicas.md §3).
--
-- Una fila por (keycloak_id, clinica_id, rol): un usuario puede tener varios
-- roles en una clínica y pertenecer a varias. Es la única fuente de verdad de
-- la pertenencia y de los roles efectivos del token de tenant.
--
-- `super_admin` no tiene fila aquí: es el único rol que se toma del realm.

CREATE TABLE catalogo.usuario_clinica (
    id          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    keycloak_id uuid        NOT NULL,
    clinica_id  bigint      NOT NULL REFERENCES catalogo.clinica (id),
    rol         varchar(30) NOT NULL,
    estado      varchar(20) NOT NULL DEFAULT 'ACTIVO',
    creado_en   timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT usuario_clinica_unico UNIQUE (keycloak_id, clinica_id, rol),
    CONSTRAINT usuario_clinica_rol_valido
        CHECK (rol IN ('clinica_admin', 'veterinario', 'recepcionista', 'cliente')),
    CONSTRAINT usuario_clinica_estado_valido CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

CREATE INDEX usuario_clinica_clinica_idx ON catalogo.usuario_clinica (clinica_id);

COMMENT ON TABLE catalogo.usuario_clinica IS
    'Pertenencia usuario-clínica, una fila por rol. Los roles efectivos del token salen de aquí, nunca del realm.';
COMMENT ON COLUMN catalogo.usuario_clinica.rol IS
    'clinica_admin, veterinario, recepcionista o cliente. `super_admin` no se guarda aquí.';
COMMENT ON COLUMN catalogo.usuario_clinica.estado IS
    'ACTIVO o INACTIVO. `varchar` con CHECK, nunca un enum.';
