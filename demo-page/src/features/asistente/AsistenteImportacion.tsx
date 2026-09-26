import { useState } from 'react';
import { PROVEEDORES, PERFILES_IMPORTACION, FILAS_PREVIA_EJEMPLO } from '../../data/mockData';

export default function AsistenteImportacion() {
  const [paso, setPaso] = useState<1 | 2 | 3 | 4>(1);

  // Estados del wizard
  const [archivoSeleccionado, setArchivoSeleccionado] = useState<string>('dataset-corregido.xlsx');
  const [proveedorId, setProveedorId] = useState<string>('prov-1');
  const [perfilId, setPerfilId] = useState<string>('perf-1');
  const [filtroVista, setFiltroVista] = useState<'todas' | 'validas' | 'errores'>('todas');

  const filas = FILAS_PREVIA_EJEMPLO;
  const filasValidas = filas.filter((f) => f.esValida);
  const filasError = filas.filter((f) => !f.esValida);

  const filasAMostrar =
    filtroVista === 'validas' ? filasValidas : filtroVista === 'errores' ? filasError : filas;

  return (
    <div className="space-y-6">
      {/* Indicador de pasos accesible */}
      <nav aria-label="Progreso del asistente" className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <ol className="grid grid-cols-2 gap-2 sm:grid-cols-4">
          {[
            { num: 1, titulo: '1. Cargar Archivo' },
            { num: 2, titulo: '2. Seleccionar Perfil' },
            { num: 3, titulo: '3. Vista Previa' },
            { num: 4, titulo: '4. Exportar' },
          ].map((item) => (
            <li
              key={item.num}
              className={`flex items-center gap-2 text-xs font-medium ${
                paso === item.num
                  ? 'text-blue-700 font-semibold'
                  : paso > item.num
                  ? 'text-emerald-700'
                  : 'text-slate-400'
              }`}
            >
              <span
                className={`flex h-6 w-6 items-center justify-center rounded-full text-xs ${
                  paso === item.num
                    ? 'bg-blue-600 text-white'
                    : paso > item.num
                    ? 'bg-emerald-100 text-emerald-800'
                    : 'bg-slate-200 text-slate-600'
                }`}
              >
                {paso > item.num ? '✓' : item.num}
              </span>
              <span>{item.titulo}</span>
            </li>
          ))}
        </ol>
      </nav>

      {/* PASO 1: Subida de archivo */}
      {paso === 1 && (
        <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm space-y-4">
          <div>
            <h2 className="text-base font-semibold text-slate-900">Paso 1: Selecciona la lista de precios del proveedor</h2>
            <p className="text-xs text-slate-500">Formatos soportados: Excel (.xlsx, .xls) o CSV delimitado por comas.</p>
          </div>

          <div className="flex flex-col items-center justify-center rounded-lg border-2 border-dashed border-slate-300 p-8 bg-slate-50/50">
            <span className="text-4xl text-slate-400">📄</span>
            <p className="mt-2 text-sm font-medium text-slate-700">Archivo cargado para la demo:</p>
            <span className="mt-1 rounded bg-blue-100 px-3 py-1 font-mono text-xs font-semibold text-blue-900">
              {archivoSeleccionado} (19.4 KB)
            </span>
            <div className="mt-4 flex gap-2">
              <button
                type="button"
                onClick={() => setArchivoSeleccionado('dataset-example.xlsx')}
                className="text-xs font-medium text-blue-700 underline focus-visible:ring-2 focus-visible:ring-blue-600 rounded px-1"
              >
                Cambiar a dataset-example.xlsx
              </button>
              <span className="text-xs text-slate-300">|</span>
              <button
                type="button"
                onClick={() => setArchivoSeleccionado('dataset-corregido.xlsx')}
                className="text-xs font-medium text-blue-700 underline focus-visible:ring-2 focus-visible:ring-blue-600 rounded px-1"
              >
                Cambiar a dataset-corregido.xlsx
              </button>
            </div>
          </div>

          <div className="flex justify-end pt-4">
            <button
              type="button"
              onClick={() => setPaso(2)}
              className="rounded-md bg-blue-600 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-blue-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600"
            >
              Continuar al perfil →
            </button>
          </div>
        </div>
      )}

      {/* PASO 2: Elegir perfil */}
      {paso === 2 && (
        <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm space-y-4">
          <div>
            <h2 className="text-base font-semibold text-slate-900">Paso 2: Asocia el proveedor y el perfil de transformación</h2>
            <p className="text-xs text-slate-500">El perfil determinará qué hoja leer, los encabezados y las fórmulas de cálculo.</p>
          </div>

          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 pt-2">
            <div>
              <label htmlFor="select-prov" className="block text-xs font-semibold text-slate-700">Proveedor</label>
              <select
                id="select-prov"
                value={proveedorId}
                onChange={(e) => {
                  setProveedorId(e.target.value);
                  setPerfilId(e.target.value === 'prov-1' ? 'perf-1' : 'perf-2');
                }}
                className="mt-1 block w-full rounded-md border border-slate-300 bg-white p-2 text-sm shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600"
              >
                {PROVEEDORES.map((p) => (
                  <option key={p.id} value={p.id}>{p.nombre}</option>
                ))}
              </select>
            </div>

            <div>
              <label htmlFor="select-perf" className="block text-xs font-semibold text-slate-700">Perfil de Importación</label>
              <select
                id="select-perf"
                value={perfilId}
                onChange={(e) => setPerfilId(e.target.value)}
                className="mt-1 block w-full rounded-md border border-slate-300 bg-white p-2 text-sm shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600"
              >
                {PERFILES_IMPORTACION.filter((p) => p.proveedorId === proveedorId).map((p) => (
                  <option key={p.id} value={p.id}>{p.nombre}</option>
                ))}
              </select>
            </div>
          </div>

          <div className="rounded-md bg-slate-50 border border-slate-200 p-4 text-xs text-slate-600 space-y-1">
            <p className="font-semibold text-slate-800">Reglas que se aplicarán automáticamente:</p>
            <p>• Lectura de hoja: <span className="font-mono text-slate-800">"{perfilId === 'perf-1' ? 'Hoja1' : 'LSTPRE'}"</span> a partir de la fila #{perfilId === 'perf-1' ? '1' : '7'}.</p>
            <p>• Cálculo de rentabilidad: Ganancia + margen según configuración del perfil.</p>
            <p>• Validación de campos requeridos: Código de producto y precio no nulo.</p>
          </div>

          <div className="flex justify-between pt-4">
            <button
              type="button"
              onClick={() => setPaso(1)}
              className="rounded-md border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-400"
            >
              ← Volver
            </button>
            <button
              type="button"
              onClick={() => setPaso(3)}
              className="rounded-md bg-blue-600 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-blue-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600"
            >
              Procesar y Ver Vista Previa →
            </button>
          </div>
        </div>
      )}

      {/* PASO 3: Vista previa y Clasificación de errores */}
      {paso === 3 && (
        <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm space-y-4">
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
            <div>
              <h2 className="text-base font-semibold text-slate-900">Paso 3: Vista Previa y Clasificación</h2>
              <p className="text-xs text-slate-500">Las filas con errores se informan en el reporte y no se descartan en silencio.</p>
            </div>

            {/* Selector de filtros accesible */}
            <div className="flex rounded-md shadow-sm border border-slate-200 p-0.5 bg-slate-50 text-xs">
              <button
                type="button"
                onClick={() => setFiltroVista('todas')}
                className={`rounded px-2.5 py-1 font-medium ${filtroVista === 'todas' ? 'bg-white shadow-sm text-slate-900 font-semibold' : 'text-slate-600'}`}
              >
                Todas ({filas.length})
              </button>
              <button
                type="button"
                onClick={() => setFiltroVista('validas')}
                className={`rounded px-2.5 py-1 font-medium ${filtroVista === 'validas' ? 'bg-white shadow-sm text-emerald-800 font-semibold' : 'text-slate-600'}`}
              >
                Válidas ({filasValidas.length})
              </button>
              <button
                type="button"
                onClick={() => setFiltroVista('errores')}
                className={`rounded px-2.5 py-1 font-medium ${filtroVista === 'errores' ? 'bg-white shadow-sm text-rose-800 font-semibold' : 'text-slate-600'}`}
              >
                Con Errores ({filasError.length})
              </button>
            </div>
          </div>

          <div className="overflow-x-auto rounded-lg border border-slate-200">
            <table className="min-w-full divide-y divide-slate-200 text-xs">
              <thead className="bg-slate-50">
                <tr>
                  <th scope="col" className="px-3 py-2.5 text-left font-semibold text-slate-600">Fila Excel</th>
                  <th scope="col" className="px-3 py-2.5 text-left font-semibold text-slate-600">Código</th>
                  <th scope="col" className="px-3 py-2.5 text-left font-semibold text-slate-600">Descripción</th>
                  <th scope="col" className="px-3 py-2.5 text-right font-semibold text-slate-600">Costo Origen</th>
                  <th scope="col" className="px-3 py-2.5 text-right font-semibold text-slate-600">Precio Destino</th>
                  <th scope="col" className="px-3 py-2.5 text-left font-semibold text-slate-600">Estado / Error</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {filasAMostrar.map((f) => (
                  <tr key={f.id} className={f.esValida ? 'hover:bg-slate-50/50' : 'bg-rose-50/40 hover:bg-rose-50/60'}>
                    <td className="px-3 py-2 font-mono text-slate-500">#{f.filaOriginal}</td>
                    <td className="px-3 py-2 font-mono font-medium text-slate-800">{f.codigo || '—'}</td>
                    <td className="px-3 py-2 text-slate-700">{f.descripcion}</td>
                    <td className="px-3 py-2 text-right font-mono text-slate-600">${f.costoProveedor.toLocaleString('es-AR')}</td>
                    <td className="px-3 py-2 text-right font-mono font-semibold text-slate-900">${f.precioDestino.toLocaleString('es-AR')}</td>
                    <td className="px-3 py-2">
                      {f.esValida ? (
                        <span className="inline-flex rounded-full bg-emerald-50 px-2 py-0.5 font-semibold text-emerald-700 border border-emerald-200">
                          Válida
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1 rounded bg-rose-100 px-2 py-0.5 font-medium text-rose-800">
                          ⚠ {f.motivoError}
                        </span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="flex justify-between pt-4">
            <button
              type="button"
              onClick={() => setPaso(2)}
              className="rounded-md border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-400"
            >
              ← Revisar perfil
            </button>
            <button
              type="button"
              onClick={() => setPaso(4)}
              className="rounded-md bg-blue-600 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-blue-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600"
            >
              Confirmar e Ir a Exportación →
            </button>
          </div>
        </div>
      )}

      {/* PASO 4: Confirmación y Exportación */}
      {paso === 4 && (
        <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm space-y-6 text-center">
          <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-emerald-100 text-emerald-700 text-xl font-bold">
            ✓
          </div>

          <div className="max-w-md mx-auto">
            <h2 className="text-lg font-semibold text-slate-900">¡Transformación completada con éxito!</h2>
            <p className="mt-1 text-xs text-slate-500">
              Se procesaron {filas.length} registros del archivo. Las {filasValidas.length} filas válidas están listas para ser ingresadas a tu sistema de ventas.
            </p>
          </div>

          <div className="max-w-lg mx-auto grid grid-cols-1 sm:grid-cols-2 gap-4 text-left">
            <div className="rounded-lg border border-emerald-200 bg-emerald-50/50 p-4">
              <p className="text-xs font-semibold text-emerald-800">Archivo Destino</p>
              <p className="mt-1 text-sm font-medium text-slate-800">LISTA_FINAL_SISTEMA.XLSX</p>
              <p className="text-xs text-slate-500 mt-1">{filasValidas.length} artículos transformados</p>
              <button
                type="button"
                onClick={() => alert('Simulando descarga de LISTA_FINAL_SISTEMA.XLSX')}
                className="mt-3 w-full rounded bg-emerald-700 px-3 py-1.5 text-xs font-semibold text-white hover:bg-emerald-800 focus-visible:ring-2 focus-visible:ring-emerald-600"
              >
                Descargar Excel Destino
              </button>
            </div>

            <div className="rounded-lg border border-rose-200 bg-rose-50/50 p-4">
              <p className="text-xs font-semibold text-rose-800">Reporte de Errores</p>
              <p className="mt-1 text-sm font-medium text-slate-800">ERRORES_IMPORTACION.CSV</p>
              <p className="text-xs text-slate-500 mt-1">{filasError.length} filas con inconsistencias</p>
              <button
                type="button"
                onClick={() => alert('Simulando descarga de ERRORES_IMPORTACION.CSV')}
                className="mt-3 w-full rounded bg-rose-700 px-3 py-1.5 text-xs font-semibold text-white hover:bg-rose-800 focus-visible:ring-2 focus-visible:ring-rose-600"
              >
                Descargar Reporte (.csv)
              </button>
            </div>
          </div>

          <div className="pt-4 border-t border-slate-100">
            <button
              type="button"
              onClick={() => {
                setPaso(1);
                setFiltroVista('todas');
              }}
              className="text-xs font-semibold text-blue-700 hover:underline focus-visible:ring-2 focus-visible:ring-blue-600 rounded px-2 py-1"
            >
              ← Comenzar una nueva importación
            </button>
          </div>
        </div>
      )}
    </div>
  );
}