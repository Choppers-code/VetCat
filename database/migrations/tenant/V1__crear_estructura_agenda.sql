CREATE TABLE consultorio (
    id uuid PRIMARY KEY,
    nombre varchar(120) NOT NULL,
    descripcion varchar(500),
    activo boolean NOT NULL DEFAULT true,
    CONSTRAINT consultorio_nombre_no_vacio CHECK (btrim(nombre) <> '')
);

CREATE TABLE horario_consultorio (
    id uuid PRIMARY KEY,
    consultorio_id uuid NOT NULL REFERENCES consultorio(id) ON DELETE CASCADE,
    dia_semana smallint,
    fecha_excepcion date,
    hora_inicio time without time zone,
    hora_fin time without time zone,
    disponible boolean NOT NULL DEFAULT true,
    CONSTRAINT horario_consultorio_dia_valido
        CHECK (dia_semana IS NULL OR dia_semana BETWEEN 1 AND 7),
    CONSTRAINT horario_consultorio_tipo_fecha
        CHECK (
            (fecha_excepcion IS NULL AND dia_semana IS NOT NULL)
            OR (fecha_excepcion IS NOT NULL AND dia_semana IS NULL)
        ),
    CONSTRAINT horario_consultorio_intervalo_valido
        CHECK (
            (disponible AND hora_inicio IS NOT NULL AND hora_fin IS NOT NULL AND hora_fin > hora_inicio)
            OR (NOT disponible AND hora_inicio IS NULL AND hora_fin IS NULL)
        )
);

CREATE INDEX horario_consultorio_consultorio_dia_idx
    ON horario_consultorio (consultorio_id, dia_semana)
    WHERE fecha_excepcion IS NULL;

CREATE INDEX horario_consultorio_consultorio_fecha_idx
    ON horario_consultorio (consultorio_id, fecha_excepcion)
    WHERE fecha_excepcion IS NOT NULL;

CREATE TABLE bloqueo_agenda (
    id uuid PRIMARY KEY,
    consultorio_id uuid REFERENCES consultorio(id),
    veterinario_id uuid,
    inicio timestamptz NOT NULL,
    fin timestamptz NOT NULL,
    motivo varchar(300) NOT NULL,
    creado_por varchar(255) NOT NULL,
    CONSTRAINT bloqueo_agenda_intervalo_valido CHECK (fin > inicio),
    CONSTRAINT bloqueo_agenda_motivo_no_vacio CHECK (btrim(motivo) <> '')
);

CREATE INDEX bloqueo_agenda_veterinario_periodo_idx
    ON bloqueo_agenda (veterinario_id, inicio, fin);

CREATE INDEX bloqueo_agenda_consultorio_periodo_idx
    ON bloqueo_agenda (consultorio_id, inicio, fin);

CREATE TABLE cita (
    id uuid PRIMARY KEY,
    mascota_id uuid NOT NULL,
    veterinario_id uuid NOT NULL,
    tipo varchar(20) NOT NULL,
    consultorio_id uuid REFERENCES consultorio(id),
    direccion varchar(500),
    inicio timestamptz NOT NULL,
    fin timestamptz NOT NULL,
    motivo text,
    estado varchar(20) NOT NULL,
    cancelada_por varchar(255),
    motivo_cancelacion varchar(500),
    reprogramada_de uuid REFERENCES cita(id) ON DELETE SET NULL,
    CONSTRAINT cita_tipo_valido CHECK (tipo IN ('CONSULTORIO', 'DOMICILIO')),
    CONSTRAINT cita_estado_valido
        CHECK (estado IN ('PENDIENTE', 'CONFIRMADA', 'ATENDIDA', 'NO_ASISTIO', 'CANCELADA')),
    CONSTRAINT cita_rango_valido CHECK (fin > inicio),
    CONSTRAINT cita_consultorio_requerido
        CHECK (tipo <> 'CONSULTORIO' OR consultorio_id IS NOT NULL),
    CONSTRAINT cita_consultorio_no_aplica
        CHECK (tipo <> 'DOMICILIO' OR consultorio_id IS NULL),
    CONSTRAINT cita_direccion_domicilio
        CHECK (tipo <> 'DOMICILIO' OR (direccion IS NOT NULL AND btrim(direccion) <> ''))
);

CREATE INDEX cita_mascota_inicio_idx ON cita (mascota_id, inicio);

ALTER TABLE cita
    ADD CONSTRAINT cita_sin_empalmes_veterinario
    EXCLUDE USING gist (
        veterinario_id ext.gist_uuid_ops WITH =,
        tstzrange(inicio, fin, '[)') WITH &&
    )
    WHERE (estado <> 'CANCELADA');

ALTER TABLE cita
    ADD CONSTRAINT cita_sin_empalmes_consultorio
    EXCLUDE USING gist (
        consultorio_id ext.gist_uuid_ops WITH =,
        tstzrange(inicio, fin, '[)') WITH &&
    )
    WHERE (estado <> 'CANCELADA' AND tipo = 'CONSULTORIO');
