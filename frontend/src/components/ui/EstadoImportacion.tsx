import type { EstadoImportacion as Estado } from '../../api/tipos';

const ESTILOS: Record<Estado, { texto: string; clases: string }> = {
  PREVIEW: { texto: 'Vista previa', clases: 'bg-blue-50 text-blue-700 border-blue-200' },
  CONFIRMADA: { texto: 'Confirmada', clases: 'bg-emerald-50 text-emerald-700 border-emerald-200' },
  CANCELADA: { texto: 'Cancelada', clases: 'bg-slate-100 text-slate-600 border-slate-200' },
  RECHAZADA: { texto: 'Rechazada', clases: 'bg-rose-50 text-rose-700 border-rose-200' },
};

export default function EstadoImportacion({ estado }: { estado: Estado }) {
  const { texto, clases } = ESTILOS[estado];
  return <span className={`inline-flex rounded-full border px-2 py-0.5 text-xs font-semibold ${clases}`}>{texto}</span>;
}

/** "07/11/2026 18:30" */
export function formatearFecha(iso: string): string {
  return new Date(iso).toLocaleString('es-AR', { dateStyle: 'short', timeStyle: 'short' });
}
