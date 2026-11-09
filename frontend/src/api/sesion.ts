// Sesión del usuario. La API usa HTTP Basic: las credenciales viajan en cada pedido.
// Se guardan en sessionStorage, así se borran al cerrar la pestaña.

import type { Usuario } from './tipos';

const CLAVE = 'tpi-sesion';

interface Sesion {
  /** Valor del header Authorization: "Basic base64(email:contraseña)". */
  autorizacion: string;
  usuario: Usuario;
}

/** Codifica en UTF-8 antes de base64 (btoa solo acepta Latin-1). */
export function autorizacionBasic(email: string, contrasena: string): string {
  const bytes = new TextEncoder().encode(`${email}:${contrasena}`);
  return 'Basic ' + btoa(String.fromCharCode(...bytes));
}

function leer(): Sesion | null {
  try {
    const texto = sessionStorage.getItem(CLAVE);
    return texto ? (JSON.parse(texto) as Sesion) : null;
  } catch {
    return null;
  }
}

export function sesionActual(): Usuario | null {
  return leer()?.usuario ?? null;
}

export function autorizacionActual(): string | null {
  return leer()?.autorizacion ?? null;
}

export function guardarSesion(autorizacion: string, usuario: Usuario) {
  try {
    sessionStorage.setItem(CLAVE, JSON.stringify({ autorizacion, usuario }));
  } catch {
    // Sin sessionStorage la sesión dura hasta recargar la página.
  }
  sesionEnMemoria = { autorizacion, usuario };
}

export function cerrarSesion() {
  try {
    sessionStorage.removeItem(CLAVE);
  } catch {
    // nada que borrar
  }
  sesionEnMemoria = null;
  window.dispatchEvent(new Event(EVENTO_SESION_CERRADA));
}

/** Se emite al cerrar sesión o cuando la API responde 401 (credenciales que dejaron de valer). */
export const EVENTO_SESION_CERRADA = 'tpi-sesion-cerrada';

// Respaldo si sessionStorage no está disponible.
let sesionEnMemoria: Sesion | null = null;

export function autorizacion(): string | null {
  return autorizacionActual() ?? sesionEnMemoria?.autorizacion ?? null;
}

export function usuario(): Usuario | null {
  return sesionActual() ?? sesionEnMemoria?.usuario ?? null;
}
