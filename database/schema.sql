-- TPI Final — Desiderio, Vazquez
-- Esquema PostgreSQL. Documentación: docs/base-de-datos.md

CREATE TYPE rol_usuario        AS ENUM ('ADMIN', 'OPERADOR');
CREATE TYPE tipo_dato          AS ENUM ('TEXTO', 'DECIMAL', 'ENTERO');
CREATE TYPE transformacion     AS ENUM ('TRIM', 'MAYUSCULAS', 'A_DECIMAL', 'A_ENTERO');
CREATE TYPE operacion_calculo  AS ENUM ('COPIAR', 'VALOR_FIJO', 'SUMAR_PORCENTAJE', 'RESTAR_PORCENTAJE',
                                        'MULTIPLICAR', 'DIVIDIR', 'SUMAR_CAMPO', 'CONCATENAR');
CREATE TYPE estado_importacion AS ENUM ('PREVIEW', 'CONFIRMADA', 'CANCELADA');
CREATE TYPE estado_fila        AS ENUM ('VALIDA', 'IGNORADA', 'ERROR');

CREATE TABLE usuario (
    id            BIGSERIAL    PRIMARY KEY,
    nombre        VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    rol           rol_usuario  NOT NULL DEFAULT 'OPERADOR',
    activo        BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE proveedor (
    id     BIGSERIAL    PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL UNIQUE,
    activo BOOLEAN      NOT NULL DEFAULT TRUE
);

-- Formato que acepta el sistema destino y sus columnas.
CREATE TABLE formato_destino (
    id      BIGSERIAL   PRIMARY KEY,
    nombre  VARCHAR(80) NOT NULL,
    version INTEGER     NOT NULL DEFAULT 1,
    UNIQUE (nombre, version)
);

CREATE TABLE campo_destino (
    id                 BIGSERIAL   PRIMARY KEY,
    formato_destino_id BIGINT      NOT NULL REFERENCES formato_destino(id) ON DELETE CASCADE,
    clave              VARCHAR(50) NOT NULL,   -- precio_costo
    etiqueta           VARCHAR(80) NOT NULL,   -- "precio de costo"
    tipo_dato          tipo_dato   NOT NULL,
    obligatorio        BOOLEAN     NOT NULL DEFAULT FALSE,
    orden              SMALLINT    NOT NULL,
    UNIQUE (formato_destino_id, clave),
    UNIQUE (formato_destino_id, orden)
);

-- Perfil de importación: el contrato de una hoja de un proveedor.
CREATE TABLE perfil_importacion (
    id                 BIGSERIAL    PRIMARY KEY,
    proveedor_id       BIGINT       NOT NULL REFERENCES proveedor(id),
    formato_destino_id BIGINT       NOT NULL REFERENCES formato_destino(id),
    nombre             VARCHAR(100) NOT NULL,
    version            INTEGER      NOT NULL DEFAULT 1,
    activo             BOOLEAN      NOT NULL DEFAULT TRUE,
    hoja               VARCHAR(100) NOT NULL,
    fila_encabezado    INTEGER      NOT NULL CHECK (fila_encabezado >= 1),
    fila_inicio_datos  INTEGER      NOT NULL,
    creado_por         BIGINT       NOT NULL REFERENCES usuario(id),
    creado_en          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (proveedor_id, nombre, version),
    CHECK (fila_inicio_datos > fila_encabezado)
);

CREATE TABLE mapeo_columna (
    id                  BIGSERIAL        PRIMARY KEY,
    perfil_id           BIGINT           NOT NULL REFERENCES perfil_importacion(id) ON DELETE CASCADE,
    columna_excel       VARCHAR(3)       NOT NULL,           -- A, B, C...
    encabezado_esperado VARCHAR(150)     NOT NULL,           -- detecta cambios de formato
    campo_destino_id    BIGINT           REFERENCES campo_destino(id), -- NULL = columna ignorada
    transformaciones    transformacion[] NOT NULL DEFAULT '{}',
    UNIQUE (perfil_id, columna_excel)
);

CREATE TABLE regla_calculo (
    id                  BIGSERIAL         PRIMARY KEY,
    perfil_id           BIGINT            NOT NULL REFERENCES perfil_importacion(id) ON DELETE CASCADE,
    orden               SMALLINT          NOT NULL,
    campo_resultado_id  BIGINT            NOT NULL REFERENCES campo_destino(id),
    operacion           operacion_calculo NOT NULL,
    campo_base_id       BIGINT            REFERENCES campo_destino(id),
    campo_secundario_id BIGINT            REFERENCES campo_destino(id),
    valor               NUMERIC(12,6),    -- 0.04 = 4 %
    UNIQUE (perfil_id, orden)
);

-- Historial de importaciones.
CREATE TABLE importacion (
    id                  BIGSERIAL          PRIMARY KEY,
    perfil_id           BIGINT             NOT NULL REFERENCES perfil_importacion(id),
    usuario_id          BIGINT             NOT NULL REFERENCES usuario(id),
    nombre_archivo      VARCHAR(255)       NOT NULL,
    estado              estado_importacion NOT NULL DEFAULT 'PREVIEW',
    estructura_coincide BOOLEAN            NOT NULL,
    filas_validas       INTEGER            NOT NULL DEFAULT 0,
    filas_ignoradas     INTEGER            NOT NULL DEFAULT 0,
    filas_con_error     INTEGER            NOT NULL DEFAULT 0,
    creado_en           TIMESTAMPTZ        NOT NULL DEFAULT now(),
    expira_en           TIMESTAMPTZ        NOT NULL DEFAULT now() + INTERVAL '90 days'
);

CREATE TABLE fila_importada (
    id              BIGSERIAL   PRIMARY KEY,
    importacion_id  BIGINT      NOT NULL REFERENCES importacion(id) ON DELETE CASCADE,
    numero_fila     INTEGER     NOT NULL,
    estado          estado_fila NOT NULL,
    datos_crudos    JSONB       NOT NULL,   -- fila tal como vino del Excel
    datos_resultado JSONB,                  -- fila transformada (formato destino)
    UNIQUE (importacion_id, numero_fila)
);

CREATE TABLE error_fila (
    id               BIGSERIAL    PRIMARY KEY,
    fila_id          BIGINT       NOT NULL REFERENCES fila_importada(id) ON DELETE CASCADE,
    campo_destino_id BIGINT       REFERENCES campo_destino(id),
    mensaje          VARCHAR(255) NOT NULL  -- "precio no numérico"
);
