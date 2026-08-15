# TPIFinalDesiderioVazquez

# Trabajo Final — Tecnicatura Universitaria en Programación a Distancia (UTN)
## Definición del Problema, Alcance y Plan de Trabajo del Proyecto

---

## 1. Definición del Problema

Los dueños de pequeños negocios y sus trabajadores no especializados en sistemas —quienes gestionan el inventario, las ventas y el catálogo de productos— reciben planillas Excel de múltiples proveedores. Cada proveedor maneja estructuras de datos, nomenclaturas de columnas y formatos de atributos distintos entre sí, lo que obliga a adaptar y consolidar esa información de forma manual y artesanal, planilla por planilla.

### 1.1 Impacto Medible

* **Costo de tiempo:** Entre **2 y 3 horas semanales** dedicadas exclusivamente a arreglar y consolidar planillas de proveedores.
* **Costo cognitivo:** La tarea es percibida como una carga mental recurrente y desgastante — la sensación de *“otra vez con los excels que tengo que arreglar”* — que consume energía y atención que podrían destinarse a decisiones de negocio. Este es, de hecho, el costo que el equipo considera más relevante de resolver, por encima incluso del tiempo insumido.
* **Riesgo operativo:** Procesos propensos a errores (desalineación de precios, stock y códigos de producto al traspasar información) e ineficiencia en la toma de decisiones por falta de centralización de datos en tiempo real.

### 1.2 Actores

* **Actor principal:** Dueño del negocio o trabajador operativo no especializado en sistemas, quien sufre directamente la carga de tiempo y el desgaste mental de la tarea.
* **Actor secundario:** Los proveedores, que generan los archivos en sus propios formatos y no necesitan modificar su forma de trabajo actual — la solución se adapta a ellos, no al revés.

### 1.3 Validación del Problema

El problema ocurre hoy, de forma recurrente y semanal. Es reconocido explícitamente por los afectados, quienes lo expresan como fastidio o rechazo hacia la tarea. La única solución parcial existente es el trabajo manual dentro de Excel, que no escala con la cantidad de proveedores y no reduce ni el tiempo ni la carga mental involucrados.

---

## 2. Solución Propuesta

Desarrollar una plataforma de software centralizada (**SaaS**) que procese y homologue automáticamente las planillas Excel heterogéneas de los distintos proveedores hacia un modelo de datos único y estandarizado, eliminando la necesidad de que el dueño o trabajador del negocio adapte manualmente cada planilla.

La solución permite un flujo bidireccional entre la plataforma y archivos externos, garantizando libertad operativa para trabajar dentro del sistema o mediante hojas de cálculo.

---

## 3. Tecnologías y Justificación del Stack

* **Frontend — HTML, CSS, TypeScript, React, Tailwind:** Uno de los dos integrantes del equipo tiene experiencia consolidada en este stack. El segundo integrante parte con conocimiento menor en React y Tailwind, pero la cursada incluye profundización en ambas tecnologías durante el desarrollo del proyecto, y el cronograma (14 semanas) admite esa curva de aprendizaje sin comprometer la entrega final.
* **Backend — Java / Spring Boot:** Se eligió por su tipado estricto y alta escalabilidad, y porque es el lenguaje donde ambos integrantes se sienten más confiados para manejar datos de forma segura — la justificación prioriza el dominio real del equipo por sobre la popularidad de otras alternativas.
* **Bases de Datos — MongoDB Atlas y Supabase (PostgreSQL):** Se separan los datos en dos motores porque tienen naturaleza distinta. Los datos de productos, una vez normalizados, siguen siendo heterogéneos y varían según el mapeo configurado por proveedor, lo que encaja con un modelo de documentos como MongoDB. Las transacciones (ventas, movimientos de stock), en cambio, requieren integridad referencial y transaccional (ACID), por lo que se gestionan en una base relacional (PostgreSQL vía Supabase).
* **Despliegue — Node.js, Vite, Netlify, Railway:** Railway es la única tecnología del stack en la que ninguno de los dos integrantes tiene experiencia previa (uno no la conoce y el otro nunca hizo un deployment ni trabajó con PostgreSQL). Se asume como un riesgo identificado y gestionado (ver sección 8), no como un punto ciego: el cronograma reserva las semanas 12 y 13 específicamente para deployment, con margen para esa curva de aprendizaje.
* **Escala esperada:** El sistema debe soportar, en esta primera fase, al menos 50 tenants con hasta 5 usuarios cada uno (~250 usuarios totales). Es un volumen que no exige arquitectura de alta concurrencia ni microservicios — evitar esa complejidad adicional responde directamente al criterio de no incurrir en sobre-ingeniería para un problema de esta escala.

---

## 4. Alcance del Proyecto (Scope)

### Módulo A: Motor de Estandarización y Mapeo de Datos
* **Carga de Archivos Heterogéneos:** Interfaz para subir planillas Excel de múltiples proveedores en sus formatos nativos.
* **Motor de Mapeo Equivalente:** Configuración de reglas de mapeo de columnas del proveedor contra la estructura de datos unificada del sistema.
* **Exportación de Plantilla Estandarizada:** Generación y descarga de archivos en el formato único homologado.
* **Sincronización Bidireccional:** Re-importación de planillas editadas externamente para actualizar la base de datos central.

### Módulo B: Gestión Operativa y Comercial
* **Control de Stock e Inventario:** Monitoreo centralizado de existencias, actualizaciones por importación y trazabilidad de productos.
* **Módulo de Ventas:** Registro y seguimiento de operaciones comerciales integradas al inventario.
* **Auditoría e Historial de Importaciones:** Registro de planillas procesadas, fecha de subida, proveedor asociado, estado del mapeo y log de errores.

### Módulo C: Arquitectura, Seguridad y Control de Accesos
* **Arquitectura Multitenancy:** Aislamiento lógico de datos por cada cliente o empresa usuaria.
* **Superadministrador:** Gestión global de la plataforma, alta de organizaciones (tenants), métricas generales y soporte técnico.
* **Administrador de Tenant:** Configuración de mapeos, gestión de usuarios de la organización, asignación de permisos.
* **Usuarios Operativos:** Acceso restringido a módulos específicos según la asignación de su Administrador.

### Matriz de Alcance

| Componente | Incluido en el Alcance (In Scope) |
| :--- | :--- |
| **Integración de Datos** | Mapeo flexible de columnas Excel, homologación de formatos y sincronización bidireccional (Import/Export). |
| **Operativa** | Gestión de inventario, ventas y trazabilidad/historial de planillas procesadas. |
| **Sostenibilidad SaaS** | Arquitectura multi-inquilino (multitenancy) con jerarquía de usuarios (Superadmin, Admin Tenant, Usuarios). |
| **Flexibilidad de Uso** | Operatividad nativa en la app web o mediante trabajo local en hojas de cálculo estandarizadas. |

---

## 5. MVP — Alcance Mínimo Viable

### Incluido en el MVP:
* **Módulo A completo:** Carga de Excel, motor de mapeo configurable, exportación estandarizada, sincronización bidireccional.
* **Módulo B básico:** Control de stock y registro de ventas.
* **Módulo C:** Multitenancy funcional con los tres roles (Superadmin, Admin Tenant, Usuario Operativo).

### Explícitamente fuera de esta versión (Out of Scope):
* Reportes avanzados y analítica de ventas.
* Integraciones con e-commerce o facturación electrónica (ARCA).
* Aplicación móvil nativa.
* Auditoría avanzada con logs exportables (se mantiene un historial básico, no un módulo completo).

---

## 6. Análisis de Competencia y Diferenciación

El mercado argentino de gestión para pymes está dominado por ERPs contables y de stock como Xubio, Colppy, Dux Software, Tango y Odoo. Estas plataformas resuelven bien la facturación, la contabilidad y el control de stock propio del negocio, pero ninguna de las relevadas ataca específicamente el problema identificado: homologar y sincronizar planillas heterogéneas que llegan de múltiples proveedores externos.

| Característica | Competidores indirectos (Xubio, Colppy, Dux, Tango, Odoo) | Nuestra propuesta |
| :--- | :--- | :--- |
| **Foco principal** | Facturación, contabilidad y stock propio del negocio | Homologación de datos entre proveedores + operación de stock/ventas |
| **Formatos heterogéneos de proveedores** | No resuelto de forma nativa | Motor de mapeo configurable por proveedor |
| **Trabajo fuera de la plataforma** | Limitado o inexistente | Sincronización bidireccional con Excel |
| **Multi-empresa (varios negocios en una cuenta)** | Generalmente no | Multitenancy nativo |

> **Riesgo competitivo a monitorear:** Si un ERP establecido (por ejemplo Xubio o Colppy) agrega un “importador inteligente” de Excel, nuestra ventaja se reduciría a la calidad del motor de mapeo y a la multitenancy orientada a quienes gestionan varios negocios a la vez (por ejemplo, consultoras o distribuidores). Vale la pena revisar este punto en revisiones futuras del proyecto.

---

## 7. Plan de Trabajo (13/08 al 21/11)

| Semanas | Foco | Entregable |
| :---: | :--- | :--- |
| **1–2** | Arquitectura, modelo de datos unificado, setup de repositorios y CI | Diagrama de arquitectura + repositorio inicial |
| **3–6** | Módulo A: carga, mapeo, exportación y sincronización bidireccional | Motor de mapeo funcional con al menos 2 formatos de proveedor de prueba |
| **7–9** | Módulo B: stock y ventas | CRUD de inventario/ventas integrado al modelo unificado |
| **10–11** | Módulo C: multitenancy y roles | Aislamiento de datos por tenant validado con pruebas |
| **12–13** | Testing, deployment (Railway/Netlify) y documentación | Demo desplegada + manual técnico |
| **14** | Buffer y entrega final | Entrega 21/11 |

---

## 8. Riesgos y Mitigaciones

1. **Curva de aprendizaje de un integrante en React, Tailwind, PostgreSQL y Railway.**
   * *Mitigación:* Asignar esas tareas desde el inicio del cronograma, con acompañamiento del integrante más experimentado; el cronograma tiene margen suficiente para absorberla.
2. **La heterogeneidad real de los Excels de proveedores puede ser mayor a la estimada.**
   * *Mitigación:* El motor de mapeo se diseña configurable desde el inicio, sin reglas de mapeo hardcodeadas.
3. **Aislamiento de datos en multitenancy mal implementado.**
   * *Mitigación:* Pruebas de aislamiento entre tenants desde la semana 10, no postergadas al final del proyecto.

---

## 9. Viabilidad del Proyecto

* **Viabilidad técnica:** Alta. El stack es conocido por al menos uno de los dos integrantes en cada capa; el único punto débil (Railway/PostgreSQL) está identificado y gestionado con tiempo de aprendizaje reservado en el cronograma.
* **Viabilidad operativa:** Condicionada. Pensado para pequeños negocios que hoy administran datos en Excel de forma dispersa; la adopción depende de que el mapeo inicial no requiera conocimientos técnicos por parte del cliente final.
* **Viabilidad temporal:** Viable. 14 semanas para tres módulos con dos personas es un plazo ajustado pero razonable, siempre que se respete el alcance definido como MVP.

---

## 10. Criterios de Éxito del MVP

* El motor de mapeo procesa correctamente al menos dos formatos reales de proveedores distintos sin intervención manual adicional.
* El tiempo de consolidación de una planilla baja de las 2–3 horas semanales actuales a minutos.
* El aislamiento de datos entre tenants pasa pruebas básicas de seguridad (un tenant no puede acceder a datos de otro).
* Los tres roles de usuario operan con los permisos correctos y esperados.

---

## 11. Repositorio

* **GitHub:** [fraanv1999/TPIFinalDesiderioVazquez](https://github.com/fraanv1999/TPIFinalDesiderioVazquez)
