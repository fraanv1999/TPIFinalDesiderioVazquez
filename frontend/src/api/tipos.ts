// Respuestas de la API del backend (ver backend/.../api y backend/.../motor).

export interface PerfilResumen {
  id: string;
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
  id: string;
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

export interface ResultadoImportacion {
  perfil: string;
  archivo: string;
  estado: 'PREVIEW' | 'RECHAZADA';
  diferencias: Diferencia[];
  resumen: Resumen;
  repetidos?: CodigoRepetido[];
  filas: FilaResultado[];
}
