-- Propietarios y mascotas del esquema de una clínica (PRO-001 a PRO-009).
--
-- Sin nombre de esquema: el migrador fija el `search_path` en el esquema de la clínica.
-- Estados con `varchar` y CHECK, nunca `CREATE TYPE`, porque un tipo se crearía una vez
-- por esquema.

CREATE TABLE propietario (
    id                    bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre                varchar(150) NOT NULL CHECK (btrim(nombre) <> ''),
    telefono              varchar(30)  NOT NULL CHECK (btrim(telefono) <> ''),
    -- Solo los dígitos, para detectar el mismo teléfono escrito de otra forma.
    telefono_normalizado  text GENERATED ALWAYS AS (regexp_replace(telefono, '\D', '', 'g')) STORED,
    correo                varchar(254),
    direccion             varchar(300),
    activo                boolean      NOT NULL DEFAULT true,
    creado_en             timestamptz  NOT NULL DEFAULT now(),
    actualizado_en        timestamptz  NOT NULL DEFAULT now()
);

CREATE INDEX propietario_telefono_normalizado_idx ON propietario (telefono_normalizado);

CREATE TABLE mascota (
    id                bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    propietario_id    bigint       NOT NULL REFERENCES propietario (id),
    nombre            varchar(100) NOT NULL CHECK (btrim(nombre) <> ''),
    especie           varchar(20)  NOT NULL
        CONSTRAINT mascota_especie_valida
        CHECK (especie IN ('PERRO', 'GATO', 'AVE', 'ROEDOR', 'REPTIL', 'OTRA')),
    raza              varchar(100),
    sexo              varchar(15)
        CONSTRAINT mascota_sexo_valido
        CHECK (sexo IN ('MACHO', 'HEMBRA', 'DESCONOCIDO')),
    fecha_nacimiento  date
        CONSTRAINT mascota_nacimiento_no_futuro CHECK (fecha_nacimiento <= CURRENT_DATE),
    peso_kg           numeric(6, 2)
        CONSTRAINT mascota_peso_positivo CHECK (peso_kg > 0),
    notas             text,
    activo            boolean      NOT NULL DEFAULT true,
    creado_en         timestamptz  NOT NULL DEFAULT now(),
    actualizado_en    timestamptz  NOT NULL DEFAULT now()
);

CREATE INDEX mascota_propietario_idx ON mascota (propietario_id);

-- Una mascota no cambia de propietario (PRO-008). El servicio ya lo rechaza; el
-- trigger sostiene la regla aunque falle la aplicación o alguien edite por SQL.
CREATE FUNCTION mascota_propietario_inmutable() RETURNS trigger AS $$
BEGIN
    IF NEW.propietario_id IS DISTINCT FROM OLD.propietario_id THEN
        RAISE EXCEPTION 'Una mascota no puede cambiar de propietario'
            USING ERRCODE = 'check_violation';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER mascota_propietario_inmutable
    BEFORE UPDATE ON mascota
    FOR EACH ROW EXECUTE FUNCTION mascota_propietario_inmutable();
