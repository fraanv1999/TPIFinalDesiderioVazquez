import { PROVEEDORES, PERFILES_IMPORTACION, IMPORTACIONES } from '../../data/mockData';
import type { SeccionApp } from '../../types';

interface DashboardProps {
  onIrASeccion: (seccion: SeccionApp) => void;
}

export default function Dashboard({ onIrASeccion }: DashboardProps) {
  const proveedoresActivos = PROVEEDORES.filter((p) => p.estado === 'activo').length;
  const perfilesActivos = PERFILES_IMPORTACION.filter((p) => p.activo).length;
  const ultimaImportacion = [...IMPORTACIONES].sort((a, b) => (a.fecha < b.fecha ? 1 : -1))[0];

  return (
    <div className="space-y-6">
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
            Administrar proveedores →
          </button>
        </div>

        <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
          <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">Perfiles configurados</p>
          <p className="mt-2 text-3xl font-semibold text-slate-900">{perfilesActivos}</p>
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
          <p className="mt-2 text-xl font-semibold text-slate-900">{ultimaImportacion?.fecha ?? '—'}</p>
          <p className="mt-1 text-xs text-slate-500 truncate">{ultimaImportacion?.archivoNombre}</p>
        </div>
      </div>

      {/* Acceso Rápido al Wizard */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 rounded-lg border border-blue-200 bg-blue-50/60 p-5">
        <div>
          <h2 className="text-base font-semibold text-slate-900">¿Tienes una lista nueva para procesar?</h2>
          <p className="text-sm text-slate-600">Inicia el asistente para cargar el archivo Excel y transformarlo al formato del sistema.</p>
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
          <h2 className="text-sm font-semibold text-slate-900">Historial reciente de transformaciones</h2>
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
              {IMPORTACIONES.map((imp) => (
                <tr key={imp.id} className="hover:bg-slate-50/70">
                  <td className="px-4 py-3 text-slate-600 whitespace-nowrap">{imp.fecha}</td>
                  <td className="px-4 py-3 font-medium text-slate-900">{imp.proveedorNombre}</td>
                  <td className="px-4 py-3 text-slate-600 font-mono text-xs">{imp.archivoNombre}</td>
                  <td className="px-4 py-3 text-right font-medium text-emerald-700">{imp.filasValidas}</td>
                  <td className="px-4 py-3 text-right font-medium text-rose-700">{imp.filasError}</td>
                  <td className="px-4 py-3 text-center">
                    <span
                      className={`inline-flex rounded-full px-2 py-0.5 text-xs font-semibold ${
                        imp.estado === 'completado'
                          ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                          : 'bg-amber-50 text-amber-800 border border-amber-200'
                      }`}
                    >
                      {imp.estado === 'completado' ? 'Sin errores' : 'Con errores'}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}