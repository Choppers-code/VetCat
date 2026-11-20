-- Estado de la migración de cada esquema de clínica.
--
-- El catálogo se migra una vez; los esquemas `clinica_xxx` se migran en bucle y
-- necesitan saber cuáles van al día y cuáles fallaron para reintentar sin
-- volver a aplicarlos (docs/Despliegue_y_operacion.md §5).
--
-- La tabla vive en `catalogo` porque es infraestructura de la plataforma, no
-- dato de ninguna clínica: no se copia a cada esquema.

CREATE TABLE catalogo.migracion_tenant (
    esquema          text        PRIMARY KEY,
    version_aplicada text,
    estado           varchar(20) NOT NULL DEFAULT 'pendiente',
    ultimo_intento   timestamptz,
    actualizado_en   timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT migracion_tenant_estado_valido
        CHECK (estado IN ('pendiente', 'aplicada', 'fallida'))
);

COMMENT ON TABLE catalogo.migracion_tenant IS
    'Una fila por esquema de clínica: qué versión tiene aplicada y si el último intento falló.';
COMMENT ON COLUMN catalogo.migracion_tenant.version_aplicada IS
    'Última versión de Flyway aplicada con éxito en el esquema.';
COMMENT ON COLUMN catalogo.migracion_tenant.estado IS
    'pendiente, aplicada o fallida. `varchar` con CHECK, nunca un enum: cambiarlo por esquema está prohibido.';
