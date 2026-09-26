# Base de datos

[← README](../README.md)

**Motor:** PostgreSQL. Script: [`database/schema.sql`](../database/schema.sql) · Datos de ejemplo: [`database/seed.sql`](../database/seed.sql)

## Por qué PostgreSQL

Los datos del sistema (usuarios, proveedores, perfiles, importaciones, errores) tienen estructura fija y están relacionados entre sí. Lo único que cambia según el proveedor es la fila original del Excel, y eso se guarda en una columna `JSONB`. Por eso alcanza con una sola base relacional.

## Diagrama

```mermaid
erDiagram
    USUARIO ||--o{ PERFIL_IMPORTACION : crea
    USUARIO ||--o{ IMPORTACION : ejecuta
    PROVEEDOR ||--o{ PERFIL_IMPORTACION : tiene
    FORMATO_DESTINO ||--|{ CAMPO_DESTINO : define
    FORMATO_DESTINO ||--o{ PERFIL_IMPORTACION : "es salida de"
    PERFIL_IMPORTACION ||--|{ MAPEO_COLUMNA : mapea
    PERFIL_IMPORTACION ||--o{ REGLA_CALCULO : calcula
    CAMPO_DESTINO |o--o{ MAPEO_COLUMNA : recibe
    PERFIL_IMPORTACION ||--o{ IMPORTACION : "se usa en"
    IMPORTACION ||--|{ FILA_IMPORTADA : contiene
    FILA_IMPORTADA ||--o{ ERROR_FILA : registra

    USUARIO { bigint id PK
              varchar email UK
              rol_usuario rol }
    PROVEEDOR { bigint id PK
                varchar nombre UK }
    FORMATO_DESTINO { bigint id PK
                      varchar nombre
                      int version }
    CAMPO_DESTINO { bigint id PK
                    varchar clave
                    tipo_dato tipo_dato
                    boolean obligatorio
                    smallint orden }
    PERFIL_IMPORTACION { bigint id PK
                         varchar hoja
                         int fila_encabezado
                         int fila_inicio_datos
                         int version }
    MAPEO_COLUMNA { bigint id PK
                    varchar columna_excel
                    varchar encabezado_esperado
                    bigint campo_destino_id FK }
    REGLA_CALCULO { bigint id PK
                    smallint orden
                    operacion_calculo operacion
                    numeric valor }
    IMPORTACION { bigint id PK
                  varchar nombre_archivo
                  estado_importacion estado
                  boolean estructura_coincide }
    FILA_IMPORTADA { bigint id PK
                     int numero_fila
                     estado_fila estado
                     jsonb datos_crudos
                     jsonb datos_resultado }
    ERROR_FILA { bigint id PK
                 varchar mensaje }
```

## Tablas

| Tabla | Qué guarda |
| --- | --- |
| `usuario` | Usuarios del sistema (admin u operador) |
| `proveedor` | Proveedores que envían listas |
| `formato_destino` / `campo_destino` | Formato que acepta el sistema destino y sus columnas |
| `perfil_importacion` | Contrato de una hoja: hoja, fila de encabezados y fila de inicio |
| `mapeo_columna` | Columna del Excel → campo destino (o ignorada) y sus transformaciones |
| `regla_calculo` | Cálculos en orden (IIBB, ganancia, IVA…) |
| `importacion` | Historial: qué archivo, quién, cuándo, resultado |
| `fila_importada` | Cada fila leída, original y transformada |
| `error_fila` | Motivo de rechazo de una fila |

## Decisiones

- **Cambio de formato:** `encabezado_esperado` se compara con el archivo nuevo. Si no coincide, se avisa antes de procesar.
- **Versiones de perfil:** si el proveedor cambia su lista, se crea una versión nueva y el historial viejo no se toca.
- **Tiempo de guardado:** las importaciones se borran a los 90 días (`expira_en`).
- **Cálculos acotados:** solo las operaciones del tipo `operacion_calculo`, sin fórmulas libres.
