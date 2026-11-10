import { useEffect, useState } from 'react';
import {
  cancelarImportacion,
  confirmarImportacion,
  descargarErrores,
  descargarExportacion,
  obtenerHistorial,
} from '../../api/cliente';
import type { ImportacionResumen } from '../../api/tipos';
import EstadoImportacion, { formatearFecha } from '../../components/ui/EstadoImportacion';

const BOTON = 'rounded px-2 py-1 text-xs font-medium hover:underline disabled:opacity-50 focus-visible:outline-none focus-visible:ring-2';

/** Todas las importaciones, con sus descargas. Una vista previa pendiente se puede confirmar o descartar desde acá. */
export default function Historial() {
  const [historial, setHistorial] = useState<ImportacionResumen[]>([]);
  const [cargando, setCargando] = useState(true);
  const [ocupado, setOcupado] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  function cargar() {
    obtenerHistorial()
      .then(setHistorial)
      .catch((e: Error) => setError(e.message))
      .finally(() => setCargando(false));
  }

  useEffect(cargar, []);

  async function ejecutar(id: number, accion: () => Promise<unknown>, recargar = false) {
    setOcupado(id);
    setError(null);
    try {
      await accion();
      if (recargar) cargar();
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setOcupado(null);
    }
  }

  return (
    <div className="space-y-4">
      <p className="text-sm text-slate-500">
        Cada vista previa queda guardada. El archivo para Organizalo se descarga de las importaciones confirmadas; el
        reporte de errores, de cualquiera.
      </p>

      {error && <p role="alert" className="rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-700">{error}</p>}

      <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white shadow-sm">
        <table className="min-w-full divide-y divide-slate-200 text-sm">
          <thead className="bg-slate-50">
            <tr>
              <th scope="col" className="px-4 py-3 text-left font-medium text-slate-600">#</th>
              <th scope="col" className="px-4 py-3 text-left font-medium text-slate-600">Fecha</th>
              <th scope="col" className="px-4 py-3 text-left font-medium text-slate-600">Proveedor / perfil</th>
              <th scope="col" className="px-4 py-3 text-left font-medium text-slate-600">Archivo</th>
              <th scope="col" className="px-4 py-3 text-right font-medium text-slate-600">Válidas</th>
              <th scope="col" className="px-4 py-3 text-right font-medium text-slate-600">Errores</th>
              <th scope="col" className="px-4 py-3 text-left font-medium text-slate-600">Usuario</th>
              <th scope="col" className="px-4 py-3 text-center font-medium text-slate-600">Estado</th>
              <th scope="col" className="px-4 py-3 text-right font-medium text-slate-600">Acciones</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {cargando && <tr><td colSpan={9} className="px-4 py-6 text-center text-slate-500">Cargando…</td></tr>}
            {!cargando && historial.length === 0 && (
              <tr><td colSpan={9} className="px-4 py-6 text-center text-slate-500">Todavía no hay importaciones.</td></tr>
            )}
            {historial.map((imp) => (
              <tr key={imp.id} className="hover:bg-slate-50/60">
                <td className="px-4 py-3 text-slate-500">{imp.id}</td>
                <td className="px-4 py-3 whitespace-nowrap text-slate-600">{formatearFecha(imp.creadoEn)}</td>
                <td className="px-4 py-3">
                  <span className="font-medium text-slate-900">{imp.proveedor}</span>
                  <span className="block text-xs text-slate-500">{imp.perfil}</span>
                </td>
                <td className="px-4 py-3 font-mono text-xs text-slate-600">{imp.archivo}</td>
                <td className="px-4 py-3 text-right font-medium text-emerald-700">{imp.validas}</td>
                <td className="px-4 py-3 text-right font-medium text-rose-700">{imp.errores}</td>
                <td className="px-4 py-3 text-slate-600">{imp.usuario}</td>
                <td className="px-4 py-3 text-center"><EstadoImportacion estado={imp.estado} /></td>
                <td className="px-4 py-3 text-right whitespace-nowrap">
                  {imp.estado === 'PREVIEW' && (
                    <>
                      <button
                        type="button"
                        disabled={ocupado === imp.id || imp.validas === 0}
                        onClick={() => ejecutar(imp.id, () => confirmarImportacion(imp.id), true)}
                        className={`${BOTON} text-blue-700 focus-visible:ring-blue-600`}
                      >
                        Confirmar
                      </button>
                      <button
                        type="button"
                        disabled={ocupado === imp.id}
                        onClick={() => ejecutar(imp.id, () => cancelarImportacion(imp.id), true)}
                        className={`${BOTON} text-slate-600 focus-visible:ring-slate-400`}
                      >
                        Descartar
                      </button>
                    </>
                  )}
                  {imp.estado === 'CONFIRMADA' && (
                    <button
                      type="button"
                      disabled={ocupado === imp.id}
                      onClick={() => ejecutar(imp.id, () => descargarExportacion(imp.id))}
                      className={`${BOTON} text-emerald-700 focus-visible:ring-emerald-600`}
                    >
                      Excel destino
                    </button>
                  )}
                  <button
                    type="button"
                    disabled={ocupado === imp.id}
                    onClick={() => ejecutar(imp.id, () => descargarErrores(imp.id))}
                    className={`${BOTON} text-rose-700 focus-visible:ring-rose-600`}
                  >
                    Reporte
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
