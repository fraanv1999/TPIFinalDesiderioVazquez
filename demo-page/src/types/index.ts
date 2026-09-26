export type SeccionApp = 'dashboard' | 'proveedores' | 'perfiles' | 'asistente';

export type FormatoArchivo = 'xlsx' | 'csv';
export type EstadoEntidad = 'activo' | 'inactivo';

export interface Proveedor {
  id: string;
  nombre: string;
  cuit: string;
  contactoNombre: string;
  email: string;
  telefono: string;
  formatoArchivo: FormatoArchivo;
  estado: EstadoEntidad;
}

export type TipoOperacion =
  | 'copiar'
  | 'valor_fijo'
  | 'sumar_porcentaje'
  | 'restar_porcentaje'
  | 'multiplicar'
  | 'dividir'
  | 'concatenar';

export interface ReglaMapeo {
  id: string;
  columnaOrigen: string;
  columnaDestino: string;
  operacion: TipoOperacion;
  valorParametro?: string;
}

export interface PerfilImportacion {
  id: string;
  proveedorId: string;
  nombre: string;
  nombreHoja: string;
  filaEncabezado: number;
  activo: boolean;
  mapeoColumnas: ReglaMapeo[];
}

export interface FilaProcesada {
  id: number;
  filaOriginal: number;
  codigo: string;
  descripcion: string;
  costoProveedor: number;
  precioDestino: number;
  esValida: boolean;
  motivoError?: string;
}

export interface ImportacionHistorial {
  id: string;
  fecha: string;
  proveedorNombre: string;
  archivoNombre: string;
  filasTotales: number;
  filasValidas: number;
  filasError: number;
  estado: 'completado' | 'con_advertencias' | 'fallido';
}