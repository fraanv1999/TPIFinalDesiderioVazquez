import { useState } from 'react';
import { PERFILES_IMPORTACION, PROVEEDORES } from '../../data/mockData';
import type { PerfilImportacion } from '../../types';
import Modal from '../../components/ui/Modal';

export default function PerfilesImportacion() {
  const [perfilDetalle, setPerfilDetalle] = useState<PerfilImportacion | null>(null);

  function nombreProveedor(id: string) {
    return PROVEEDORES.find((p) => p.id === id)?.nombre ?? 'Desconocido';
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-base font-semibold text-slate-900">Perfiles de Importación</h2>
          <p className="text-sm text-slate-500">Mapeos de columnas, cabeceras y reglas de cálculo fijadas por proveedor.</p>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
        {PERFILES_IMPORTACION.map((perfil) => (
          <div key={perfil.id} className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm flex flex-col justify-between">
            <div>
              <div className="flex items-start justify-between gap-2">
                <div>
                  <h3 className="font-semibold text-slate-900">{perfil.nombre}</h3>
                  <p className="text-xs text-blue-700 font-medium">{nombreProveedor(perfil.proveedorId)}</p>
                </div>
                <span
                  className={`inline-flex rounded-full px-2 py-0.5 text-xs font-semibold ${
                    perfil.activo ? 'bg-emerald-50 text-emerald-700 border border-emerald-200' : 'bg-slate-100 text-slate-600'
                  }`}
                >
                  {perfil.activo ? 'Activo' : 'Inactivo'}
                </span>
              </div>

              <dl className="mt-4 grid grid-cols-2 gap-2 text-xs text-slate-600 border-t border-slate-100 pt-3">
                <div>
                  <dt className="text-slate-400">Hoja a procesar:</dt>
                  <dd className="font-semibold font-mono text-slate-800">"{perfil.nombreHoja}"</dd>
                </div>
                <div>
                  <dt className="text-slate-400">Fila encabezados:</dt>
                  <dd className="font-semibold font-mono text-slate-800">Fila #{perfil.filaEncabezado}</dd>
                </div>
                <div className="col-span-2">
                  <dt className="text-slate-400">Reglas configuradas:</dt>
                  <dd className="font-medium text-slate-700">{perfil.mapeoColumnas.length} columnas transformadas</dd>
                </div>
              </dl>
            </div>

            <div className="mt-5 border-t border-slate-100 pt-3">
              <button
                type="button"
                onClick={() => setPerfilDetalle(perfil)}
                className="w-full rounded-md border border-slate-200 bg-slate-50 py-2 text-xs font-semibold text-slate-700 hover:bg-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600"
              >
                Ver configuración y fórmulas de cálculo →
              </button>
            </div>
          </div>
        ))}
      </div>

      <Modal
        abierto={perfilDetalle !== null}
        alCerrar={() => setPerfilDetalle(null)}
        tituloId="perfil-titulo"
        anchoMaximo="max-w-2xl"
      >
        {perfilDetalle && (
          <>
            <div className="flex items-start justify-between border-b border-slate-200 pb-3">
              <div>
                <h3 id="perfil-titulo" className="text-base font-semibold text-slate-900">{perfilDetalle.nombre}</h3>
                <p className="text-xs text-slate-500">Proveedor: {nombreProveedor(perfilDetalle.proveedorId)}</p>
              </div>
              <button
                type="button"
                onClick={() => setPerfilDetalle(null)}
                aria-label="Cerrar modal"
                className="rounded p-1 text-slate-400 hover:text-slate-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600"
              >
                ✕
              </button>
            </div>

            <div className="mt-4 space-y-4">
              <div className="grid grid-cols-2 gap-3 rounded-md bg-slate-50 p-3 text-xs">
                <div><span className="font-semibold text-slate-600">Hoja Excel:</span> {perfilDetalle.nombreHoja}</div>
                <div><span className="font-semibold text-slate-600">Fila encabezados:</span> Fila {perfilDetalle.filaEncabezado}</div>
              </div>

              <div>
                <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-500 mb-2">Mapeo de Columnas y Fórmulas</h4>
                <div className="overflow-x-auto rounded border border-slate-200">
                  <table className="min-w-full divide-y divide-slate-200 text-xs">
                    <thead className="bg-slate-50">
                      <tr>
                        <th scope="col" className="px-3 py-2 text-left font-semibold text-slate-600">Columna Origen (Excel)</th>
                        <th scope="col" className="px-3 py-2 text-left font-semibold text-slate-600">Operación / Cálculo</th>
                        <th scope="col" className="px-3 py-2 text-left font-semibold text-slate-600">Columna Destino</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {perfilDetalle.mapeoColumnas.map((m) => (
                        <tr key={m.id}>
                          <td className="px-3 py-2 font-mono text-slate-700">{m.columnaOrigen}</td>
                          <td className="px-3 py-2">
                            <span className="inline-block rounded bg-slate-100 px-2 py-0.5 font-medium text-slate-800">
                              {m.operacion} {m.valorParametro ? `(${m.valorParametro}%)` : ''}
                            </span>
                          </td>
                          <td className="px-3 py-2 font-mono font-semibold text-blue-700">{m.columnaDestino}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>

            <div className="mt-6 flex justify-end">
              <button
                type="button"
                onClick={() => setPerfilDetalle(null)}
                className="rounded-md bg-slate-800 px-4 py-2 text-xs font-semibold text-white hover:bg-slate-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600"
              >
                Cerrar vista
              </button>
            </div>
          </>
        )}
      </Modal>
    </div>
  );
}
