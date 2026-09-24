# Entregas y devoluciones

[← README](../README.md)

## Entregas

| Entrega | Contenido | Fecha máxima | Estado |
| --- | --- | --- | --- |
| 1.ª — Propuesta y repositorio | Problema, alcance, tecnologías y repositorio | 30/08 | Presentada |
| 2.ª — Diseño y módulos | [Base de datos](base-de-datos.md) y [módulos](modulos.md), aprobados por el tutor y el comité | 27/09 | En revisión |
| Final | Código, base de datos, despliegue online, informe y video (en inglés) | 14/11 | Pendiente |
| Defensa oral | Presentación ante el comité | Mesa de examen | Pendiente |

## Devolución del 15/08

| Pedido del tutor | Qué cambiamos |
| --- | --- |
| Excels reales de proveedores | Se agregó `dataset-example/` |
| No construir otro ERP | Se quitaron stock, ventas y multitenancy |
| Justificar MongoDB + PostgreSQL | Se usa una sola base: PostgreSQL |
| Definir "homologar" | Relacionar columnas, normalizar valores y calcular campos |
| Probar el despliegue temprano | Prueba de despliegue antes del desarrollo |
| Evitar "sincronización bidireccional" | Se habla de exportar y reimportar |

## Devolución del 25/08

| Pedido del tutor | Qué cambiamos |
| --- | --- |
| El proceso incluye cálculos, no solo mapeo | Se agregaron reglas de cálculo por perfil |
| Contrato configurable, no adivinar el Excel | Perfil de importación: hoja, filas, columnas y cálculos |
| Vista previa antes de confirmar | Estado `PREVIEW` en la importación |
| Informar filas rechazadas | Tabla `error_fila` y exportación de errores |
| Avisar si el proveedor cambia el formato | Se comparan los encabezados con el perfil |
| Primer caso real | `Lista Ferreteria 19 08.xlsx`, hoja `LSTPRE` |
| Par original/corregido del mismo proveedor | Pendiente de conseguir |
