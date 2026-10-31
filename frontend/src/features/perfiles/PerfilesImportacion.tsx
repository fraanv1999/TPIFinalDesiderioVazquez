import { useEffect, useState } from 'react';
import { obtenerPerfil, obtenerPerfiles } from '../../api/cliente';
import type { Perfil, ReglaCalculo } from '../../api/tipos';
import Modal from '../../components/ui/Modal';

/** "SUMAR_PORCENTAJE(ingreso_bruto, 50 %)": la regla en una línea, con el porcentaje ×100 solo para mostrar. */
function describirRegla(r: ReglaCalculo) {
  const argumentos = [r.base, r.secundario].filter(Boolean) as string[];
  if (r.valor !== null) {
    const esPorcentaje = r.operacion.includes('PORCENTAJE') || r.operacion === 'MULTIPLICAR';
    argumentos.push(esPorcentaje ? `${(r.valor * 100).toLocaleString('es-AR')} %` : r.valor.toLocaleString('es-AR'));
  }
  return `${r.operacion}(${argumentos.join(', ')})`;
}

export default function PerfilesImportacion() {
  const [perfiles, setPerfiles] = useState<Perfil[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [perfilDetalle, setPerfilDetalle] = useState<Perfil | null>(null);

  useEffect(() => {
    obtenerPerfiles()
      .then((resumenes) => Promise.all(resumenes.map((p) => obtenerPerfil(p.id))))
      .then(setPerfiles)
      .catch((e: Error) => setError(e.message));
  }, []);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-base font-semibold text-slate-900">Perfiles de Importación</h2>
          <p className="text-sm text-slate-500">Un perfil por hoja: encabezados esperados, mapeo de columnas y reglas de cálculo.</p>
        </div>
      </div>

      {error && (
        <p role="alert" className="rounded-md border border-rose-200 bg-rose-50 p-3 text-sm text-rose-800">{error}</p>
      )}
      {!error && perfiles === null && <p className="text-sm text-slate-500">Cargando perfiles…</p>}
      {perfiles?.length === 0 && <p className="text-sm text-slate-500">El servidor no tiene perfiles cargados.</p>}

      <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
        {perfiles?.map((perfil) => (
          <div key={perfil.id} className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm flex flex-col justify-between">
            <div>
              <div className="flex items-start justify-between gap-2">
                <div>
                  <h3 className="font-semibold text-slate-900">{perfil.nombre}</h3>
                  <p className="text-xs text-blue-700 font-medium">{perfil.proveedor}</p>
                </div>
                <span className="inline-flex rounded-full bg-slate-100 px-2 py-0.5 font-mono text-xs text-slate-600">
                  {perfil.formato}
                </span>
              </div>

              <dl className="mt-4 grid grid-cols-2 gap-2 text-xs text-slate-600 border-t border-slate-100 pt-3">
                <div>
                  <dt className="text-slate-400">Hoja a procesar:</dt>
                  <dd className="font-semibold font-mono text-slate-800 whitespace-pre">"{perfil.hoja}"</dd>
                </div>
                <div>
                  <dt className="text-slate-400">Fila encabezados:</dt>
                  <dd className="font-semibold font-mono text-slate-800">
                    Fila #{perfil.filaEncabezado}
                    {perfil.margenEncabezado ? ` (±${perfil.margenEncabezado})` : ''}
                  </dd>
                </div>
                <div className="col-span-2">
                  <dt className="text-slate-400">Configuración:</dt>
                  <dd className="font-medium text-slate-700">
                    {perfil.columnas.filter((c) => c.campo).length} columnas mapeadas · {perfil.reglas.length} reglas de cálculo
                  </dd>
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
                <p className="text-xs text-slate-500">Proveedor: {perfilDetalle.proveedor}</p>
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
                <div><span className="font-semibold text-slate-600">Hoja Excel:</span> <span className="whitespace-pre">"{perfilDetalle.hoja}"</span></div>
                <div><span className="font-semibold text-slate-600">Fila encabezados:</span> {perfilDetalle.filaEncabezado}</div>
                <div><span className="font-semibold text-slate-600">Datos desde la fila:</span> {perfilDetalle.filaInicioDatos}</div>
                <div><span className="font-semibold text-slate-600">Campo clave:</span> {perfilDetalle.campoClave}</div>
              </div>

              <div>
                <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-500 mb-2">Mapeo de columnas</h4>
                <div className="overflow-x-auto rounded border border-slate-200">
                  <table className="min-w-full divide-y divide-slate-200 text-xs">
                    <thead className="bg-slate-50">
                      <tr>
                        <th scope="col" className="px-3 py-2 text-left font-semibold text-slate-600">Columna</th>
                        <th scope="col" className="px-3 py-2 text-left font-semibold text-slate-600">Encabezado esperado</th>
                        <th scope="col" className="px-3 py-2 text-left font-semibold text-slate-600">Transformaciones</th>
                        <th scope="col" className="px-3 py-2 text-left font-semibold text-slate-600">Campo destino</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {perfilDetalle.columnas.map((m) => (
                        <tr key={m.columna}>
                          <td className="px-3 py-2 font-mono text-slate-700">{m.columna}</td>
                          <td className="px-3 py-2 text-slate-700">{m.encabezado}</td>
                          <td className="px-3 py-2 font-mono text-slate-500">{m.transformaciones.join(', ') || '—'}</td>
                          <td className={`px-3 py-2 font-mono ${m.campo ? 'font-semibold text-blue-700' : 'text-slate-400'}`}>
                            {m.campo ?? 'ignorada'}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>

              <div>
                <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-500 mb-2">Reglas de cálculo (en orden)</h4>
                {perfilDetalle.reglas.length === 0 ? (
                  <p className="text-xs text-slate-500">Sin reglas.</p>
                ) : (
                  <ol className="space-y-1 text-xs">
                    {perfilDetalle.reglas.map((r, i) => (
                      <li key={i} className="font-mono text-slate-700">
                        {i + 1}. <span className="font-semibold text-blue-700">{r.resultado}</span> = {describirRegla(r)}
                      </li>
                    ))}
                  </ol>
                )}
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
