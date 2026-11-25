-- Auditoría de las acciones de plataforma (CLI).
--
-- Vive en el catálogo porque es global: registra lo que el super_admin hace
-- sobre clínicas y planes, no datos clínicos. Nunca se borra desde la API.

CREATE TABLE catalogo.auditoria_plataforma (
    id                bigint      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    actor_keycloak_id uuid        NOT NULL,
    accion            varchar(40) NOT NULL,
    clinica_id        bigint      REFERENCES catalogo.clinica (id),
    detalle           text,
    creada_en         timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT auditoria_accion_valida CHECK (accion IN (
        'ALTA_CLINICA', 'CAMBIO_PLAN', 'CAMBIO_ESTADO_CLINICA',
        'ALTA_PLAN', 'CAMBIO_PLAN_CATALOGO', 'PAGO_REGISTRADO'
    ))
);

COMMENT ON TABLE catalogo.auditoria_plataforma IS
    'Quién hizo qué en la plataforma, cuándo y sobre qué clínica. Solo lectura desde la API.';
COMMENT ON COLUMN catalogo.auditoria_plataforma.detalle IS
    'Texto libre con el detalle mínimo (plan anterior y nuevo, por ejemplo). Nunca datos clínicos.';

CREATE INDEX auditoria_por_clinica
    ON catalogo.auditoria_plataforma (clinica_id, creada_en DESC);
