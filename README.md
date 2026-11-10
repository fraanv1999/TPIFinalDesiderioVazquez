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
| Backend | Java 17 + Spring Boot 4 (Web, JDBC, Security) + Apache POI (Render, Docker) |
| Base de datos | PostgreSQL (Render) |
| Pruebas | JUnit 5 + AssertJ + MockMvc, con PostgreSQL embebido |

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
├── docs/              documentación
├── docker-compose.yml PostgreSQL + backend en local
└── render.yaml        despliegue en Render
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

La API usa HTTP Basic (header `Authorization` en cada pedido). Usuarios de prueba del `seed.sql`:

| Email | Contraseña | Rol |
| --- | --- | --- |
| `admin@tpi.local` | `admin123` | `ADMIN`: además administra proveedores |
| `operador@tpi.local` | `operador123` | `OPERADOR`: importa y exporta |

| Método | Ruta | Qué hace |
| --- | --- | --- |
| `GET` | `/api/salud` | Estado del servicio (sin login) |
| `GET` | `/api/auth/yo` | Usuario autenticado; el frontend lo usa para validar el login |
| `GET` | `/api/proveedores` | Proveedores con su cantidad de perfiles |
| `POST` / `PUT` | `/api/proveedores`, `/api/proveedores/{id}` | Alta y edición (solo `ADMIN`): `{ "nombre": "...", "activo": true }` |
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

### Pruebas

```bash
cd backend
./mvnw test
```

Incluyen pruebas unitarias del motor, pruebas de referencia contra los Excel de `dataset-example/` y pruebas de la API. La comparación con la corrección manual de Paint Roller usa archivos del cliente que no están en el repo: se corre definiendo `DATASET_EXTERNO` con la carpeta que los contiene; si no está, se saltea.

### Frontend

Con el backend corriendo:

```bash
cd frontend
npm install
npm run dev
```

Abrí `http://localhost:5173` e ingresá con uno de los usuarios de prueba. Todas las pantallas usan la API. Para otra URL del backend, copiá `.env.example` a `.env` y cambiá `VITE_API_URL`.

### Con Docker

Levanta PostgreSQL y el backend juntos, sin instalar Java ni PostgreSQL:

```bash
docker compose up --build   # API en http://localhost:8080
```

El frontend se corre igual que arriba.

## Despliegue en Render

`render.yaml` define la base PostgreSQL, la API (imagen de `backend/Dockerfile`) y el frontend como static site.

1. En Render: **New → Blueprint** y elegir este repositorio.
2. Al terminar, revisar las URLs que asignó Render. Si no son `tpi-final-api.onrender.com` y `tpi-final-web.onrender.com`, actualizar `CORS_ORIGENES` (API) y `VITE_API_URL` (frontend) y volver a desplegar.
3. La base se crea vacía: en el primer arranque la API ejecuta `schema.sql` y `seed.sql`.
