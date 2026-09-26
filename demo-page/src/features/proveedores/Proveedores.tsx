import { useState } from 'react';
import { PROVEEDORES as DATOS_INICIALES } from '../../data/mockData';
import type { Proveedor, FormatoArchivo, EstadoEntidad } from '../../types';
import Modal from '../../components/ui/Modal';

export default function Proveedores() {
  const [proveedores, setProveedores] = useState<Proveedor[]>(DATOS_INICIALES);
  const [modalAbierto, setModalAbierto] = useState(false);
  const [proveedorEditando, setProveedorEditando] = useState<Proveedor | null>(null);

  // Form local state
  const [nombre, setNombre] = useState('');
  const [cuit, setCuit] = useState('');
  const [contacto, setContacto] = useState('');
  const [formato, setFormato] = useState<FormatoArchivo>('xlsx');
  const [estado, setEstado] = useState<EstadoEntidad>('activo');
  const [errores, setErrores] = useState<{ nombre?: string; cuit?: string }>({});

  function abrirModalNuevo() {
    setProveedorEditando(null);
    setNombre('');
    setCuit('');
    setContacto('');
    setFormato('xlsx');
    setEstado('activo');
    setErrores({});
    setModalAbierto(true);
  }

  function abrirModalEditar(p: Proveedor) {
    setProveedorEditando(p);
    setNombre(p.nombre);
    setCuit(p.cuit);
    setContacto(p.contactoNombre);
    setFormato(p.formatoArchivo);
    setEstado(p.estado);
    setErrores({});
    setModalAbierto(true);
  }

  function cerrarModal() {
    setModalAbierto(false);
    setErrores({});
  }

  function guardarProveedor(e: React.FormEvent) {
    e.preventDefault();
    const nuevosErrores: { nombre?: string; cuit?: string } = {};

    if (!nombre.trim()) nuevosErrores.nombre = 'El nombre de la empresa es obligatorio.';
    if (!cuit.trim()) nuevosErrores.cuit = 'El CUIT es obligatorio (ej. 30-12345678-9).';

    if (Object.keys(nuevosErrores).length > 0) {
      setErrores(nuevosErrores);
      return;
    }

    if (proveedorEditando) {
      setProveedores((prev) =>
        prev.map((p) =>
          p.id === proveedorEditando.id
            ? { ...p, nombre, cuit, contactoNombre: contacto, formatoArchivo: formato, estado }
            : p
        )
      );
    } else {
      const nuevo: Proveedor = {
        id: `prov-${Date.now()}`,
        nombre,
        cuit,
        contactoNombre: contacto || 'Sin asignar',
        email: 'contacto@proveedor.com',
        telefono: '-',
        formatoArchivo: formato,
        estado,
      };
      setProveedores((prev) => [nuevo, ...prev]);
    }

    cerrarModal();
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-base font-semibold text-slate-900">Listado de Proveedores</h2>
          <p className="text-sm text-slate-500">Administra los proveedores registrados y sus formatos de archivo.</p>
        </div>
        <button
          type="button"
          onClick={abrirModalNuevo}
          className="rounded-md bg-blue-600 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-blue-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600"
        >
          + Nuevo Proveedor
        </button>
      </div>

      <div className="overflow-x-auto rounded-lg border border-slate-200 bg-white shadow-sm">
        <table className="min-w-full divide-y divide-slate-200 text-sm">
          <thead className="bg-slate-50">
            <tr>
              <th scope="col" className="px-4 py-3 text-left font-medium text-slate-600">Nombre</th>
              <th scope="col" className="px-4 py-3 text-left font-medium text-slate-600">CUIT</th>
              <th scope="col" className="px-4 py-3 text-left font-medium text-slate-600">Contacto</th>
              <th scope="col" className="px-4 py-3 text-left font-medium text-slate-600">Formato</th>
              <th scope="col" className="px-4 py-3 text-left font-medium text-slate-600">Estado</th>
              <th scope="col" className="px-4 py-3 text-right font-medium text-slate-600">Acción</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {proveedores.map((p) => (
              <tr key={p.id} className="hover:bg-slate-50/60">
                <td className="px-4 py-3 font-semibold text-slate-900">{p.nombre}</td>
                <td className="px-4 py-3 font-mono text-xs text-slate-600">{p.cuit}</td>
                <td className="px-4 py-3 text-slate-600">{p.contactoNombre}</td>
                <td className="px-4 py-3 uppercase text-xs font-semibold text-slate-700">{p.formatoArchivo}</td>
                <td className="px-4 py-3">
                  <span
                    className={`inline-flex rounded-full px-2 py-0.5 text-xs font-semibold ${
                      p.estado === 'activo'
                        ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                        : 'bg-slate-100 text-slate-600 border border-slate-200'
                    }`}
                  >
                    {p.estado === 'activo' ? 'Activo' : 'Inactivo'}
                  </span>
                </td>
                <td className="px-4 py-3 text-right">
                  <button
                    type="button"
                    onClick={() => abrirModalEditar(p)}
                    className="font-medium text-blue-700 hover:text-blue-900 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600 rounded px-1"
                  >
                    Editar
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <Modal abierto={modalAbierto} alCerrar={cerrarModal} tituloId="modal-proveedor-titulo" anchoMaximo="max-w-md">
        <h3 id="modal-proveedor-titulo" className="text-lg font-semibold text-slate-900">
          {proveedorEditando ? 'Editar Proveedor' : 'Registrar Nuevo Proveedor'}
        </h3>
        <p className="mt-1 text-xs text-slate-500">Configura la información básica para mapear sus listas de precios.</p>

        <form onSubmit={guardarProveedor} className="mt-4 space-y-4">
          <div>
            <label htmlFor="input-nombre" className="block text-xs font-semibold text-slate-700">
              Razón Social / Nombre *
            </label>
            <input
              id="input-nombre"
              type="text"
              value={nombre}
              onChange={(e) => setNombre(e.target.value)}
              aria-invalid={Boolean(errores.nombre)}
              aria-describedby={errores.nombre ? 'error-nombre' : undefined}
              className={`mt-1 block w-full rounded-md border px-3 py-2 text-sm shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600 ${
                errores.nombre ? 'border-rose-500 bg-rose-50/30' : 'border-slate-300'
              }`}
              placeholder="ej. Cerrajera Kallay S.A."
            />
            {errores.nombre && (
              <p id="error-nombre" className="mt-1 text-xs text-rose-600">
                {errores.nombre}
              </p>
            )}
          </div>

          <div>
            <label htmlFor="input-cuit" className="block text-xs font-semibold text-slate-700">
              CUIT *
            </label>
            <input
              id="input-cuit"
              type="text"
              value={cuit}
              onChange={(e) => setCuit(e.target.value)}
              aria-invalid={Boolean(errores.cuit)}
              aria-describedby={errores.cuit ? 'error-cuit' : undefined}
              className={`mt-1 block w-full rounded-md border px-3 py-2 text-sm shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600 ${
                errores.cuit ? 'border-rose-500 bg-rose-50/30' : 'border-slate-300'
              }`}
              placeholder="ej. 30-54918234-9"
            />
            {errores.cuit && (
              <p id="error-cuit" className="mt-1 text-xs text-rose-600">
                {errores.cuit}
              </p>
            )}
          </div>

          <div>
            <label htmlFor="input-contacto" className="block text-xs font-semibold text-slate-700">
              Persona de Contacto
            </label>
            <input
              id="input-contacto"
              type="text"
              value={contacto}
              onChange={(e) => setContacto(e.target.value)}
              className="mt-1 block w-full rounded-md border border-slate-300 px-3 py-2 text-sm shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600"
              placeholder="ej. Juan Pérez"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label htmlFor="select-formato" className="block text-xs font-semibold text-slate-700">
                Formato Habitual
              </label>
              <select
                id="select-formato"
                value={formato}
                onChange={(e) => setFormato(e.target.value as FormatoArchivo)}
                className="mt-1 block w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600"
              >
                <option value="xlsx">Excel (.xlsx)</option>
                <option value="csv">Texto plano (.csv)</option>
              </select>
            </div>

            <div>
              <label htmlFor="select-estado" className="block text-xs font-semibold text-slate-700">
                Estado
              </label>
              <select
                id="select-estado"
                value={estado}
                onChange={(e) => setEstado(e.target.value as EstadoEntidad)}
                className="mt-1 block w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600"
              >
                <option value="activo">Activo</option>
                <option value="inactivo">Inactivo</option>
              </select>
            </div>
          </div>

          <div className="mt-6 flex justify-end gap-3 pt-2">
            <button
              type="button"
              onClick={cerrarModal}
              className="rounded-md border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-400"
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="rounded-md bg-blue-600 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-blue-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600"
            >
              Guardar
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
