CREATE TABLE archivo_carga (
    id_archivo       BIGSERIAL PRIMARY KEY,
    nombre           VARCHAR(255) NOT NULL,
    fecha_carga      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado           VARCHAR(40) NOT NULL
        CHECK (estado IN ('validado', 'validado_con_observaciones', 'rechazado')),
    registros_leidos INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE incidencia_calidad (
    id_incidencia   BIGSERIAL PRIMARY KEY,
    id_archivo      BIGINT NOT NULL REFERENCES archivo_carga (id_archivo),
    tipo_incidencia VARCHAR(40) NOT NULL
        CHECK (tipo_incidencia IN ('campo_vacio', 'formato_invalido', 'duplicado')),
    detalle         VARCHAR(500)
);

CREATE TABLE cliente (
    id_cliente VARCHAR(50) PRIMARY KEY
);

CREATE TABLE compra (
    id_compra     BIGSERIAL PRIMARY KEY,
    id_archivo    BIGINT NOT NULL REFERENCES archivo_carga (id_archivo),
    id_cliente    VARCHAR(50) NOT NULL REFERENCES cliente (id_cliente),
    codigo_compra VARCHAR(50) NOT NULL,
    fecha_compra  DATE NOT NULL,
    valor_compra  NUMERIC(12, 2) NOT NULL CHECK (valor_compra > 0),
    UNIQUE (id_archivo, codigo_compra)
);

CREATE TABLE ejecucion (
    id_ejecucion     BIGSERIAL PRIMARY KEY,
    id_archivo       BIGINT NOT NULL REFERENCES archivo_carga (id_archivo),
    tecnica          VARCHAR(40) NOT NULL,
    variables_usadas VARCHAR(255) NOT NULL,
    parametros       VARCHAR(255),
    fecha            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado           VARCHAR(20) NOT NULL
        CHECK (estado IN ('en_proceso', 'completada', 'fallida')),
    mensaje_error    VARCHAR(500)
);

CREATE TABLE segmento (
    id_segmento         BIGSERIAL PRIMARY KEY,
    id_ejecucion        BIGINT NOT NULL REFERENCES ejecucion (id_ejecucion),
    etiqueta            VARCHAR(100),
    descripcion         VARCHAR(1000),
    num_clientes        INTEGER NOT NULL,
    frecuencia_media    NUMERIC(12, 2),
    valor_total_medio   NUMERIC(12, 2),
    recencia_media_dias NUMERIC(12, 2)
);

CREATE TABLE asignacion (
    id_segmento BIGINT NOT NULL REFERENCES segmento (id_segmento),
    id_cliente  VARCHAR(50) NOT NULL REFERENCES cliente (id_cliente),
    PRIMARY KEY (id_segmento, id_cliente)
);

CREATE INDEX idx_incidencia_archivo ON incidencia_calidad (id_archivo);
CREATE INDEX idx_compra_archivo ON compra (id_archivo);
CREATE INDEX idx_compra_cliente ON compra (id_cliente);
CREATE INDEX idx_ejecucion_archivo ON ejecucion (id_archivo);
CREATE INDEX idx_segmento_ejecucion ON segmento (id_ejecucion);