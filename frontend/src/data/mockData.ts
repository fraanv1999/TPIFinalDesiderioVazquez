import type { Proveedor, PerfilImportacion, ImportacionHistorial } from '../types';

export const PROVEEDORES: Proveedor[] = [
  {
    id: 'prov-1',
    nombre: 'Cerrajera Kallay S.A.',
    estado: 'activo',
  },
  {
    id: 'prov-2',
    nombre: 'Energizer Argentina S.A.',
    estado: 'activo',
  },
  {
    id: 'prov-3',
    nombre: 'Distribuidora San Martín',
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
      { id: 'm-4', columnaOrigen: 'precio de costo', columnaDestino: 'PRECIO_VENTA', operacion: 'sumar_porcentaje', valorParametro: 0.5 },
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
      { id: 'm-9', columnaOrigen: 'CON_IVA', columnaDestino: 'PRECIO_VENTA', operacion: 'sumar_porcentaje', valorParametro: 0.3 },
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
