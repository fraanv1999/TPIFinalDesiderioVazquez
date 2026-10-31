import { useEffect, useMemo, useState } from 'react';
import { obtenerPerfil, obtenerPerfiles, previsualizar } from '../../api/cliente';
import type { FilaResultado, Perfil, PerfilResumen, ResultadoImportacion } from '../../api/tipos';

type Filtro = 'todas' | 'validas' | 'errores' | 'ignoradas';

const MAXIMO_FILAS_VISIBLES = 500;

function formatear(valor: string | number | null | undefined) {
  if (valor === null || valor === undefined) return '—';
  return typeof valor === 'number' ? valor.toLocaleString('es-AR', { maximumFractionDigits: 2 }) : valor;
}

function pasaFiltro(f: FilaResultado, filtro: Filtro) {
  if (filtro === 'validas') return f.estado === 'VALIDA';
  if (filtro === 'errores') return f.estado === 'ERROR';
  if (filtro === 'ignoradas') return f.estado === 'IGNORADA';
  return true;
}

export default function AsistenteImportacion() {
  const [paso, setPaso] = useState<1 | 2 | 3 | 4>(1);

  // Estados del wizard
  const [archivo, setArchivo] = useState<File | null>(null);
  const [perfiles, setPerfiles] = useState<PerfilResumen[]>([]);
  const [proveedor, setProveedor] = useState('');
  const [perfilId, setPerfilId] = useState('');
  const [perfil, setPerfil] = useState<Perfil | null>(null);
  const [resultado, setResultado] = useState<ResultadoImportacion | null>(null);
  const [procesando, setProcesando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [filtroVista, setFiltroVista] = useState<Filtro>('todas');

  useEffect(() => {
    obtenerPerfiles()
      .then((lista) => {
        setPerfiles(lista);
        if (lista.length > 0) {
          setProveedor(lista[0].proveedor);
          setPerfilId(lista[0].id);
        }
      })
      .catch((e: Error) => setError(e.message));
  }, []);

  useEffect(() => {
    if (!perfilId) return;
    obtenerPerfil(perfilId)
      .then(setPerfil)
      .catch((e: Error) => setError(e.message));
  }, [perfilId]);

  const proveedores = useMemo(() => [...new Set(perfiles.map((p) => p.proveedor))], [perfiles]);
  const perfilesDelProveedor = perfiles.filter((p) => p.proveedor === proveedor);

  const filas = resultado?.filas ?? [];
  const filasFiltradas = filas.filter((f) => pasaFiltro(f, filtroVista));
  const avisos = resultado?.diferencias.filter((d) => d.bloqueante === false) ?? [];
  const repetidos = resultado?.repetidos ?? [];
  // Columnas del resultado: las del formato destino, en el orden en que las devuelve la API.
  const columnas = Object.keys(filas.find((f) => f.datosResultado)?.datosResultado ?? {});

  async function procesar() {
    if (!archivo || !perfilId) return;
    setProcesando(true);
    setError(null);
    try {
      setResultado(await previsualizar(perfilId, archivo));
      setFiltroVista('todas');
      setPaso(3);
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setProcesando(false);
    }
  }

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

      {error && (
        <p role="alert" className="rounded-md border border-rose-200 bg-rose-50 p-3 text-sm text-rose-800">{error}</p>
      )}

      {/* PASO 1: Subida de archivo */}
      {paso === 1 && (
        <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm space-y-4">
          <div>
            <h2 className="text-base font-semibold text-slate-900">Paso 1: Selecciona la lista de precios del proveedor</h2>
            <p className="text-xs text-slate-500">Formatos soportados: Excel (.xlsx o .xls), hasta 10 MB.</p>
          </div>

          <label
            htmlFor="archivo"
            className="flex cursor-pointer flex-col items-center justify-center rounded-lg border-2 border-dashed border-slate-300 p-8 bg-slate-50/50 hover:bg-slate-50 focus-within:ring-2 focus-within:ring-blue-600"
          >
            <span className="text-4xl text-slate-400">📄</span>
            {archivo ? (
              <>
                <p className="mt-2 text-sm font-medium text-slate-700">Archivo seleccionado:</p>
                <span className="mt-1 rounded bg-blue-100 px-3 py-1 font-mono text-xs font-semibold text-blue-900">
                  {archivo.name} ({(archivo.size / 1024).toLocaleString('es-AR', { maximumFractionDigits: 1 })} KB)
                </span>
                <span className="mt-3 text-xs font-medium text-blue-700 underline">Elegir otro archivo</span>
              </>
            ) : (
              <p className="mt-2 text-sm font-medium text-slate-700">Hacé clic para elegir el Excel del proveedor</p>
            )}
            <input
              id="archivo"
              type="file"
              accept=".xlsx,.xls"
              className="sr-only"
              onChange={(e) => {
                setArchivo(e.target.files?.[0] ?? null);
                setResultado(null);
              }}
            />
          </label>

          <div className="flex justify-end pt-4">
            <button
              type="button"
              disabled={!archivo}
              onClick={() => setPaso(2)}
              className="rounded-md bg-blue-600 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-slate-300 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600"
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
            <p className="text-xs text-slate-500">El perfil determina qué hoja leer, qué encabezados esperar y qué cálculos aplicar.</p>
          </div>

          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 pt-2">
            <div>
              <label htmlFor="select-prov" className="block text-xs font-semibold text-slate-700">Proveedor</label>
              <select
                id="select-prov"
                value={proveedor}
                onChange={(e) => {
                  setProveedor(e.target.value);
                  setPerfilId(perfiles.find((p) => p.proveedor === e.target.value)?.id ?? '');
                }}
                className="mt-1 block w-full rounded-md border border-slate-300 bg-white p-2 text-sm shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600"
              >
                {proveedores.map((p) => (
                  <option key={p} value={p}>{p}</option>
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
                {perfilesDelProveedor.map((p) => (
                  <option key={p.id} value={p.id}>{p.nombre}</option>
                ))}
              </select>
            </div>
          </div>

          {perfil && perfil.id === perfilId && (
            <div className="rounded-md bg-slate-50 border border-slate-200 p-4 text-xs text-slate-600 space-y-1">
              <p className="font-semibold text-slate-800">Reglas que se aplicarán automáticamente:</p>
              <p>
                • Hoja <span className="font-mono text-slate-800 whitespace-pre">"{perfil.hoja}"</span>, encabezados en la fila{' '}
                {perfil.filaEncabezado}
                {perfil.margenEncabezado ? ` (±${perfil.margenEncabezado})` : ''} y datos desde la fila {perfil.filaInicioDatos}.
              </p>
              <p>
                • Encabezados esperados:{' '}
                <span className="font-mono text-slate-800">{perfil.columnas.map((c) => `${c.columna}=${c.encabezado}`).join(' · ')}</span>
              </p>
              <p>• {perfil.reglas.length} reglas de cálculo, en orden: {perfil.reglas.map((r) => r.resultado).join(' → ') || 'ninguna'}.</p>
              <p>• Campo clave: <span className="font-mono text-slate-800">{perfil.campoClave}</span>. Una fila de producto sin él es un error.</p>
            </div>
          )}

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
              disabled={!perfilId || procesando}
              onClick={procesar}
              className="rounded-md bg-blue-600 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-slate-300 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600"
            >
              {procesando ? 'Procesando…' : 'Procesar y Ver Vista Previa →'}
            </button>
          </div>
        </div>
      )}

      {/* PASO 3: Estructura rechazada */}
      {paso === 3 && resultado?.estado === 'RECHAZADA' && (
        <div className="rounded-lg border border-rose-200 bg-white p-6 shadow-sm space-y-4">
          <div>
            <h2 className="text-base font-semibold text-rose-800">La estructura del archivo no coincide con el perfil</h2>
            <p className="text-xs text-slate-500">
              No se procesó ninguna fila. Revisá si el proveedor cambió la lista o si elegiste otro perfil.
            </p>
          </div>
          <ul className="space-y-2 text-sm">
            {resultado.diferencias.map((d, i) => (
              <li key={i} className="rounded border border-rose-100 bg-rose-50/50 p-3">
                <span className="font-mono text-xs font-semibold text-rose-800">{d.tipo}</span>
                <p className="text-slate-700">{d.mensaje}</p>
              </li>
            ))}
          </ul>
          <div className="flex justify-start pt-4">
            <button
              type="button"
              onClick={() => setPaso(2)}
              className="rounded-md border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-400"
            >
              ← Revisar perfil
            </button>
          </div>
        </div>
      )}

      {/* PASO 3: Vista previa y clasificación */}
      {paso === 3 && resultado?.estado === 'PREVIEW' && (
        <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm space-y-4">
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
            <div>
              <h2 className="text-base font-semibold text-slate-900">Paso 3: Vista Previa y Clasificación</h2>
              <p className="text-xs text-slate-500">
                {resultado.archivo}: {resultado.resumen.vacias} filas vacías y {resultado.resumen.titulos} títulos ignorados. Las
                filas con error se informan y no pasan al archivo final.
              </p>
            </div>

            {/* Selector de filtros accesible */}
            <div className="flex flex-wrap rounded-md shadow-sm border border-slate-200 p-0.5 bg-slate-50 text-xs">
              {(
                [
                  ['todas', `Todas (${filas.length})`, 'text-slate-900'],
                  ['validas', `Válidas (${resultado.resumen.validas})`, 'text-emerald-800'],
                  ['errores', `Con Errores (${resultado.resumen.errores})`, 'text-rose-800'],
                  ['ignoradas', `Ignoradas (${resultado.resumen.ignoradas})`, 'text-slate-700'],
                ] as const
              ).map(([valor, texto, color]) => (
                <button
                  key={valor}
                  type="button"
                  aria-pressed={filtroVista === valor}
                  onClick={() => setFiltroVista(valor)}
                  className={`rounded px-2.5 py-1 font-medium ${filtroVista === valor ? `bg-white shadow-sm font-semibold ${color}` : 'text-slate-600'}`}
                >
                  {texto}
                </button>
              ))}
            </div>
          </div>

          {(avisos.length > 0 || repetidos.length > 0) && (
            <div className="rounded-md border border-amber-200 bg-amber-50 p-3 text-xs text-amber-900 space-y-1">
              <p className="font-semibold">Avisos (no bloquean la importación)</p>
              {avisos.map((d, i) => (
                <p key={i}>• {d.mensaje}</p>
              ))}
              {repetidos.map((r) => (
                <p key={r.codigo}>
                  • Código <span className="font-mono">{r.codigo}</span> repetido en {r.filas.length} filas ({r.filas.slice(0, 8).join(', ')}
                  {r.filas.length > 8 ? '…' : ''})
                </p>
              ))}
            </div>
          )}

          <div className="overflow-x-auto rounded-lg border border-slate-200">
            <table className="min-w-full divide-y divide-slate-200 text-xs">
              <thead className="bg-slate-50">
                <tr>
                  <th scope="col" className="px-3 py-2.5 text-left font-semibold text-slate-600">Fila Excel</th>
                  <th scope="col" className="px-3 py-2.5 text-left font-semibold text-slate-600">Estado</th>
                  {columnas.map((c) => (
                    <th key={c} scope="col" className="px-3 py-2.5 text-left font-semibold text-slate-600 whitespace-nowrap">{c}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {filasFiltradas.slice(0, MAXIMO_FILAS_VISIBLES).map((f) => (
                  <tr
                    key={f.numeroFila}
                    className={
                      f.estado === 'ERROR' ? 'bg-rose-50/40 hover:bg-rose-50/60' : f.estado === 'IGNORADA' ? 'text-slate-400' : 'hover:bg-slate-50/50'
                    }
                  >
                    <td className="px-3 py-2 font-mono text-slate-500">#{f.numeroFila}</td>
                    <td className="px-3 py-2 whitespace-nowrap">
                      {f.estado === 'VALIDA' && (
                        <span className="inline-flex rounded-full bg-emerald-50 px-2 py-0.5 font-semibold text-emerald-700 border border-emerald-200">
                          Válida
                        </span>
                      )}
                      {f.estado === 'ERROR' && (
                        <span className="inline-flex items-center gap-1 rounded bg-rose-100 px-2 py-0.5 font-medium text-rose-800">
                          ⚠ {f.errores.map((e) => e.mensaje).join(' · ')}
                        </span>
                      )}
                      {f.estado === 'IGNORADA' && (
                        <span className="inline-flex rounded-full bg-slate-100 px-2 py-0.5 font-medium text-slate-600">
                          {f.motivoIgnorada === 'TITULO' ? 'Título' : 'Vacía'}
                        </span>
                      )}
                    </td>
                    {f.datosResultado ? (
                      columnas.map((c) => (
                        <td key={c} className={`px-3 py-2 ${typeof f.datosResultado?.[c] === 'number' ? 'text-right font-mono' : ''}`}>
                          {formatear(f.datosResultado?.[c])}
                        </td>
                      ))
                    ) : (
                      <td colSpan={Math.max(columnas.length, 1)} className="px-3 py-2 font-mono text-slate-500">
                        {Object.entries(f.datosCrudos)
                          .map(([col, v]) => `${col}: ${formatear(v)}`)
                          .join(' · ') || '—'}
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {filasFiltradas.length > MAXIMO_FILAS_VISIBLES && (
            <p className="text-xs text-slate-500">
              Se muestran las primeras {MAXIMO_FILAS_VISIBLES} de {filasFiltradas.length} filas. Usá los filtros para ver el resto.
            </p>
          )}

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
      {paso === 4 && resultado && (
        <div className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm space-y-6 text-center">
          <div className="max-w-md mx-auto">
            <h2 className="text-lg font-semibold text-slate-900">Resumen de la importación</h2>
            <p className="mt-1 text-xs text-slate-500">
              Se leyeron {filas.length} filas de {resultado.archivo}. {resultado.resumen.validas} filas válidas quedan listas para el
              sistema destino.
            </p>
          </div>

          <div className="max-w-lg mx-auto grid grid-cols-1 sm:grid-cols-2 gap-4 text-left">
            <div className="rounded-lg border border-emerald-200 bg-emerald-50/50 p-4">
              <p className="text-xs font-semibold text-emerald-800">Archivo Destino</p>
              <p className="text-xs text-slate-500 mt-1">{resultado.resumen.validas} artículos transformados</p>
              <button
                type="button"
                disabled
                title="La exportación todavía no está disponible en la API"
                className="mt-3 w-full rounded bg-slate-300 px-3 py-1.5 text-xs font-semibold text-white cursor-not-allowed"
              >
                Descargar Excel Destino (próximamente)
              </button>
            </div>

            <div className="rounded-lg border border-rose-200 bg-rose-50/50 p-4">
              <p className="text-xs font-semibold text-rose-800">Reporte de Errores</p>
              <p className="text-xs text-slate-500 mt-1">{resultado.resumen.errores} filas con inconsistencias</p>
              <button
                type="button"
                disabled
                title="La exportación todavía no está disponible en la API"
                className="mt-3 w-full rounded bg-slate-300 px-3 py-1.5 text-xs font-semibold text-white cursor-not-allowed"
              >
                Descargar Reporte (próximamente)
              </button>
            </div>
          </div>

          <div className="pt-4 border-t border-slate-100">
            <button
              type="button"
              onClick={() => {
                setPaso(1);
                setArchivo(null);
                setResultado(null);
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
