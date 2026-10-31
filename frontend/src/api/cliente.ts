import type { Perfil, PerfilResumen, ResultadoImportacion } from './tipos';

/** URL del backend. En Render se define VITE_API_URL al construir el sitio. */
const API_URL = (import.meta.env.VITE_API_URL ?? 'http://localhost:8080').replace(/\/$/, '');

/** Error de la API con el mensaje listo para mostrar al usuario. */
export class ErrorApi extends Error {}

async function pedir<T>(ruta: string, opciones?: RequestInit): Promise<T> {
  let respuesta: Response;
  try {
    respuesta = await fetch(API_URL + ruta, opciones);
  } catch {
    throw new ErrorApi(`No se pudo conectar con el servidor (${API_URL}). ¿Está corriendo el backend?`);
  }
  if (!respuesta.ok) {
    // Los errores llegan como ProblemDetail: { title, detail, status }
    const problema = await respuesta.json().catch(() => null);
    throw new ErrorApi(problema?.detail ?? `El servidor respondió ${respuesta.status}`);
  }
  return respuesta.json() as Promise<T>;
}

export function obtenerPerfiles(): Promise<PerfilResumen[]> {
  return pedir('/api/perfiles');
}

export function obtenerPerfil(id: string): Promise<Perfil> {
  return pedir(`/api/perfiles/${encodeURIComponent(id)}`);
}

export function previsualizar(perfilId: string, archivo: File): Promise<ResultadoImportacion> {
  const formulario = new FormData();
  formulario.append('perfil', perfilId);
  formulario.append('archivo', archivo);
  return pedir('/api/importaciones/preview', { method: 'POST', body: formulario });
}
