import { useEffect, useState } from 'react';
import { borrarProveedor, crearProveedor, editarProveedor, obtenerProveedores } from '../../api/cliente';
import type { Proveedor } from '../../api/tipos';
import Modal from '../../components/ui/Modal';

interface ProveedoresProps {
  /** Solo un administrador crea, edita o borra proveedores (la API lo controla igual). */
  puedeEditar: boolean;
}

export default function Proveedores({ puedeEditar }: ProveedoresProps) {
  const [proveedores, setProveedores] = useState<Proveedor[]>([]);
  const [cargando, setCargando] = useState(true);
  const [errorLista, setErrorLista] = useState<string | null>(null);

  const [modalAbierto, setModalAbierto] = useState(false);
  const [proveedorEditando, setProveedorEditando] = useState<Proveedor | null>(null);
  const [nombre, setNombre] = useState('');
  const [activo, setActivo] = useState(true);
  const [errorForm, setErrorForm] = useState<string | null>(null);
  const [guardando, setGuardando] = useState(false);

  function cargar() {
    setCargando(true);
    obtenerProveedores()
      .then((lista) => {
        setProveedores(lista);
        setErrorLista(null);
      })
      .catch((e: Error) => setErrorLista(e.message))
      .finally(() => setCargando(false));
  }

  useEffect(cargar, []);

  function abrirModal(p: Proveedor | null) {
    setProveedorEditando(p);
    setNombre(p?.nombre ?? '');
    setActivo(p?.activo ?? true);
    setErrorForm(null);
    setModalAbierto(true);
  }

  /** Ejecuta un cambio contra la API; si sale bien, cierra el modal y recarga la lista. */
  async function ejecutar(accion: () => Promise<unknown>) {
    setGuardando(true);
    setErrorForm(null);
    try {
      await accion();
      setModalAbierto(false);
      cargar();
    } catch (e) {
      setErrorForm((e as Error).message);
    } finally {
      setGuardando(false);
    }
  }

  function guardar(e: React.FormEvent) {
    e.preventDefault();
    if (!nombre.trim()) {
      setErrorForm('El nombre es obligatorio');
      return;
    }
    const datos = { nombre: nombre.trim(), activo };
    ejecutar(() => (proveedorEditando ? editarProveedor(proveedorEditando.id, datos) : crearProveedor(datos)));
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-base font-semibold text-slate-900">Listado de Proveedores</h2>
          <p className="text-sm text-slate-500">Proveedores que envían listas de precios. Cada lista se configura en Perfiles.</p>
        </div>
        {puedeEditar && (
          <button
            type="button"
            onClick={() => abrirModal(null)}
            className="rounded-md bg-blue-600 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-blue-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600"
          >
            + Nuevo Proveedor
          </button>
        )}
      </div>

      {errorLista && (
        <p role="alert" className="rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-700">{errorLista}</p>
      )}

      <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white shadow-sm">
        <table className="min-w-full divide-y divide-slate-200 text-sm">
          <thead className="bg-slate-50">
            <tr>
              <th scope="col" className="px-4 py-3 text-left font-medium text-slate-600">Nombre</th>
              <th scope="col" className="px-4 py-3 text-right font-medium text-slate-600">Perfiles</th>
              <th scope="col" className="px-4 py-3 text-left font-medium text-slate-600">Estado</th>
              {puedeEditar && <th scope="col" className="px-4 py-3 text-right font-medium text-slate-600">Acción</th>}
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {cargando && proveedores.length === 0 && (
              <tr><td colSpan={4} className="px-4 py-6 text-center text-slate-500">Cargando…</td></tr>
            )}
            {proveedores.map((p) => (
              <tr key={p.id} className="hover:bg-slate-50/60">
                <td className="px-4 py-3 font-semibold text-slate-900">{p.nombre}</td>
                <td className="px-4 py-3 text-right text-slate-600">{p.perfiles}</td>
                <td className="px-4 py-3">
                  <span
                    className={`inline-flex rounded-full px-2 py-0.5 text-xs font-semibold ${
                      p.activo
                        ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                        : 'bg-slate-100 text-slate-600 border border-slate-200'
                    }`}
                  >
                    {p.activo ? 'Activo' : 'Inactivo'}
                  </span>
                </td>
                {puedeEditar && (
                  <td className="px-4 py-3 text-right">
                    <button
                      type="button"
                      onClick={() => abrirModal(p)}
                      className="font-medium text-blue-700 hover:text-blue-900 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600 rounded px-1"
                    >
                      Editar
                    </button>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <Modal abierto={modalAbierto} alCerrar={() => setModalAbierto(false)} tituloId="modal-proveedor-titulo" anchoMaximo="max-w-md">
        <h3 id="modal-proveedor-titulo" className="text-lg font-semibold text-slate-900">
          {proveedorEditando ? 'Editar Proveedor' : 'Registrar Nuevo Proveedor'}
        </h3>
        <p className="mt-1 text-xs text-slate-500">El formato de cada lista se configura después, en un perfil por hoja.</p>

        <form onSubmit={guardar} className="mt-4 space-y-4">
          <div>
            <label htmlFor="input-nombre" className="block text-xs font-semibold text-slate-700">Nombre *</label>
            <input
              id="input-nombre"
              type="text"
              maxLength={120}
              value={nombre}
              onChange={(e) => setNombre(e.target.value)}
              className="mt-1 block w-full rounded-md border border-slate-300 px-3 py-2 text-sm shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600"
              placeholder="ej. Paint Roller"
            />
          </div>

          <label className="flex items-center gap-2 text-sm text-slate-700">
            <input type="checkbox" checked={activo} onChange={(e) => setActivo(e.target.checked)} className="h-4 w-4" />
            Activo
          </label>

          {errorForm && (
            <p role="alert" className="rounded-md border border-red-200 bg-red-50 p-2 text-xs text-red-700">{errorForm}</p>
          )}

          <div className="mt-6 flex items-center gap-3 pt-2">
            {proveedorEditando && proveedorEditando.perfiles === 0 && (
              <button
                type="button"
                disabled={guardando}
                onClick={() => ejecutar(() => borrarProveedor(proveedorEditando.id))}
                className="text-sm font-medium text-rose-700 hover:underline disabled:opacity-50"
              >
                Borrar
              </button>
            )}
            <div className="ml-auto flex gap-3">
              <button
                type="button"
                onClick={() => setModalAbierto(false)}
                className="rounded-md border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-400"
              >
                Cancelar
              </button>
              <button
                type="submit"
                disabled={guardando}
                className="rounded-md bg-blue-600 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-blue-700 disabled:opacity-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600"
              >
                {guardando ? 'Guardando…' : 'Guardar'}
              </button>
            </div>
          </div>
        </form>
      </Modal>
    </div>
  );
}
