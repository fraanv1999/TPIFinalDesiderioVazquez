# TPI Final - Desiderio, Vazquez
## Tecnicatura Universitaria en Programación a Distancia (UTN)

---

## 1. Problema

Los dueños de pequeños negocios reciben Excels de proveedores con estructuras, nombres de columna y formatos distintos entre sí, y hoy los transcriben manualmente a otro sistema que solo acepta un formato fijo. Eso ocupa entre 2 y 3 horas semanales y es percibido como una carga mental recurrente, además de ser una fuente de errores (precios, stock, códigos mal traspasados).

**Transformación manual actual:** se toma el Excel del proveedor —que ya trae cálculos propios (precios, descuentos, totales)— y se transcribe a mano, columna por columna, en otro sistema que solo acepta un formato de Excel específico y rígido. Ahí está el cuello de botella: nuestra propuesta permite cargar el Excel del proveedor tal cual llega, sin importar su estructura, y es el sistema el que lo interpreta.

---

## 2. Solución: módulo de homologación (no otro ERP)

En lugar de una plataforma de gestión tipo ERP, el producto se replantea como un **módulo/servicio especializado en importación y homologación de datos**, que puede ser consumido por otros sistemas en vez de competir con ellos:

```
Sistema externo → Excel → Motor de homologación → datos normalizados → sistema destino
```

Dos mecanismos de ingesta:

* **Opción 1 — Formato conocido:** el sistema publica una plantilla/spec con las columnas requeridas. Cualquier sistema externo que pueda exportar Excel/CSV en ese formato lo importa directo, sin mapeo.
* **Opción 2 — Formato desconocido:** el usuario carga un Excel cualquiera. El sistema detecta los encabezados y, mediante una interfaz guiada, el usuario indica a qué campo corresponde cada columna. Ese mapeo se guarda como perfil de ese proveedor; la próxima carga con la misma estructura se procesa automáticamente.

---

## 3. Homologación de datos

Homologar = **relacionar columnas equivalentes entre el Excel del proveedor y el modelo de datos destino, y normalizar el formato de esos valores** (separador decimal, fechas, etc.). No incluye, en esta versión:

* Determinar si dos registros de proveedores distintos representan el mismo producto (matching/deduplicación).
* Conversión de unidades o interpretación de categorías distintas entre proveedores.

Esas dos cosas quedan explícitamente como evolución futura, no como parte del MVP.

---

## 4. Modelo de datos destino

El modelo no lo define el equipo de forma cerrada: lo define el cliente (nombres de campo que necesita, ej. `nombre_producto`, `precio_unitario`, `codigo_proveedor`). Ese modelo es el destino contra el que se mapea cada Excel.

---

## 5. Mapeo: qué hace el usuario y qué automatiza el sistema

1. Primera carga de un proveedor nuevo → el usuario mapea manualmente cada columna contra el modelo destino (interfaz guiada).
2. El sistema guarda ese mapeo como **perfil del proveedor**.
3. Próximas cargas del mismo proveedor → mapeo automático usando el perfil guardado. El usuario puede igual re-mapear a mano si el proveedor cambió su estructura.

---

## 6. Stack

* **Backend:** Java / Spring Boot.
* **Frontend:** React + Tailwind.
* **Persistencia — MongoDB Atlas (motor único):** se mantiene por dos razones concretas, no por el dato ya homologado en sí (que una vez validado tiene forma consistente):
  1. **Perfiles de mapeo por proveedor**, de forma variable — cada proveedor tiene su propio set de columnas/reglas.
  3. **Campos estrictos + un campo genuinamente variable, en el mismo documento.** El schema de `productos_normalizados` (ver Anexo) usa `$jsonSchema` para exigir tipado obligatorio en los campos de negocio (`sku_interno`, `precio_costo`, `stock`, etc.), pero mantiene `origen.datos_crudos_excel` como un objeto sin sub-schema fijo, porque ahí vive la fila cruda tal como la trajo cada proveedor — eso sí varía por proveedor por definición. Además, `proveedor_id` y `lote_id` quedan como referencia (compartidos entre productos), mientras que `origen`, `comercial` y `auditoria` van embebidos (específicos del producto), lo que permite mostrar el producto normalizado completo — el último paso del MVP — con una sola lectura, sin joins.

  *(Honestidad: Postgres con una columna JSONB también podría resolver el punto 3 — la ventaja de Mongo es que es el modelo natural, no un workaround.)*

  Se descarta PostgreSQL/Supabase: no hay módulo transaccional que requiera ACID.
* **Deploy:** Netlify + Railway.

> **Pendiente para el profesor:** nivel real de cada integrante en React/Tailwind, Railway y MongoDB (a completar con algo concreto: nunca lo usó / tutorial / proyecto chico / experiencia sólida).

---

## 7. MVP vs. evoluciones futuras

**MVP (indispensable):**
Cargar Excel → identificar columnas → mapear (manual/automático por perfil) → validar → guardar → mostrar datos normalizados. Login básico de usuario + historial de qué se importó, cuándo, quién y por cuánto tiempo se persiste en el sistema.

**Evoluciones futuras (fuera del MVP):**
* Matching de productos entre proveedores, conversión de unidades, categorías.
* Multitenancy (necesaria solo si el producto se vende a múltiples negocios a la vez; no para resolver el problema de un negocio hoy).
* Stock, ventas, reportes, integraciones (e-commerce, ARCA), app móvil.
* Base relacional, si en el futuro se agregan módulos transaccionales.

---

## 8. Plan de trabajo (31/08 – 21/11)

| Semanas | Foco |
| :---: | :--- |
| 1–2 | Arquitectura + prueba mínima de deployment de punta a punta |
| 3–6 | Motor de mapeo (Opción 2: carga, detección de columnas, mapeo guiado, perfiles) |
| 7–9 | Validación, guardado del resultado homologado, visualización |
| 10 | Opción 1: import directo por plantilla/spec conocida |
| 11 | Login de usuarios + historial de importaciones |
| 12–13 | Testing, deployment final, documentación |
| 14 | Buffer y entrega (21/11) |

---

## 9. Riesgos

1. **Curva de aprendizaje (React/Tailwind, Railway):** mitigado con la prueba de deployment temprana y acompañamiento del integrante más experimentado.
2. **Heterogeneidad real de los Excels mayor a la estimada:** motor de mapeo configurable desde el inicio, sin reglas hardcodeadas.
3. **Modelo de datos definido por el cliente queda incompleto:** permitir ajustar campos sin intervención técnica; validar con casos reales de al menos 2 proveedores.

---

## 10. Repositorio

[fraanv1999/TPIFinalDesiderioVazquez](https://github.com/fraanv1999/TPIFinalDesiderioVazquez)

---

## Anexo: schema de validación — `productos_normalizados`

```javascript
db.createCollection("productos_normalizados", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["sku_interno", "descripcion", "categoria", "origen", "comercial", "stock", "activo", "auditoria"],
      properties: {
        _id: { bsonType: "objectId" },
        sku_interno: { bsonType: "string", description: "SKU interno único del producto y es obligatorio." },
        descripcion: { bsonType: "string", description: "Descripción del producto y es obligatorio." },
        categoria: { bsonType: "string", description: "Categoría a la que pertenece el producto y es obligatorio." },
        origen: {
          bsonType: "object",
          required: ["proveedor_id", "lote_id", "sku_proveedor", "datos_crudos_excel"],
          description: "Datos de origen del producto y es obligatorio.",
          properties: {
            proveedor_id: { bsonType: "objectId", description: "Referencia al proveedor del producto y es obligatorio." },
            lote_id: { bsonType: "objectId", description: "Referencia al lote de ingesta del producto y es obligatorio." },
            sku_proveedor: { bsonType: "string", description: "SKU original del proveedor y es obligatorio." },
            datos_crudos_excel: { bsonType: "object", description: "Datos crudos extraídos del archivo Excel y es obligatorio." }
          }
        },
        comercial: {
          bsonType: "object",
          required: ["precio_costo", "moneda", "margen_aplicado", "precio_venta"],
          description: "Datos comerciales del producto y es obligatorio.",
          properties: {
            precio_costo: { bsonType: "double", description: "Precio de costo del producto y es obligatorio." },
            moneda: { bsonType: "string", description: "Moneda del precio y es obligatorio." },
            margen_aplicado: { bsonType: "double", description: "Margen de ganancia aplicado sobre el costo y es obligatorio." },
            precio_venta: { bsonType: "double", description: "Precio de venta calculado y es obligatorio." }
          }
        },
        stock: { bsonType: "int", description: "Cantidad de unidades en stock y es obligatorio." },
        activo: { bsonType: "bool", description: "Indica si el producto está activo y es obligatorio." },
        auditoria: {
          bsonType: "object",
          required: ["ultima_actualizacion"],
          description: "Datos de auditoría del producto y es obligatorio.",
          properties: {
            ultima_actualizacion: { bsonType: "date", description: "Fecha y hora de la última actualización del producto y es obligatorio." }
          }
        }
      }
    }
  }
})
```