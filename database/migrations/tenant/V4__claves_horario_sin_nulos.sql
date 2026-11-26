ALTER TABLE horario_consultorio
    DROP CONSTRAINT horario_consultorio_sin_contradicciones;

ALTER TABLE horario_consultorio
    ADD CONSTRAINT horario_consultorio_sin_contradicciones
    EXCLUDE USING gist (
        consultorio_id WITH =,
        (COALESCE(dia_semana, 0)) WITH =,
        (COALESCE(fecha_excepcion, DATE '0001-01-01')) WITH =,
        (tsrange(
            CASE WHEN es_cierre THEN timestamp '2000-01-01'
                 ELSE timestamp '2000-01-01' + inicio END,
            CASE WHEN es_cierre THEN timestamp '2000-01-02'
                 ELSE timestamp '2000-01-01' + fin END,
            '[)'
        )) WITH &&
    )
    WHERE (activo);
