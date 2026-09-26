import type { Proveedor, PerfilImportacion, ImportacionHistorial, FilaProcesada } from '../types';

export const PROVEEDORES: Proveedor[] = [
  {
    id: 'prov-1',
    nombre: 'Cerrajera Kallay S.A.',
    cuit: '30-54918234-9',
    contactoNombre: 'Roberto Rossi',
    email: 'ventas@kallay.com.ar',
    telefono: '+54 11 4752-9900',
    formatoArchivo: 'xlsx',
    estado: 'activo',
  },
  {
    id: 'prov-2',
    nombre: 'Energizer Argentina S.A.',
    cuit: '30-68192039-4',
    contactoNombre: 'Mariana Gómez',
    email: 'pedidos@energizer-dist.com',
    telefono: '+54 11 5233-1100',
    formatoArchivo: 'xlsx',
    estado: 'activo',
  },
  {
    id: 'prov-3',
    nombre: 'Distribuidora San Martín',
    cuit: '33-71029384-9',
    contactoNombre: 'Carlos Méndez',
    email: 'contacto@distrisanmartin.com.ar',
    telefono: '+54 11 4488-3321',
    formatoArchivo: 'csv',
    estado: 'inactivo',
  },
];

export const PERFILES_IMPORTACION: PerfilImportacion[] = [
  {
    id: 'perf-1',
    proveedorId: 'prov-1',
    nombre: 'Kallay - Lista General de Precios',
    nombreHoja: 'Hoja1',
    filaEncabezado: 1,
    activo: true,
    mapeoColumnas: [
      { id: 'm-1', columnaOrigen: 'codigo', columnaDestino: 'COD_ART', operacion: 'copiar' },
      { id: 'm-2', columnaOrigen: 'producto', columnaDestino: 'DETALLE', operacion: 'copiar' },
      { id: 'm-3', columnaOrigen: 'precio de costo', columnaDestino: 'COSTO_BASE', operacion: 'copiar' },
      { id: 'm-4', columnaOrigen: 'precio de costo', columnaDestino: 'PRECIO_VENTA', operacion: 'sumar_porcentaje', valorParametro: '50' },
      { id: 'm-5', columnaOrigen: 'stock', columnaDestino: 'CANT_STOCK', operacion: 'copiar' },
    ],
  },
  {
    id: 'perf-2',
    proveedorId: 'prov-2',
    nombre: 'Energizer - Formato Mayorista LSTPRE',
    nombreHoja: 'LSTPRE',
    filaEncabezado: 7,
    activo: true,
    mapeoColumnas: [
      { id: 'm-6', columnaOrigen: 'CODIGO', columnaDestino: 'COD_ART', operacion: 'copiar' },
      { id: 'm-7', columnaOrigen: 'DESCRIP', columnaDestino: 'DETALLE', operacion: 'copiar' },
      { id: 'm-8', columnaOrigen: 'PRECIO', columnaDestino: 'COSTO_BASE', operacion: 'copiar' },
      { id: 'm-9', columnaOrigen: 'CON_IVA', columnaDestino: 'PRECIO_VENTA', operacion: 'sumar_porcentaje', valorParametro: '30' },
    ],
  },
];

export const IMPORTACIONES: ImportacionHistorial[] = [
  {
    id: 'imp-101',
    fecha: '2026-09-24 16:45',
    proveedorNombre: 'Cerrajera Kallay S.A.',
    archivoNombre: 'dataset-corregido.xlsx',
    filasTotales: 19,
    filasValidas: 17,
    filasError: 2,
    estado: 'con_advertencias',
  },
  {
    id: 'imp-100',
    fecha: '2026-09-20 10:12',
    proveedorNombre: 'Energizer Argentina S.A.',
    archivoNombre: 'LSTPRE_SEPTIEMBRE.xlsx',
    filasTotales: 1959,
    filasValidas: 1950,
    filasError: 9,
    estado: 'con_advertencias',
  },
  {
    id: 'imp-099',
    fecha: '2026-09-12 14:00',
    proveedorNombre: 'Cerrajera Kallay S.A.',
    archivoNombre: 'Kallay_Agosto26.xlsx',
    filasTotales: 22,
    filasValidas: 22,
    filasError: 0,
    estado: 'completado',
  },
];

export const FILAS_PREVIA_EJEMPLO: FilaProcesada[] = [
  { id: 1, filaOriginal: 4, codigo: 'K4002', descripcion: 'KALLAY 4002', costoProveedor: 19100, precioDestino: 46869.11, esValida: true },
  { id: 2, filaOriginal: 5, codigo: 'K4003', descripcion: 'KALLAY 4003', costoProveedor: 19100, precioDestino: 46869.11, esValida: true },
  { id: 3, filaOriginal: 6, codigo: 'PASACRUZ', descripcion: 'PASADOR CERRO CRUZ', costoProveedor: 9800, precioDestino: 24048.02, esValida: true },
  { id: 4, filaOriginal: 7, codigo: 'K4006', descripcion: 'KALLAY 4006', costoProveedor: 14500.8, precioDestino: 35583.22, esValida: true },
  { id: 5, filaOriginal: 14, codigo: 'K5002', descripcion: 'kallay 5002', costoProveedor: 27067.5, precioDestino: 66420.40, esValida: false, motivoError: 'Columna stock vacía en fila obligatoria' },
  { id: 6, filaOriginal: 15, codigo: '', descripcion: 'Cerradura sin código asignado', costoProveedor: 15232.5, precioDestino: 37378.73, esValida: false, motivoError: 'Código de producto ausente o nulo' },
  { id: 7, filaOriginal: 16, codigo: 'K4015', descripcion: 'Kallay 4015', costoProveedor: 19353.6, precioDestino: 47491.41, esValida: true },
  { id: 8, filaOriginal: 18, codigo: 'S525', descripcion: 'star 525', costoProveedor: 0, precioDestino: 0, esValida: false, motivoError: 'Precio de costo igual a 0 o no numérico' },
];