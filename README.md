# TPI Final — Vazquez

Tecnicatura Universitaria en Programación a Distancia (UTN)

## Integrantes

- Franco Vazquez

## Problema

Los pequeños negocios reciben listas de precios en Excel de varios proveedores. Cada lista tiene un formato distinto, y hoy se pasan a mano al formato fijo que acepta su sistema: eso lleva entre 2 y 3 horas por semana y genera errores en precios y códigos.

## Solución

Un sistema que importa la lista del proveedor, la transforma según un **perfil** configurado una vez por cada hoja de cada proveedor y exporta un archivo listo para el sistema destino.

El perfil indica qué hoja leer, en qué fila están los encabezados, qué representa cada columna y qué cálculos aplicar (IIBB, ganancia, IVA). Si el archivo ya no cumple el perfil, se informa qué cambió. Las filas con errores se informan, no se descartan en silencio.

```mermaid
flowchart LR
    A[Excel del proveedor] --> B[Perfil] --> C[Control de estructura] --> D[Clasificación y cálculos] --> E[Vista previa]
    E --> F[Archivo para el sistema destino]
    E --> G[Reporte de errores]
```

## Demo del Frontend

![Demo del prototipo](frontend/gif-page.gif)

## Tecnologías

| Capa | Tecnología |
| --- | --- |
| Frontend | React + Tailwind (Render, static site) |
| Backend | Java 17 + Spring Boot 4 + Apache POI (Render, Docker) |
| Base de datos | PostgreSQL (Render) |

## Documentación

- [Base de datos](docs/base-de-datos.md)
- [Módulos](docs/modulos.md)
- [Entregas y devoluciones](docs/entregas.md)

## Estructura

```
├── backend/           API REST y motor de transformación (Spring Boot)
├── database/          scripts SQL (schema.sql, seed.sql)
├── dataset-example/   Excels reales de proveedores y de la plataforma anterior
├── frontend/          frontend React + Tailwind (Vite), conectado a la API
└── docs/              documentación
```

## Instalación

### Base de datos

Crear una base vacía en PostgreSQL:

```bash
createdb tpi_final
```

Al arrancar, el backend ejecuta `database/schema.sql` y `seed.sql` si la base no tiene tablas. La conexión se configura con `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` y `DB_PASSWORD` (por defecto `localhost:5432/tpi_final`, usuario y clave `postgres`).

### Backend (requiere Java 17)

```bash
cd backend
./mvnw test             # pruebas (levantan un PostgreSQL embebido, no hace falta tenerlo instalado)
./mvnw spring-boot:run  # API en http://localhost:8080
```

| Método | Ruta | Qué hace |
| --- | --- | --- |
| `GET` | `/api/proveedores` | Proveedores con su cantidad de perfiles |
| `POST` / `PUT` | `/api/proveedores`, `/api/proveedores/{id}` | Alta y edición: `{ "nombre": "...", "activo": true }` |
| `DELETE` | `/api/proveedores/{id}` | Baja de un proveedor sin perfiles (si tiene, se desactiva) |
| `GET` | `/api/perfiles` | Lista los perfiles |
| `GET` | `/api/perfiles/{id}` | Perfil completo: columnas, transformaciones y reglas |
| `POST` | `/api/importaciones/preview` | Vista previa: procesa y guarda la importación. Multipart con `perfil` (id, por ejemplo `1` = Ferretería LSTPRE) y `archivo` |
| `GET` | `/api/importaciones` | Historial, las más nuevas primero |
| `GET` | `/api/importaciones/{id}` | Importación completa: diferencias, resumen y filas |
| `POST` | `/api/importaciones/{id}/confirmar` | Confirma una vista previa (409 si no está en `PREVIEW` o no tiene filas válidas) |
| `POST` | `/api/importaciones/{id}/cancelar` | Descarta una vista previa |
| `GET` | `/api/importaciones/{id}/exportar` | XLSX para Organizalo: filas válidas, columnas del formato destino, precios con 2 decimales. Solo de una importación confirmada |
| `GET` | `/api/importaciones/{id}/errores` | XLSX con las filas rechazadas (motivo y datos originales) y los avisos: diferencias de estructura y códigos repetidos |

Los perfiles se leen de la base (los cinco de `seed.sql`).

### Frontend

Con el backend corriendo:

```bash
cd frontend
npm install
npm run dev
```

Abrí `http://localhost:5173`. Las pantallas de **Perfiles** y **Asistente de importación** usan la API (subís un Excel real y ves la vista previa). Proveedores y el panel todavía usan datos simulados. Para otra URL del backend, copiá `.env.example` a `.env` y cambiá `VITE_API_URL`.
