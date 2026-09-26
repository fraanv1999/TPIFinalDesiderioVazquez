import React, { useEffect, useRef, useState } from 'react';
import type { SeccionApp } from '../../types';

interface ItemNav {
  id: SeccionApp;
  etiqueta: string;
  icono: React.ReactNode;
}

function IconDashboard() {
  return (
    <svg viewBox="0 0 20 20" fill="none" aria-hidden="true" className="h-5 w-5">
      <rect x="2.5" y="2.5" width="6" height="6" rx="1" stroke="currentColor" strokeWidth="1.5" />
      <rect x="11.5" y="2.5" width="6" height="9" rx="1" stroke="currentColor" strokeWidth="1.5" />
      <rect x="2.5" y="11.5" width="6" height="6" rx="1" stroke="currentColor" strokeWidth="1.5" />
      <rect x="11.5" y="14.5" width="6" height="3" rx="1" stroke="currentColor" strokeWidth="1.5" />
    </svg>
  );
}

function IconProveedores() {
  return (
    <svg viewBox="0 0 20 20" fill="none" aria-hidden="true" className="h-5 w-5">
      <path d="M3 7.5 4.2 3h11.6L17 7.5" stroke="currentColor" strokeWidth="1.5" strokeLinejoin="round" />
      <path d="M3 7.5v8.5a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1V7.5" stroke="currentColor" strokeWidth="1.5" strokeLinejoin="round" />
      <path d="M7.5 17V12a1 1 0 0 1 1-1h3a1 1 0 0 1 1 1v5" stroke="currentColor" strokeWidth="1.5" strokeLinejoin="round" />
      <path d="M3 7.5h14" stroke="currentColor" strokeWidth="1.5" />
    </svg>
  );
}

function IconPerfiles() {
  return (
    <svg viewBox="0 0 20 20" fill="none" aria-hidden="true" className="h-5 w-5">
      <rect x="3" y="2.5" width="14" height="15" rx="1.5" stroke="currentColor" strokeWidth="1.5" />
      <path d="M6.5 6.5h7M6.5 10h7M6.5 13.5h4" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
    </svg>
  );
}

function IconAsistente() {
  return (
    <svg viewBox="0 0 20 20" fill="none" aria-hidden="true" className="h-5 w-5">
      <path d="M10 3v3.2M10 13.8V17M3 10h3.2M13.8 10H17" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
      <circle cx="10" cy="10" r="3.3" stroke="currentColor" strokeWidth="1.5" />
    </svg>
  );
}

function IconMenu() {
  return (
    <svg viewBox="0 0 20 20" fill="none" aria-hidden="true" className="h-6 w-6">
      <path d="M3 5.5h14M3 10h14M3 14.5h14" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
    </svg>
  );
}

function IconClose() {
  return (
    <svg viewBox="0 0 20 20" fill="none" aria-hidden="true" className="h-6 w-6">
      <path d="M5 5l10 10M15 5 5 15" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
    </svg>
  );
}

const ITEMS_NAV: ItemNav[] = [
  { id: 'dashboard', etiqueta: 'Dashboard', icono: <IconDashboard /> },
  { id: 'proveedores', etiqueta: 'Proveedores', icono: <IconProveedores /> },
  { id: 'perfiles', etiqueta: 'Perfiles de importación', icono: <IconPerfiles /> },
  { id: 'asistente', etiqueta: 'Asistente de importación', icono: <IconAsistente /> },
];

interface LayoutProps {
  seccionActual: SeccionApp;
  onCambiarSeccion: (seccion: SeccionApp) => void;
  children: React.ReactNode;
}

function ContenidoNav({
  seccionActual,
  onSeleccionar,
}: {
  seccionActual: SeccionApp;
  onSeleccionar: (seccion: SeccionApp) => void;
}) {
  return (
    <nav aria-label="Secciones principales" className="flex flex-col gap-1 p-3">
      {ITEMS_NAV.map((item) => {
        const activo = item.id === seccionActual;
        return (
          <button
            key={item.id}
            type="button"
            onClick={() => onSeleccionar(item.id)}
            aria-current={activo ? 'page' : undefined}
            className={
              'flex items-center gap-3 rounded-md px-3 py-2.5 text-sm font-medium transition-colors ' +
              'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-2 focus-visible:ring-offset-slate-900 ' +
              (activo
                ? 'bg-slate-700 text-white'
                : 'text-slate-300 hover:bg-slate-700/60 hover:text-white')
            }
          >
            <span className={activo ? 'text-blue-400' : 'text-slate-400'}>{item.icono}</span>
            {item.etiqueta}
          </button>
        );
      })}
    </nav>
  );
}

const SELECTOR_FOCUSABLE =
  'a[href], button:not([disabled]), textarea:not([disabled]), input:not([disabled]), select:not([disabled]), [tabindex]:not([tabindex="-1"])';

export default function Layout({ seccionActual, onCambiarSeccion, children }: LayoutProps) {
  const [menuAbierto, setMenuAbierto] = useState(false);
  const botonMenuRef = useRef<HTMLButtonElement>(null);
  const drawerRef = useRef<HTMLDivElement>(null);

  // El menú off-canvas es, a efectos de accesibilidad, un diálogo modal:
  // foco inicial dentro, trampa de Tab, Escape cierra y devuelve el foco,
  // y bloquea el scroll del contenido de fondo mientras está abierto.
  useEffect(() => {
    if (!menuAbierto) return;

    document.body.classList.add('modal-abierto');
    const nodo = drawerRef.current;
    const primerFocable = nodo?.querySelector<HTMLElement>(SELECTOR_FOCUSABLE);
    (primerFocable ?? nodo)?.focus();

    function alPresionarTecla(evento: KeyboardEvent) {
      if (evento.key === 'Escape') {
        setMenuAbierto(false);
        return;
      }
      if (evento.key !== 'Tab' || !nodo) return;

      const focables = Array.from(nodo.querySelectorAll<HTMLElement>(SELECTOR_FOCUSABLE)).filter(
        (el) => el.offsetParent !== null
      );
      if (focables.length === 0) return;

      const primero = focables[0];
      const ultimo = focables[focables.length - 1];

      if (evento.shiftKey && document.activeElement === primero) {
        evento.preventDefault();
        ultimo.focus();
      } else if (!evento.shiftKey && document.activeElement === ultimo) {
        evento.preventDefault();
        primero.focus();
      }
    }

    document.addEventListener('keydown', alPresionarTecla);
    return () => {
      document.removeEventListener('keydown', alPresionarTecla);
      document.body.classList.remove('modal-abierto');
      botonMenuRef.current?.focus();
    };
  }, [menuAbierto]);

  function seleccionarSeccion(seccion: SeccionApp) {
    onCambiarSeccion(seccion);
    setMenuAbierto(false);
  }

  const etiquetaActual = ITEMS_NAV.find((item) => item.id === seccionActual)?.etiqueta ?? '';

  return (
    <div className="min-h-screen bg-slate-50">
      <a
        href="#contenido-principal"
        className="sr-only focus:not-sr-only focus:fixed focus:left-4 focus:top-4 focus:z-50 focus:rounded-md focus:bg-white focus:px-4 focus:py-2 focus:text-sm focus:font-medium focus:text-slate-900 focus:shadow-lg focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600"
      >
        Saltar al contenido principal
      </a>

      {/* Sidebar fija en escritorio (md en adelante) */}
      <aside
        aria-label="Navegación principal"
        className="hidden md:fixed md:inset-y-0 md:left-0 md:z-30 md:flex md:w-64 md:flex-col md:bg-slate-800"
      >
        <div className="flex h-16 items-center gap-2 border-b border-slate-700 px-4">
          <span className="flex h-8 w-8 items-center justify-center rounded bg-blue-600 text-sm font-semibold text-white">
            IX
          </span>
          <span className="text-sm font-semibold leading-tight text-white">
            Importador de listas
            <span className="block text-xs font-normal text-slate-400">Panel de gestión</span>
          </span>
        </div>
        <div className="flex-1 overflow-y-auto">
          <ContenidoNav seccionActual={seccionActual} onSeleccionar={seleccionarSeccion} />
        </div>
      </aside>

      {/* Menú off-canvas para pantallas angostas (tablet en portrait) */}
      {menuAbierto && (
        <div className="fixed inset-0 z-40 md:hidden">
          <div
            className="absolute inset-0 bg-slate-900/50"
            onClick={() => setMenuAbierto(false)}
            aria-hidden="true"
          />
          <div
            ref={drawerRef}
            role="dialog"
            aria-modal="true"
            aria-label="Navegación principal"
            tabIndex={-1}
            className="absolute inset-y-0 left-0 flex w-72 max-w-[85%] flex-col bg-slate-800 shadow-xl focus:outline-none"
          >
            <div className="flex h-16 items-center justify-between border-b border-slate-700 px-4">
              <span className="text-sm font-semibold text-white">Importador de listas</span>
              <button
                type="button"
                onClick={() => setMenuAbierto(false)}
                aria-label="Cerrar navegación"
                className="rounded-md p-1.5 text-slate-300 hover:bg-slate-700 hover:text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500"
              >
                <IconClose />
              </button>
            </div>
            <div className="flex-1 overflow-y-auto">
              <ContenidoNav seccionActual={seccionActual} onSeleccionar={seleccionarSeccion} />
            </div>
          </div>
        </div>
      )}

      <div className="md:pl-64">
        <header className="sticky top-0 z-20 flex h-16 items-center gap-3 border-b border-slate-200 bg-white px-4 md:px-6">
          <button
            ref={botonMenuRef}
            type="button"
            onClick={() => setMenuAbierto(true)}
            aria-label="Abrir navegación"
            aria-haspopup="dialog"
            className="rounded-md p-1.5 text-slate-600 hover:bg-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600 md:hidden"
          >
            <IconMenu />
          </button>
          <h1 className="text-base font-semibold text-slate-900 md:text-lg">{etiquetaActual}</h1>
        </header>

        <main id="contenido-principal" className="px-4 py-6 md:px-8 md:py-8">
          <div className="mx-auto max-w-6xl">{children}</div>
        </main>
      </div>
    </div>
  );
}
