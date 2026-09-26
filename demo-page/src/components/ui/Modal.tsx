import { useEffect, useRef, type ReactNode } from 'react';

interface ModalProps {
  abierto: boolean;
  alCerrar: () => void;
  tituloId: string;
  descripcionId?: string;
  anchoMaximo?: string; // clase Tailwind, ej. 'max-w-md' | 'max-w-2xl'
  children: ReactNode;
}

const SELECTOR_FOCUSABLE =
  'a[href], button:not([disabled]), textarea:not([disabled]), input:not([disabled]), select:not([disabled]), [tabindex]:not([tabindex="-1"])';

export default function Modal({
  abierto,
  alCerrar,
  tituloId,
  descripcionId,
  anchoMaximo = 'max-w-md',
  children,
}: ModalProps) {
  const contenedorRef = useRef<HTMLDivElement>(null);
  const elementoPrevioRef = useRef<HTMLElement | null>(null);
  const alCerrarRef = useRef(alCerrar);
  alCerrarRef.current = alCerrar;

  useEffect(() => {
    if (!abierto) return;

    // Guarda qué elemento tenía el foco para devolvérselo al cerrar.
    elementoPrevioRef.current = document.activeElement as HTMLElement | null;
    document.body.classList.add('modal-abierto');

    const nodo = contenedorRef.current;
    const primerFocable = nodo?.querySelector<HTMLElement>(SELECTOR_FOCUSABLE);
    (primerFocable ?? nodo)?.focus();

    function alPresionarTecla(evento: KeyboardEvent) {
      if (evento.key === 'Escape') {
        alCerrarRef.current();
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
      elementoPrevioRef.current?.focus();
    };
  }, [abierto]);

  if (!abierto) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="fixed inset-0 bg-slate-900/50" onClick={alCerrar} aria-hidden="true" />
      <div
        ref={contenedorRef}
        role="dialog"
        aria-modal="true"
        aria-labelledby={tituloId}
        aria-describedby={descripcionId}
        tabIndex={-1}
        className={`relative w-full ${anchoMaximo} rounded-lg border border-slate-200 bg-white p-6 shadow-lg focus:outline-none`}
      >
        {children}
      </div>
    </div>
  );
}
