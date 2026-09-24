-- Datos de ejemplo: formato destino actual y perfil de "Lista Ferreteria 19 08.xlsx".
-- Porcentajes tomados de KALLAYCORREGIDO.xlsx.

INSERT INTO usuario (nombre, email, password_hash, rol)
VALUES ('Administrador', 'admin@tpi.local', '<bcrypt>', 'ADMIN');

INSERT INTO formato_destino (nombre) VALUES ('plataforma_anterior');

INSERT INTO campo_destino (formato_destino_id, clave, etiqueta, tipo_dato, obligatorio, orden)
SELECT 1, c.clave, c.etiqueta, c.tipo::tipo_dato, c.obl, c.orden
FROM (VALUES
    ('codigo',        'codigo',          'TEXTO',   TRUE,  1),
    ('producto',      'producto',        'TEXTO',   TRUE,  2),
    ('precio_costo',  'precio de costo', 'DECIMAL', TRUE,  3),
    ('ingreso_bruto', 'ingreso bruto',   'DECIMAL', FALSE, 4),
    ('ganancia',      'ganancia',        'DECIMAL', FALSE, 5),
    ('iva',           'iva',             'DECIMAL', FALSE, 6),
    ('costo_mas_iva', 'costo + iva',     'DECIMAL', FALSE, 7),
    ('stock',         'stock',           'ENTERO',  FALSE, 8),
    ('precio_venta',  '% ganancia',      'DECIMAL', FALSE, 9)
) AS c(clave, etiqueta, tipo, obl, orden);

INSERT INTO proveedor (nombre) VALUES ('Ferretería');

-- Hoja LSTPRE: encabezados en la fila 7, datos desde la fila 8.
INSERT INTO perfil_importacion (proveedor_id, formato_destino_id, nombre, hoja,
                                fila_encabezado, fila_inicio_datos, creado_por)
VALUES (1, 1, 'Lista general', 'LSTPRE', 7, 8, 1);

-- A → codigo, B → producto, C → precio_costo. D y E se ignoran.
INSERT INTO mapeo_columna (perfil_id, columna_excel, encabezado_esperado, campo_destino_id, transformaciones)
VALUES (1, 'A', 'CODIGO',  1,    '{TRIM}'),
       (1, 'B', 'DESCRIP', 2,    '{TRIM}'),
       (1, 'C', 'PRECIO',  3,    '{A_DECIMAL}'),
       (1, 'D', 'CON_IVA', NULL, '{}'),
       (1, 'E', 'TIP_LST', NULL, '{}');

-- Misma cadena de cálculo que las planillas corregidas a mano.
INSERT INTO regla_calculo (perfil_id, orden, campo_resultado_id, operacion, campo_base_id, campo_secundario_id, valor)
VALUES (1, 1, 4, 'SUMAR_PORCENTAJE', 3, NULL, 0.04),  -- ingreso bruto = costo + 4 %
       (1, 2, 5, 'SUMAR_PORCENTAJE', 4, NULL, 0.50),  -- ganancia      = ingreso bruto + 50 %
       (1, 3, 6, 'MULTIPLICAR',      5, NULL, 0.21),  -- iva           = ganancia × 21 %
       (1, 4, 7, 'SUMAR_CAMPO',      5, 6,    NULL),  -- costo + iva   = ganancia + iva
       (1, 5, 9, 'SUMAR_PORCENTAJE', 7, NULL, 0.30);  -- precio venta  = costo + iva + 30 %
