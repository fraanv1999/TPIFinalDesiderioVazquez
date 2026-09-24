# Consignas del Trabajo Final

[← Volver al README](../README.md)

Reglas y entregas del Trabajo Final de la Tecnicatura Universitaria en Programación a Distancia (UTN), según lo que comunicó la cátedra. Lo que no está confirmado figura como *a confirmar*.

---

## 1. Entregas

| Entrega | Contenido | Estado |
| --- | --- | --- |
| **1.ª — Definición del problema y alcance** | Problema, actores, impacto, solución, stack, MVP, plan y riesgos | Presentada y revisada (devoluciones 15/08 y 25/08) → [entrega-1.md](entregas/entrega-1.md) |
| **2.ª — Diseño y Módulos (Condición de Regular)** | Esquema de la base de datos (relacional o documental) y listado de módulos a desarrollar en el repositorio, **aprobados de forma previa y explícita por el tutor y luego por el comité de trabajo final** | En preparación → [esquema](esquema-base-de-datos.md) · [módulos](modulos.md) |
| Entrega final | Sistema funcionando + documentación | 21/11 (según el plan de trabajo) · *requisitos a confirmar* |

### Checklist de la 2.ª entrega

- [x] Esquema de base de datos con diagrama → [esquema-base-de-datos.md](esquema-base-de-datos.md)
- [x] DDL ejecutable → [`database/schema.sql`](../database/schema.sql)
- [x] Listado de módulos con prioridad y dependencias → [modulos.md](modulos.md)
- [x] Respuesta a cada punto de las devoluciones → [devoluciones.md](devoluciones.md)
- [ ] Aprobación explícita del tutor
- [ ] Aprobación del comité de trabajo final

---

## 2. Pautas de documentación

Indicaciones de la cátedra:

1. Usar **Markdown** (`README.md` y otros `.md`) en lugar de PDF: facilita la lectura, la navegación, el seguimiento de cambios y las devoluciones.
2. Estructura simple: un `README.md` principal y, si hace falta, otros `.md` dentro del repositorio.
3. Los diagramas, modelos, capturas e imágenes se **guardan dentro del repositorio** y se enlazan desde el Markdown, para recorrer todo desde GitHub sin descargar nada.
4. El repositorio va quedando como la **documentación completa y ordenada** del proyecto, además del código.

Cómo se aplican en este repo:

- Los diagramas están en **Mermaid**, dentro de los mismos `.md` (GitHub los muestra directamente).
- Si se agregan capturas, van en `docs/img/` y se enlazan con rutas relativas: `![Vista previa](img/vista-previa.png)`.
- Cada documento tiene un enlace para volver al README.
- Las entregas ya presentadas se guardan en `docs/entregas/` y no se editan.

---

## 3. Criterios para validar el problema

Del material de la cátedra (*Identificación de una problemática y propuesta de solución*):

| Criterio | Cómo lo cumple el proyecto |
| --- | --- |
| Contexto claro | Pequeños negocios que reciben listas de varios proveedores y las cargan en un sistema con formato fijo |
| Afecta a personas concretas | Dueño o empleado no técnico |
| Impacto medible | 2–3 h semanales + errores de transcripción |
| Admite una solución tecnológica | Transformación repetible a partir de un contrato |
| ¿Ocurre hoy? | Sí: planillas corregidas reales en `dataset-previous-platform` |
| ¿Existe una solución parcial? | Excel con fórmulas a mano: no escala y no reduce la carga |
| ¿Hay algo similar en el mercado? | Los ERP (Xubio, Colppy, Tango, Odoo) no homologan listas heterogéneas de proveedores |
| Valor agregado | Reduce tiempo y errores; el perfil hace repetible lo que hoy es artesanal |

## 4. Criterios del tutor

Surgen de las devoluciones y aplican a todo el proyecto:

- Cada componente responde a una necesidad concreta. Si al quitarlo no se pierde nada del problema, queda fuera del MVP.
- Preferir una solución **acotada, bien fundamentada y técnicamente sólida** antes que muchas funcionalidades.
- El sistema procesa **archivos que cumplen un contrato**, no "cualquier Excel".
- El proceso tiene que ser **configurable, repetible, validado y trazable**.
- El deploy se prueba temprano.
