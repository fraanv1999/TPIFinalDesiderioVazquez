import { useEffect, useState } from 'react';
import { obtenerHistorial, obtenerPerfiles, obtenerProveedores } from '../../api/cliente';
import type { ImportacionResumen, PerfilResumen, Proveedor } from '../../api/tipos';
import type { SeccionApp } from '../../types';
import EstadoImportacion, { formatearFecha } from '../../components/ui/EstadoImportacion';

interface DashboardProps {
  onIrASeccion: (seccion: SeccionApp) => void;
}

export default function Dashboard({ onIrASeccion }: DashboardProps) {
  const [proveedores, setProveedores] = useState<Proveedor[]>([]);
  const [perfiles, setPerfiles] = useState<PerfilResumen[]>([]);
  const [historial, setHistorial] = useState<ImportacionResumen[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([obtenerProveedores(), obtenerPerfiles(), obtenerHistorial()])
      .then(([prov, perf, hist]) => {
        setProveedores(prov);
        setPerfiles(perf);
        setHistorial(hist);
      })
      .catch((e: Error) => setError(e.message));
  }, []);

  const proveedoresActivos = proveedores.filter((p) => p.activo).length;
  // El historial viene ordenado de la más nueva a la más vieja.
  const ultima = historial[0];
  const recientes = historial.slice(0, 8);

  return (
    <div className="space-y-6">
      {error && <p role="alert" className="rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-700">{error}</p>}

      {/* Tarjetas de Métricas */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
          <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">Proveedores activos</p>
          <p className="mt-2 text-3xl font-semibold text-slate-900">{proveedoresActivos}</p>
          <button
            type="button"
            onClick={() => onIrASeccion('proveedores')}
            className="mt-3 text-xs font-medium text-blue-700 hover:underline focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600 rounded"
          >
            Ver proveedores →
          </button>
        </div>

        <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
          <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">Perfiles configurados</p>
          <p className="mt-2 text-3xl font-semibold text-slate-900">{perfiles.length}</p>
          <button
            type="button"
            onClick={() => onIrASeccion('perfiles')}
            className="mt-3 text-xs font-medium text-blue-700 hover:underline focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600 rounded"
          >
            Ver perfiles de mapeo →
          </button>
        </div>

        <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
          <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">Última importación</p>
          <p className="mt-2 text-xl font-semibold text-slate-900">{ultima ? formatearFecha(ultima.creadoEn) : '—'}</p>
          <p className="mt-1 text-xs text-slate-500 truncate">{ultima?.archivo}</p>
        </div>
      </div>

      {/* Acceso Rápido al Wizard */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 rounded-lg border border-blue-200 bg-blue-50/60 p-5">
        <div>
          <h2 className="text-base font-semibold text-slate-900">¿Tenés una lista nueva para procesar?</h2>
          <p className="text-sm text-slate-600">Iniciá el asistente para cargar el Excel y transformarlo al formato del sistema.</p>
        </div>
        <button
          type="button"
          onClick={() => onIrASeccion('asistente')}
          className="inline-flex items-center gap-2 rounded-md bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-blue-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-2"
        >
          Iniciar importación
        </button>
      </div>

      {/* Historial Reciente */}
      <div className="rounded-lg border border-slate-200 bg-white shadow-sm">
        <div className="border-b border-slate-200 px-5 py-4">
          <h2 className="text-sm font-semibold text-slate-900">Importaciones recientes</h2>
        </div>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-slate-200 text-sm">
            <thead className="bg-slate-50">
              <tr>
                <th scope="col" className="px-4 py-3 text-left font-medium text-slate-600">Fecha</th>
                <th scope="col" className="px-4 py-3 text-left font-medium text-slate-600">Proveedor</th>
                <th scope="col" className="px-4 py-3 text-left font-medium text-slate-600">Archivo</th>
                <th scope="col" className="px-4 py-3 text-right font-medium text-slate-600">Válidas</th>
                <th scope="col" className="px-4 py-3 text-right font-medium text-slate-600">Errores</th>
                <th scope="col" className="px-4 py-3 text-center font-medium text-slate-600">Estado</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {recientes.length === 0 && (
                <tr><td colSpan={6} className="px-4 py-6 text-center text-slate-500">Todavía no hay importaciones.</td></tr>
              )}
              {recientes.map((imp) => (
                <tr key={imp.id} className="hover:bg-slate-50/70">
                  <td className="px-4 py-3 text-slate-600 whitespace-nowrap">{formatearFecha(imp.creadoEn)}</td>
                  <td className="px-4 py-3 font-medium text-slate-900">{imp.proveedor}</td>
                  <td className="px-4 py-3 text-slate-600 font-mono text-xs">{imp.archivo}</td>
                  <td className="px-4 py-3 text-right font-medium text-emerald-700">{imp.validas}</td>
                  <td className="px-4 py-3 text-right font-medium text-rose-700">{imp.errores}</td>
                  <td className="px-4 py-3 text-center"><EstadoImportacion estado={imp.estado} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
