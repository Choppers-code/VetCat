-- Planes y funciones habilitadas (docs/requerimientos/02_PLA_planes.md).
--
-- Un solo modelo de datos: el plan enciende funciones y fija límites, sin
-- duplicar esquemas. Los nombres, precios y límites se cargan desde datos,
-- nunca desde el código.

CREATE TABLE catalogo.plan (
    id            bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre        text        NOT NULL,
    descripcion   text,
    activo        boolean     NOT NULL DEFAULT true,
    creado_en     timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT plan_nombre_unico UNIQUE (nombre),
    CONSTRAINT plan_nombre_no_vacio CHECK (length(btrim(nombre)) > 0)
);

COMMENT ON TABLE catalogo.plan IS
    'Catálogo de planes. Un plan en uso no se borra: se marca inactivo (PLA-001).';

CREATE TABLE catalogo.plan_funcion (
    plan_id  bigint      NOT NULL REFERENCES catalogo.plan (id),
    funcion  varchar(30) NOT NULL,
    PRIMARY KEY (plan_id, funcion),
    CONSTRAINT plan_funcion_valida CHECK (funcion IN (
        'portal_cliente', 'recordatorios', 'ventas', 'turno_caja',
        'inventario', 'visitas_domicilio', 'reportes'
    ))
);

COMMENT ON TABLE catalogo.plan_funcion IS
    'Funciones de plan incluidas. Las claves son las de PLA §Funciones de plan.';

CREATE TABLE catalogo.plan_limite (
    plan_id  bigint      NOT NULL REFERENCES catalogo.plan (id),
    recurso  varchar(20) NOT NULL,
    maximo   integer     NOT NULL,
    PRIMARY KEY (plan_id, recurso),
    CONSTRAINT plan_limite_recurso_valido CHECK (recurso IN ('usuarios', 'veterinarios', 'mascotas')),
    CONSTRAINT plan_limite_positivo CHECK (maximo > 0)
);

COMMENT ON TABLE catalogo.plan_limite IS
    'Una fila por límite. Si el recurso no tiene fila, el plan no lo limita (PLA-001).';

CREATE TABLE catalogo.pago_plan (
    id            bigint        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    clinica_id    bigint        NOT NULL REFERENCES catalogo.clinica (id),
    fecha         timestamptz   NOT NULL DEFAULT now(),
    monto         numeric(12,2) NOT NULL,
    periodo_desde date          NOT NULL,
    periodo_hasta date          NOT NULL,
    autor         text          NOT NULL,
    CONSTRAINT pago_monto_no_negativo CHECK (monto >= 0),
    CONSTRAINT pago_periodo_valido CHECK (periodo_hasta > periodo_desde),
    CONSTRAINT pago_autor_no_vacio CHECK (length(btrim(autor)) > 0)
);

COMMENT ON TABLE catalogo.pago_plan IS
    'Pagos manuales que extienden la vigencia del plan (PLA-006). Dinero en NUMERIC, nunca en coma flotante.';

-- La clínica tiene exactamente un plan (PLA-002). La columna queda anulable en
-- esta migración: las clínicas existentes no se pueden rellenar solas y el
-- aprovisionamiento es quien fija el plan al darlas de alta.
ALTER TABLE catalogo.clinica
    ADD COLUMN plan_id bigint REFERENCES catalogo.plan (id);

COMMENT ON COLUMN catalogo.clinica.plan_id IS
    'Plan vigente de la clínica. Lo fija el aprovisionamiento y lo cambia el super_admin (PLA-002).';
