-- Clínicas de la plataforma (docs/Decisiones_tecnicas.md §1 y §3).
--
-- Una fila por clínica. El nombre del esquema lo genera la base a partir del
-- identificador interno: nunca lo envía el cliente. El plan y su vigencia
-- llegan con el módulo de planes, en su propia migración.

CREATE TABLE catalogo.clinica (
    id                bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    esquema           text        GENERATED ALWAYS AS ('clinica_' || lpad(id::text, 3, '0')) STORED NOT NULL,
    nombre            text        NOT NULL,
    correo_contacto   text,
    telefono_contacto text,
    zona_horaria      text        NOT NULL,
    estado            varchar(20) NOT NULL DEFAULT 'ACTIVA',
    creada_en         timestamptz NOT NULL DEFAULT now(),
    actualizada_en    timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT clinica_esquema_unico UNIQUE (esquema),
    CONSTRAINT clinica_nombre_no_vacio CHECK (length(btrim(nombre)) > 0),
    CONSTRAINT clinica_estado_valido CHECK (estado IN ('ACTIVA', 'SUSPENDIDA', 'BAJA'))
);

COMMENT ON TABLE catalogo.clinica IS
    'Una fila por clínica. Dos clínicas con el mismo nombre se aceptan: el identificador único es el interno.';
COMMENT ON COLUMN catalogo.clinica.esquema IS
    'Esquema de la clínica, derivado del id. Lo genera la base; el cliente nunca lo envía.';
COMMENT ON COLUMN catalogo.clinica.zona_horaria IS
    'Zona IANA (por ejemplo America/Mexico_City). Horarios y disponibilidad se calculan en ella.';
COMMENT ON COLUMN catalogo.clinica.estado IS
    'ACTIVA, SUSPENDIDA o BAJA. `varchar` con CHECK, nunca un enum.';

-- Cada fila de migracion_tenant (V2) corresponde a una clínica existente.
ALTER TABLE catalogo.migracion_tenant
    ADD CONSTRAINT migracion_tenant_clinica_fk
    FOREIGN KEY (esquema) REFERENCES catalogo.clinica (esquema);
