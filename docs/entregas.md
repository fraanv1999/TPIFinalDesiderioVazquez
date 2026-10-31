# Entregas y devoluciones

[← README](../README.md)

## Entregas

| Entrega | Contenido | Fecha máxima | Estado |
| --- | --- | --- | --- |
| 1.ª — Propuesta y repositorio | Problema, alcance, tecnologías y repositorio | 30/08 | Presentada |
| 2.ª — Diseño y módulos | [Base de datos](base-de-datos.md) y [módulos](modulos.md), aprobados por el tutor y el comité | 27/09 | Conforme con correcciones (29/09) |
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
| Par original/corregido del mismo proveedor | Conseguido: Paint Roller del 02/10 (ver devolución del 29/09) |

## Devolución del 29/09

Modelo y arquitectura: conforme con correcciones. Pendiente principal: un motor ejecutable con un Excel real.

| Pedido del tutor | Qué cambiamos |
| --- | --- |
| Definir cómo se decide VALIDA, IGNORADA y ERROR | [Regla de clasificación](modulos.md#clasificación-de-filas): campo clave por perfil, filas vacías y títulos ignorados, el resto se valida |
| Un perfil representa una hoja y un contrato | Documentado. Un proveedor puede tener varios perfiles |
| No usar los porcentajes de Kallay para Ferretería | Cada proveedor con sus reglas. Ferretería y Paint Roller con regla provisoria `precio = costo` hasta confirmar |
| Par original/corregido como prueba de referencia | Paint Roller 02/10. La corrección manual se reproduce con `RELLENAR_ABAJO` y `CONCATENAR` |
| Stock: una única regla | Opcional en todos lados. Si viene, tiene que ser entero |
| Proveedor: mismos datos en frontend y base | Solo nombre y estado. Se quitaron CUIT y contacto de la demo |
| Una sola convención de porcentajes | Fracción en todas las capas: `0.04` = 4 % |
| Hojas en dólares | Fuente no soportada. Un cambio a dólares se rechaza por encabezado modificado |
| Informar qué no coincide en la estructura | Tabla `diferencia_estructura` con tipo, columna, esperado y encontrado |
| `expira_en` no borra nada | Documentado como fecha desde la que se puede purgar |
| Construir un corte vertical en backend | Spring Boot + Apache POI: subir LSTPRE → vista previa con válidas, ignoradas y errores |

Al revisar más listas reales de la ferretería (agosto a octubre) aparecieron casos nuevos que se incorporaron al modelo:

- LSTPRE trae los encabezados en la fila 7 o en la 8 según la versión: `margen_encabezado`.
- Paint Roller pone el nombre del producto una vez por grupo, en celdas combinadas: `RELLENAR_ABAJO`.
- Fenix arrastra fórmulas que dan 0 en filas sin producto: un 0 cuenta como vacío al clasificar.
- El sistema destino real es el importador de productos de Organizalo: nuevo formato destino `organizalo`.
