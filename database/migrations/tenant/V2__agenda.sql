-- Estructura base de la agenda. `ext` ya contiene btree_gist desde catalogo/V1.

CREATE TABLE consultorio (
    id             bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre         varchar(100) NOT NULL
        CONSTRAINT consultorio_nombre_no_vacio CHECK (btrim(nombre) <> ''),
    activo         boolean     NOT NULL DEFAULT true,
    creado_en      timestamptz NOT NULL DEFAULT now(),
    actualizado_en timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE horario_consultorio (
    id                bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    consultorio_id    bigint      NOT NULL REFERENCES consultorio (id),
    dia_semana        smallint,
    fecha_excepcion   date,
    inicio            time,
    fin               time,
    es_cierre         boolean     NOT NULL DEFAULT false,
    creado_en         timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT horario_consultorio_dia_o_excepcion
        CHECK (
            (dia_semana IS NOT NULL AND dia_semana BETWEEN 1 AND 7 AND fecha_excepcion IS NULL)
            OR (dia_semana IS NULL AND fecha_excepcion IS NOT NULL)
        ),
    CONSTRAINT horario_consultorio_rango_valido
        CHECK (
            (es_cierre AND inicio IS NULL AND fin IS NULL)
            OR (NOT es_cierre AND inicio IS NOT NULL AND fin IS NOT NULL AND fin > inicio)
        )
);

CREATE INDEX horario_consultorio_semanal_idx
    ON horario_consultorio (consultorio_id, dia_semana)
    WHERE fecha_excepcion IS NULL;

CREATE INDEX horario_consultorio_excepcion_idx
    ON horario_consultorio (consultorio_id, fecha_excepcion)
    WHERE fecha_excepcion IS NOT NULL;

CREATE TABLE bloqueo_agenda (
    id              bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    veterinario_id  bigint,
    consultorio_id  bigint REFERENCES consultorio (id),
    inicio          timestamptz NOT NULL,
    fin             timestamptz NOT NULL,
    motivo          text        NOT NULL
        CONSTRAINT bloqueo_agenda_motivo_no_vacio CHECK (btrim(motivo) <> ''),
    creado_en       timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT bloqueo_agenda_un_solo_destino
        CHECK ((veterinario_id IS NOT NULL) <> (consultorio_id IS NOT NULL)),
    CONSTRAINT bloqueo_agenda_rango_valido CHECK (fin > inicio)
);

CREATE INDEX bloqueo_agenda_veterinario_rango_idx
    ON bloqueo_agenda (veterinario_id, inicio, fin)
    WHERE veterinario_id IS NOT NULL;

CREATE INDEX bloqueo_agenda_consultorio_rango_idx
    ON bloqueo_agenda (consultorio_id, inicio, fin)
    WHERE consultorio_id IS NOT NULL;

CREATE TABLE cita (
    id                 bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    mascota_id         bigint      NOT NULL REFERENCES mascota (id),
    veterinario_id     bigint      NOT NULL,
    consultorio_id     bigint REFERENCES consultorio (id),
    tipo               varchar(20) NOT NULL
        CONSTRAINT cita_tipo_valido CHECK (tipo IN ('CONSULTORIO', 'DOMICILIO')),
    direccion          varchar(300),
    inicio             timestamptz NOT NULL,
    fin                timestamptz NOT NULL,
    motivo             text,
    estado             varchar(20) NOT NULL DEFAULT 'PENDIENTE'
        CONSTRAINT cita_estado_valido
        CHECK (estado IN ('PENDIENTE', 'CONFIRMADA', 'ATENDIDA', 'NO_ASISTIO', 'CANCELADA')),
    origen             varchar(20) NOT NULL DEFAULT 'PERSONAL'
        CONSTRAINT cita_origen_valido CHECK (origen IN ('PERSONAL', 'CLIENTE')),
    cancelada_por      bigint,
    motivo_cancelacion text,
    reprogramada_de    bigint REFERENCES cita (id),
    creado_en          timestamptz NOT NULL DEFAULT now(),
    actualizado_en     timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT cita_tipo_destino_valido
        CHECK (
            (tipo = 'CONSULTORIO' AND consultorio_id IS NOT NULL AND direccion IS NULL)
            OR (
                tipo = 'DOMICILIO'
                AND consultorio_id IS NULL
                AND direccion IS NOT NULL
                AND btrim(direccion) <> ''
            )
        ),
    CONSTRAINT cita_rango_valido CHECK (fin > inicio),
    CONSTRAINT cita_cancelacion_valida
        CHECK (
            (
                estado = 'CANCELADA'
                AND cancelada_por IS NOT NULL
                AND motivo_cancelacion IS NOT NULL
                AND btrim(motivo_cancelacion) <> ''
            )
            OR (estado <> 'CANCELADA' AND cancelada_por IS NULL AND motivo_cancelacion IS NULL)
        ),
    CONSTRAINT cita_sin_empalmes_consultorio
        EXCLUDE USING gist (
            consultorio_id WITH =,
            tstzrange(inicio, fin, '[)') WITH &&
        )
        WHERE (estado <> 'CANCELADA'),
    CONSTRAINT cita_sin_empalmes_veterinario
        EXCLUDE USING gist (
            veterinario_id WITH =,
            tstzrange(inicio, fin, '[)') WITH &&
        )
        WHERE (estado <> 'CANCELADA')
);

CREATE INDEX cita_mascota_inicio_idx ON cita (mascota_id, inicio);
CREATE INDEX cita_veterinario_inicio_idx ON cita (veterinario_id, inicio);
