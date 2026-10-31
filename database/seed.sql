-- Datos de ejemplo: formatos destino y perfiles de los Excel reales.
-- Cada proveedor tiene sus propios porcentajes: no se copian reglas de uno a otro.

INSERT INTO usuario (nombre, email, password_hash, rol)
VALUES ('Administrador', 'admin@tpi.local', '<bcrypt>', 'ADMIN');

-- 1. Formato de importación de Administralo (sistema destino real). Campos con id 1..11.
--    No se exportan columnas de porcentaje: el modelo los guarda como fracción y Administralo como 21.
INSERT INTO formato_destino (nombre) VALUES ('administralo');

INSERT INTO campo_destino (formato_destino_id, clave, etiqueta, tipo_dato, obligatorio, exportado, orden)
SELECT 1, c.clave, c.etiqueta, c.tipo::tipo_dato, c.obl, c.exp, c.orden
FROM (VALUES
    ('codigo',          'Codigo',          'TEXTO',   FALSE, TRUE,   1),
    ('nombre',          'Nombre',          'TEXTO',   TRUE,  TRUE,   2),
    ('descripcion',     'Descripcion',     'TEXTO',   FALSE, TRUE,   3),
    ('categoria',       'Categoria',       'TEXTO',   FALSE, TRUE,   4),
    ('stock',           'Stock',           'ENTERO',  FALSE, TRUE,   5),
    ('precio_costo',    'Precio de costo', 'DECIMAL', TRUE,  FALSE,  6),
    ('ingresos_brutos', 'Ingresos brutos', 'DECIMAL', FALSE, TRUE,   7),
    ('ganancia',        'Ganancia',        'DECIMAL', FALSE, TRUE,   8),
    ('iva',             'IVA',             'DECIMAL', FALSE, TRUE,   9),
    ('precio_mas_iva',  'Precio + IVA',    'DECIMAL', FALSE, TRUE,  10),
    ('precio',          'Precio',          'DECIMAL', TRUE,  TRUE,  11)
) AS c(clave, etiqueta, tipo, obl, exp, orden);

-- 2. Planilla de la plataforma anterior (dataset-previous-platform). Campos con id 12..20.
--    Se usa como referencia para comprobar los cálculos contra las planillas corregidas a mano.
INSERT INTO formato_destino (nombre) VALUES ('plataforma_anterior');

INSERT INTO campo_destino (formato_destino_id, clave, etiqueta, tipo_dato, obligatorio, orden)
SELECT 2, c.clave, c.etiqueta, c.tipo::tipo_dato, c.obl, c.orden
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

INSERT INTO proveedor (nombre) VALUES ('Ferretería'), ('Paint Roller'), ('Kallay'), ('Bulones'), ('Matienzo');

-- Perfil 1. Ferretería, hoja LSTPRE. Encabezados en la fila 7 (en algunas versiones, la 8: margen 3).
--           Desde ahí hay filas vacías, títulos de categoría y productos.
-- Perfil 2. Paint Roller, hoja " Paint Roller " (con espacios). Encabezados en la fila 24; el nombre del
--           producto viene una sola vez por grupo y se completa hacia abajo.
INSERT INTO perfil_importacion (proveedor_id, formato_destino_id, nombre, hoja, fila_encabezado, margen_encabezado,
                                fila_inicio_datos, campo_clave_id, campo_categoria_id, creado_por)
VALUES (1, 1, 'Lista general',    'LSTPRE',         7, 3,  8, 1, 4, 1),
       (2, 1, 'Lista mayorista', ' Paint Roller ', 24, 3, 25, 1, 4, 1);

INSERT INTO mapeo_columna (perfil_id, columna_excel, encabezado_esperado, campo_destino_id, transformaciones)
VALUES (1, 'A', 'CODIGO',               1,    '{TRIM}'),
       (1, 'B', 'DESCRIP',              2,    '{TRIM}'),
       (1, 'C', 'PRECIO',               6,    '{A_DECIMAL}'),
       (1, 'D', 'CON_IVA',              NULL, '{}'),
       (1, 'E', 'TIP_LST',              NULL, '{}'),
       (2, 'A', 'Producto',             2,    '{TRIM,RELLENAR_ABAJO}'),
       (2, 'B', 'Codigo',               1,    '{TRIM}'),
       (2, 'C', 'Medida',               3,    '{TRIM}'),
       (2, 'D', 'Bulto',                NULL, '{}'),
       (2, 'E', 'Aclaracion',           NULL, '{}'),
       (2, 'F', 'Precio lista',         6,    '{A_DECIMAL}'),
       (2, 'G', 'Ultima actualizacion', NULL, '{}');

-- Porcentajes de Ferretería y Paint Roller pendientes de confirmar con el negocio.
-- Regla provisoria y explícita: precio = costo. No se toman porcentajes de otro proveedor.
INSERT INTO regla_calculo (perfil_id, orden, campo_resultado_id, operacion, campo_base_id, campo_secundario_id, valor)
VALUES (1, 1, 11, 'COPIAR',     6, NULL, NULL),  -- precio = costo (provisorio)
       (2, 1,  2, 'CONCATENAR', 2, 3,    NULL),  -- nombre = nombre del grupo + medida (como la corrección manual)
       (2, 2, 11, 'COPIAR',     6, NULL, NULL);  -- precio = costo (provisorio)

-- Perfiles 3-5. Planillas corregidas a mano. Fila 2 = porcentajes, datos desde la 3.
-- Entrada: columnas A, B, C y H. El resto es el resultado esperado y solo se controla su encabezado.
INSERT INTO perfil_importacion (proveedor_id, formato_destino_id, nombre, hoja,
                                fila_encabezado, fila_inicio_datos, campo_clave_id, creado_por)
VALUES (3, 2, 'Corregido', 'Hoja1', 1, 3, 12, 1),
       (4, 2, 'Corregido', 'Hoja1', 1, 3, 12, 1),
       (5, 2, 'Corregido', 'Hoja1', 1, 3, 12, 1);

INSERT INTO mapeo_columna (perfil_id, columna_excel, encabezado_esperado, campo_destino_id, transformaciones)
SELECT p.id, m.col, m.enc, m.campo, m.tr::transformacion[]
FROM perfil_importacion p
CROSS JOIN (VALUES
    ('A', 'codigo',          12,   '{TRIM}'),
    ('B', 'producto',        13,   '{TRIM}'),
    ('C', 'precio de costo', 14,   '{A_DECIMAL}'),
    ('D', 'ingreso bruto',   NULL, '{}'),
    ('E', 'ganancia',        NULL, '{}'),
    ('F', 'iva',             NULL, '{}'),
    ('G', 'costo + iva',     NULL, '{}'),
    ('H', 'stock',           19,   '{A_ENTERO}'),
    ('I', '% ganancia',      NULL, '{}')
) AS m(col, enc, campo, tr)
WHERE p.id IN (3, 4, 5);

-- Kallay: IIBB 4 %, ganancia 50 %, IVA 21 % sobre ganancia, venta +30 %.
INSERT INTO regla_calculo (perfil_id, orden, campo_resultado_id, operacion, campo_base_id, campo_secundario_id, valor)
VALUES (3, 1, 15, 'SUMAR_PORCENTAJE', 14, NULL, 0.04),  -- ingreso bruto = costo + 4 %
       (3, 2, 16, 'SUMAR_PORCENTAJE', 15, NULL, 0.50),  -- ganancia      = ingreso bruto + 50 %
       (3, 3, 17, 'MULTIPLICAR',      16, NULL, 0.21),  -- iva           = ganancia × 21 %
       (3, 4, 18, 'SUMAR_CAMPO',      16, 17,   NULL),  -- costo + iva   = ganancia + iva
       (3, 5, 20, 'SUMAR_PORCENTAJE', 18, NULL, 0.30);  -- precio venta  = costo + iva + 30 %

-- Bulones: IIBB 4 %, ganancia 40 %, IVA 10,5 % sobre el COSTO (opción válida, Administralo la llama
-- "base costo"), venta +70 %.
INSERT INTO regla_calculo (perfil_id, orden, campo_resultado_id, operacion, campo_base_id, campo_secundario_id, valor)
VALUES (4, 1, 15, 'SUMAR_PORCENTAJE', 14, NULL, 0.04),
       (4, 2, 16, 'SUMAR_PORCENTAJE', 15, NULL, 0.40),
       (4, 3, 17, 'MULTIPLICAR',      14, NULL, 0.105),
       (4, 4, 18, 'SUMAR_CAMPO',      16, 17,   NULL),
       (4, 5, 20, 'SUMAR_PORCENTAJE', 18, NULL, 0.70);

-- Matienzo: IIBB 4 %, ganancia 30 %, IVA 21 % sobre ganancia, venta +60 %.
INSERT INTO regla_calculo (perfil_id, orden, campo_resultado_id, operacion, campo_base_id, campo_secundario_id, valor)
VALUES (5, 1, 15, 'SUMAR_PORCENTAJE', 14, NULL, 0.04),
       (5, 2, 16, 'SUMAR_PORCENTAJE', 15, NULL, 0.30),
       (5, 3, 17, 'MULTIPLICAR',      16, NULL, 0.21),
       (5, 4, 18, 'SUMAR_CAMPO',      16, 17,   NULL),
       (5, 5, 20, 'SUMAR_PORCENTAJE', 18, NULL, 0.60);
