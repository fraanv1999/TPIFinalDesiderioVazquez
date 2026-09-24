# Esquema de base de datos

[← Volver al README](../README.md) · **2.ª Entrega — Diseño y Módulos**

- **Motor:** PostgreSQL (relacional), con columnas `JSONB` solo donde el contenido depende de cada proveedor.
- **DDL completo:** [`database/schema.sql`](../database/schema.sql)
- **Datos de ejemplo** (formato destino + perfil del primer caso real): [`database/seed.sql`](../database/seed.sql)

---

## 1. Por qué PostgreSQL (y no MongoDB)

En las dos devoluciones, el tutor preguntó qué heterogeneidad justificaba MongoDB si después del proceso los datos quedan normalizados. Al rediseñar sobre el dataset real, la respuesta es que **casi todo el dominio es relacional y tiene estructura fija**:

| Entidad | ¿Estructura fija? | Relaciones |
| --- | --- | --- |
| usuarios, proveedores | Sí | proveedor → perfiles |
| formato destino y sus campos | Sí | campos → mapeos, cálculos |
| perfil (hoja, filas, mapeos, transformaciones, cálculos) | **Sí**: lo que varía es el *contenido*, no la *forma* | perfil → mapeos → transformaciones |
| importaciones, errores, exportaciones | Sí | importación → filas → errores |
| **fila cruda del Excel** | **No**: columnas distintas por proveedor | → `JSONB` |
| **resultado por fila** | Depende de los campos del formato destino | → `JSONB`, validado contra `campo_destino` |

Además hay **integridad referencial real**: un mapeo apunta a un campo del formato destino, un cálculo a campos existentes y un error a una fila de una importación. Con claves foráneas y `CHECK` eso lo garantiza la base, no el código.

El único dato que de verdad varía es la fila cruda, y `JSONB` lo resuelve sin sumar un segundo motor. Se usa **una sola base**, como sugirió el tutor.

---

## 2. Diagrama entidad-relación

```mermaid
erDiagram
    USUARIO ||--o{ PROVEEDOR : "da de alta"
    USUARIO ||--o{ PERFIL_IMPORTACION : "crea"
    USUARIO ||--o{ IMPORTACION : "ejecuta"
    USUARIO ||--o{ EXPORTACION : "genera"

    PROVEEDOR ||--o{ PERFIL_IMPORTACION : "tiene (uno por hoja/versión)"
    FORMATO_DESTINO ||--|{ CAMPO_DESTINO : "define"
    FORMATO_DESTINO ||--o{ PERFIL_IMPORTACION : "es la salida de"

    PERFIL_IMPORTACION ||--|{ MAPEO_COLUMNA : "mapea"
    PERFIL_IMPORTACION ||--o{ REGLA_CALCULO : "calcula"
    MAPEO_COLUMNA ||--o{ TRANSFORMACION : "aplica"
    CAMPO_DESTINO |o--o{ MAPEO_COLUMNA : "recibe"
    CAMPO_DESTINO ||--o{ REGLA_CALCULO : "es resultado / base"

    PERFIL_IMPORTACION ||--o{ IMPORTACION : "se usa en"
    IMPORTACION ||--|{ FILA_IMPORTADA : "contiene"
    FILA_IMPORTADA ||--o{ ERROR_FILA : "registra"
    IMPORTACION ||--o{ EXPORTACION : "produce"

    USUARIO {
        bigint id PK
        varchar nombre
        varchar email UK
        varchar password_hash
        rol_usuario rol "ADMIN | OPERADOR"
        boolean activo
        timestamptz creado_en
    }
    PROVEEDOR {
        bigint id PK
        varchar nombre UK
        varchar cuit
        varchar contacto
        boolean activo
        bigint creado_por FK
    }
    FORMATO_DESTINO {
        bigint id PK
        varchar nombre "UK con version"
        int version
        formato_archivo formato_archivo "XLSX | CSV"
        char separador_decimal
        boolean activo
    }
    CAMPO_DESTINO {
        bigint id PK
        bigint formato_destino_id FK
        varchar clave "precio_costo"
        varchar etiqueta "precio de costo"
        tipo_dato tipo_dato "TEXTO | DECIMAL | ENTERO"
        boolean obligatorio
        smallint orden
        smallint decimales
    }
    PERFIL_IMPORTACION {
        bigint id PK
        bigint proveedor_id FK
        bigint formato_destino_id FK
        varchar nombre
        int version
        estado_perfil estado "BORRADOR | ACTIVO | ARCHIVADO"
        varchar hoja "LSTPRE"
        int fila_encabezado "7"
        int fila_inicio_datos "8"
        int fila_fin_datos
        char separador_decimal_origen
        boolean titulos_como_categoria
        bigint creado_por FK
    }
    MAPEO_COLUMNA {
        bigint id PK
        bigint perfil_id FK
        varchar columna_excel "A"
        varchar encabezado_esperado "CODIGO"
        bigint campo_destino_id FK "null si se ignora"
        boolean ignorar
    }
    TRANSFORMACION {
        bigint id PK
        bigint mapeo_columna_id FK
        smallint orden
        tipo_transformacion tipo "TRIM | A_DECIMAL ..."
        jsonb parametros
    }
    REGLA_CALCULO {
        bigint id PK
        bigint perfil_id FK
        smallint orden
        bigint campo_resultado_id FK
        operacion_calculo operacion "SUMAR_PORCENTAJE ..."
        bigint campo_base_id FK
        bigint campo_secundario_id FK
        numeric valor_numerico "0.04"
        varchar valor_texto
    }
    IMPORTACION {
        bigint id PK
        bigint perfil_id FK
        bigint usuario_id FK
        varchar nombre_archivo
        char hash_archivo "SHA-256"
        varchar hoja_procesada
        estado_importacion estado "PREVIEW | CONFIRMADA | CANCELADA | FALLIDA"
        boolean estructura_coincide
        jsonb diferencias
        int filas_leidas
        int filas_validas
        int filas_ignoradas
        int filas_con_error
        timestamptz creado_en
        timestamptz confirmado_en
        timestamptz expira_en
    }
    FILA_IMPORTADA {
        bigint id PK
        bigint importacion_id FK
        int numero_fila
        estado_fila estado "VALIDA | IGNORADA | ERROR"
        motivo_ignorada motivo_ignorada
        varchar categoria
        jsonb datos_crudos
        jsonb datos_resultado
    }
    ERROR_FILA {
        bigint id PK
        bigint fila_id FK
        bigint campo_destino_id FK
        varchar columna_excel
        codigo_error codigo
        text valor_original
        varchar mensaje
    }
    EXPORTACION {
        bigint id PK
        bigint importacion_id FK
        bigint usuario_id FK
        tipo_exportacion tipo "RESULTADO | ERRORES"
        formato_archivo formato_archivo
        varchar nombre_archivo
        int cantidad_filas
        timestamptz generado_en
    }
```

---

## 3. Descripción de las tablas

### Configuración (el "contrato")

| Tabla | Para qué sirve | Reglas de integridad principales |
| --- | --- | --- |
| `usuario` | Login y autoría (quién importó qué) | `email` único; contraseña con BCrypt |
| `proveedor` | Origen de las listas | `nombre` único |
| `formato_destino` | Lo que espera el sistema destino (hoy: `plataforma_anterior` v1) | `(nombre, version)` único |
| `campo_destino` | Conceptos del dominio: código, producto, precio de costo, IVA, stock… | `(formato, clave)` y `(formato, orden)` únicos |
| `perfil_importacion` | Hoja, fila de encabezado, fila inicial y opciones de lectura | `fila_inicio_datos > fila_encabezado`; **un solo perfil `ACTIVO`** por proveedor y nombre (índice parcial) |
| `mapeo_columna` | Columna del Excel → campo destino, o *ignorar* | Mapea **o** ignora (`CHECK`); un campo destino se alimenta de una sola columna |
| `transformacion` | Transformaciones ordenadas por columna (`TRIM`, `A_DECIMAL`…) | `(mapeo, orden)` único |
| `regla_calculo` | Cálculos ordenados (conjunto cerrado de operaciones) | Cada operación exige sus operandos (`CHECK`); `DIVIDIR` no admite 0 |

### Ejecución (el historial)

| Tabla | Para qué sirve | Reglas de integridad principales |
| --- | --- | --- |
| `importacion` | Una carga de archivo: quién, cuándo, con qué perfil y resultado | `filas_leidas = válidas + ignoradas + con error`; `CONFIRMADA` exige `confirmado_en` |
| `fila_importada` | Cada fila leída con sus datos crudos y su resultado | Una fila `VALIDA` tiene resultado; una `IGNORADA` tiene motivo |
| `error_fila` | Motivo de rechazo por campo (fila 248: precio no numérico) | Se borra en cascada con la fila |
| `exportacion` | Registro de cada archivo descargado (resultado o errores) | El archivo se genera a demanda y no se guarda |

---

## 4. Decisiones de diseño

1. **Versionado de perfiles.** Si el proveedor cambia el formato, no se edita el perfil en uso: se crea la versión `n+1` y la anterior pasa a `ARCHIVADO`. Las importaciones viejas siguen apuntando a la versión con la que se procesaron, y así la trazabilidad se mantiene.
2. **Detección de cambio de estructura.** `mapeo_columna.encabezado_esperado` guarda el encabezado que tenía cada columna al crear el perfil. En cada importación se compara con el archivo y el resultado queda en `importacion.estructura_coincide` / `diferencias`.
3. **La vista previa también se persiste.** La importación nace en `PREVIEW` con todas sus filas. Confirmar solo cambia el estado; cancelar la marca `CANCELADA`. Así la vista previa y el resultado final no pueden diferir.
4. **Tiempo de guardado.** `importacion.expira_en` (90 días por defecto) responde a "por cuánto tiempo se persiste en el sistema" del MVP. Una tarea programada borra las importaciones vencidas y sus filas en cascada.
5. **Archivo repetido.** `hash_archivo` (SHA-256) permite avisar "este archivo ya se importó el 19/08".
6. **Qué no se guarda.** El Excel original no se guarda en la base (solo nombre y hash). Los archivos exportados tampoco: se regeneran desde `fila_importada`.
7. **Sin multitenancy.** No hay `tenant_id`. Es una evolución futura (ver [problema y alcance](problema-y-alcance.md#6-mvp-y-evoluciones)).

---

## 5. Ciclo de vida

```mermaid
stateDiagram-v2
    direction LR
    state "Perfil" as P {
        [*] --> BORRADOR
        BORRADOR --> ACTIVO : se prueba con un archivo real
        ACTIVO --> ARCHIVADO : el proveedor cambió el formato → versión n+1
    }
    state "Importación" as I {
        [*] --> PREVIEW : se sube el archivo
        PREVIEW --> CONFIRMADA : el usuario confirma
        PREVIEW --> CANCELADA : el usuario cancela
        PREVIEW --> FALLIDA : hoja inexistente / archivo ilegible
        CONFIRMADA --> [*] : vence expira_en
    }
```

---

## 6. Ejemplo de una fila persistida

Fila 12 de `Lista Ferreteria 19 08.xlsx` después de procesarla con el perfil de [`seed.sql`](../database/seed.sql):

```json
{
  "numero_fila": 12,
  "estado": "VALIDA",
  "categoria": "PILAS ENERGIZER ALCALINAS",
  "datos_crudos":    { "A": "EP52", "B": "ENERGIZER AA BLISTER X 1", "C": 1474.41, "D": 1784.04, "E": null },
  "datos_resultado": {
    "codigo": "EP52", "producto": "ENERGIZER AA BLISTER X 1",
    "precio_costo": 1474.41, "ingreso_bruto": 1533.39, "ganancia": 2300.08,
    "iva": 483.02, "costo_mas_iva": 2783.10, "stock": null, "precio_venta": 3618.03
  }
}
```

(El motor calcula sin redondear y redondea solo al exportar, según `campo_destino.decimales`.)
