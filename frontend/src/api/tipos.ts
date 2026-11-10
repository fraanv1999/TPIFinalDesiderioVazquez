// Respuestas de la API del backend (ver backend/.../api y backend/.../motor).

export interface Usuario {
  id: number;
  nombre: string;
  email: string;
  rol: 'ADMIN' | 'OPERADOR';
}

export interface Proveedor {
  id: number;
  nombre: string;
  activo: boolean;
  /** Cantidad de perfiles de importación. Con 0 se puede borrar. */
  perfiles: number;
}

export interface DatosProveedor {
  nombre: string;
  activo: boolean;
}

export interface PerfilResumen {
  id: number;
  proveedor: string;
  nombre: string;
  hoja: string;
}

export interface MapeoColumna {
  columna: string;
  encabezado: string;
  /** null = columna ignorada: solo se controla su encabezado. */
  campo: string | null;
  transformaciones: string[];
}

export interface ReglaCalculo {
  resultado: string;
  operacion: string;
  base: string | null;
  secundario: string | null;
  /** Fracción: 0.04 = 4 %. */
  valor: number | null;
}

export interface Perfil {
  id: number;
  proveedor: string;
  nombre: string;
  formato: string;
  hoja: string;
  filaEncabezado: number;
  margenEncabezado?: number;
  filaInicioDatos: number;
  campoClave: string;
  campoCategoria?: string | null;
  columnas: MapeoColumna[];
  reglas: ReglaCalculo[];
}

export interface Diferencia {
  tipo: string;
  /** Si falta (versiones viejas de la API), se toma como bloqueante. */
  bloqueante?: boolean;
  columna: string | null;
  esperado: string | null;
  encontrado: string | null;
  mensaje: string;
}

export interface Resumen {
  validas: number;
  ignoradas: number;
  vacias: number;
  titulos: number;
  errores: number;
}

export interface FilaResultado {
  numeroFila: number;
  estado: 'VALIDA' | 'IGNORADA' | 'ERROR';
  motivoIgnorada: 'VACIA' | 'TITULO' | null;
  datosCrudos: Record<string, string | number>;
  datosResultado: Record<string, string | number | null> | null;
  errores: { campo: string; mensaje: string }[];
}

export interface CodigoRepetido {
  codigo: string;
  filas: number[];
}

export type EstadoImportacion = 'PREVIEW' | 'CONFIRMADA' | 'CANCELADA' | 'RECHAZADA';

export interface ResultadoImportacion {
  /** Id de la importación guardada. */
  id: number;
  perfil: number;
  archivo: string;
  estado: EstadoImportacion;
  diferencias: Diferencia[];
  resumen: Resumen;
  repetidos?: CodigoRepetido[];
  filas: FilaResultado[];
}

/** Una fila del historial. */
export interface ImportacionResumen {
  id: number;
  perfilId: number;
  perfil: string;
  proveedor: string;
  archivo: string;
  estado: EstadoImportacion;
  validas: number;
  ignoradas: number;
  errores: number;
  usuario: string;
  /** ISO 8601 */
  creadoEn: string;
}
