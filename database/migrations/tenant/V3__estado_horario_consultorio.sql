ALTER TABLE horario_consultorio
    ADD COLUMN activo boolean NOT NULL DEFAULT true,
    ADD COLUMN actualizado_en timestamptz NOT NULL DEFAULT now(),
    ADD CONSTRAINT horario_consultorio_sin_contradicciones
        EXCLUDE USING gist (
            consultorio_id WITH =,
            dia_semana WITH =,
            fecha_excepcion WITH =,
            (tsrange(
                CASE WHEN es_cierre THEN timestamp '2000-01-01'
                     ELSE timestamp '2000-01-01' + inicio END,
                CASE WHEN es_cierre THEN timestamp '2000-01-02'
                     ELSE timestamp '2000-01-01' + fin END,
                '[)'
            )) WITH &&
        )
        WHERE (activo);
