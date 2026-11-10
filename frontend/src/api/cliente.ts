import type { Perfil, PerfilResumen, ResultadoImportacion, Usuario } from './tipos';
import { autorizacion, autorizacionBasic, cerrarSesion, guardarSesion } from './sesion';

/** URL del backend. En Render se define VITE_API_URL al construir el sitio. */
const API_URL = (import.meta.env.VITE_API_URL ?? 'http://localhost:8080').replace(/\/$/, '');

/** Error de la API con el mensaje listo para mostrar al usuario. */
export class ErrorApi extends Error {}

async function enviar(ruta: string, opciones: RequestInit = {}, auth = autorizacion()): Promise<Response> {
  const headers = new Headers(opciones.headers);
  if (auth) headers.set('Authorization', auth);
  let respuesta: Response;
  try {
    respuesta = await fetch(API_URL + ruta, { ...opciones, headers });
  } catch {
    throw new ErrorApi(`No se pudo conectar con el servidor (${API_URL}). ¿Está corriendo el backend?`);
  }
  if (!respuesta.ok) {
    // Los errores llegan como ProblemDetail: { title, detail, status }
    const problema = await respuesta.json().catch(() => null);
    if (respuesta.status === 401 && auth === autorizacion()) {
      cerrarSesion();
    }
    throw new ErrorApi(problema?.detail ?? `El servidor respondió ${respuesta.status}`);
  }
  return respuesta;
}

async function pedir<T>(ruta: string, opciones?: RequestInit): Promise<T> {
  const respuesta = await enviar(ruta, opciones);
  return respuesta.status === 204 ? (undefined as T) : (respuesta.json() as Promise<T>);
}

/** Valida las credenciales contra la API y, si son correctas, guarda la sesión. */
export async function iniciarSesion(email: string, contrasena: string): Promise<Usuario> {
  const auth = autorizacionBasic(email.trim(), contrasena);
  const respuesta = await enviar('/api/auth/yo', {}, auth);
  const usuario = (await respuesta.json()) as Usuario;
  guardarSesion(auth, usuario);
  return usuario;
}

export function obtenerPerfiles(): Promise<PerfilResumen[]> {
  return pedir('/api/perfiles');
}

export function obtenerPerfil(id: number): Promise<Perfil> {
  return pedir(`/api/perfiles/${id}`);
}

export function previsualizar(perfilId: number, archivo: File): Promise<ResultadoImportacion> {
  const formulario = new FormData();
  formulario.append('perfil', String(perfilId));
  formulario.append('archivo', archivo);
  return pedir('/api/importaciones/preview', { method: 'POST', body: formulario });
}
