# Devoluciones del tutor y respuestas

[← Volver al README](../README.md)

Registro de cada devolución y de dónde se responde cada punto. Se actualiza con cada instancia.

---

## Devolución 1 — 15/08

Sobre la propuesta original (ERP con stock, ventas, multitenancy, MongoDB + PostgreSQL).

| # | Pedido del tutor | Respuesta | Dónde |
| --- | --- | --- | --- |
| 1 | 2 o 3 Excels reales de proveedores distintos | Se agregaron 3 de proveedores y 3 corregidos de la plataforma anterior | [`dataset-example/`](../dataset-example) · [análisis](analisis-dataset.md) |
| 2 | Qué transformación se hace hoy a mano | Transcripción + porcentajes por proveedor + fórmulas | [problema y alcance §1](problema-y-alcance.md#1-problema) |
| 3 | Definición precisa de "homologar" | Estructura + mapeo + normalización + cálculo; sin matching | [problema y alcance §3](problema-y-alcance.md#3-qué-significa-homologar-en-este-proyecto) |
| 4 | Modelo común de destino | Formato `plataforma_anterior` v1 (9 campos) | [esquema](esquema-base-de-datos.md) · [`seed.sql`](../database/seed.sql) |
| 5 | Qué hace el usuario y qué automatiza el sistema | Primera vez: asistente de perfil. Después: se reutiliza el perfil | [contrato](contrato-de-importacion.md) |
| 6 | MVP: separar lo indispensable de lo evolutivo | Tabla P0 / P1 / Futuro | [módulos](modulos.md) · [alcance §6](problema-y-alcance.md#6-mvp-y-evoluciones) |
| 7 | ¿Hace falta MongoDB + PostgreSQL? | **Una sola base: PostgreSQL** (+ JSONB) | [esquema §1](esquema-base-de-datos.md#1-por-qué-postgresql-y-no-mongodb) |
| 8 | ¿Hace falta multitenancy desde el inicio? | No. Queda como evolución | [alcance §6](problema-y-alcance.md#6-mvp-y-evoluciones) |
| 9 | Nivel real del equipo por tecnología | Tabla por integrante | [alcance §4](problema-y-alcance.md#nivel-del-equipo) |
| — | No dejar el deploy para el final | M0 en las semanas 1–2 | [módulos §5](modulos.md#5-plan-de-trabajo-por-módulo-3108--2111) |
| — | "Sincronización bidireccional" → exportación y reimportación | Se cambió el término y el alcance | [alcance §6](problema-y-alcance.md#6-mvp-y-evoluciones) |
| — | No construir otro ERP | El producto complementa al ERP | [alcance §2](problema-y-alcance.md#2-solución) |

## Devolución 2 — 25/08

Después de revisar el dataset. Concepto central: **contrato configurable**.

| # | Pedido del tutor | Respuesta | Dónde |
| --- | --- | --- | --- |
| 1 | Elegir un archivo real como primer caso | `Lista Ferreteria 19 08.xlsx` | [contrato §1](contrato-de-importacion.md#1-qué-define-un-perfil) |
| 2 | Definir la hoja | `LSTPRE` | ídem |
| 3 | Fila de encabezados y fila inicial | Encabezado en la 7, datos desde la 8 (primer producto en la 12) | ídem |
| 4 | Columnas usadas e ignoradas | A, B, C usadas · D, E ignoradas | ídem |
| 5 | Modelo destino | `plataforma_anterior` v1 | [`seed.sql`](../database/seed.sql) |
| 6 | Obligatorios y tipos | `campo_destino.obligatorio` / `tipo_dato` | [esquema](esquema-base-de-datos.md) |
| 7 | Transformaciones | Conjunto cerrado: TRIM, A_DECIMAL… | [contrato §2](contrato-de-importacion.md#2-operaciones-soportadas-p0) |
| 8 | Cálculos | Conjunto cerrado + `SUMAR_CAMPO` (lo pide el dataset) | ídem |
| 9 | Diseñar la vista previa | Maqueta con totales y estados por fila | [contrato §4](contrato-de-importacion.md#4-vista-previa-diseño) |
| 10 | Cómo se informan las filas rechazadas | `error_fila` + exportación de errores | [contrato §5](contrato-de-importacion.md#5-reporte-de-filas-rechazadas) |
| 11 | Primera salida compatible con el destino | M8. **Pendiente de implementar** | [módulos](modulos.md#m8--exportación-y-reporte-de-errores--p0) |
| 12 | Par original / corregido del mismo proveedor | **Pendiente de conseguir**. El test ya está diseñado (M9) | [contrato §6](contrato-de-importacion.md#6-caso-de-aceptación-antes--después) |
| — | Detectar cambio de formato del proveedor | `encabezado_esperado` + `estructura_coincide` | [esquema §4](esquema-base-de-datos.md#4-decisiones-de-diseño) |
| — | Elegir la base según el dominio y lo que el equipo conoce | Se revisó el dominio: es relacional | [esquema §1](esquema-base-de-datos.md#1-por-qué-postgresql-y-no-mongodb) |
