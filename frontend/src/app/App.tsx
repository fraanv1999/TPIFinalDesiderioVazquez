import { useEffect, useState } from 'react';
import type { SeccionApp } from '../types';
import type { Usuario } from '../api/tipos';
import { EVENTO_SESION_CERRADA, cerrarSesion, usuario as usuarioGuardado } from '../api/sesion';
import Layout from '../components/layout/Layout';
import Login from '../features/login/Login';
import Dashboard from '../features/dashboard/Dashboard';
import Proveedores from '../features/proveedores/Proveedores';
import PerfilesImportacion from '../features/perfiles/PerfilesImportacion';
import AsistenteImportacion from '../features/asistente/AsistenteImportacion';

export default function App() {
  const [usuario, setUsuario] = useState<Usuario | null>(usuarioGuardado);
  const [seccionActual, setSeccionActual] = useState<SeccionApp>('dashboard');

  // Si la API responde 401 (por ejemplo, cambió la contraseña) se vuelve al login.
  useEffect(() => {
    const alCerrar = () => setUsuario(null);
    window.addEventListener(EVENTO_SESION_CERRADA, alCerrar);
    return () => window.removeEventListener(EVENTO_SESION_CERRADA, alCerrar);
  }, []);

  if (!usuario) {
    return <Login onIngresar={setUsuario} />;
  }

  function renderizarSeccion() {
    switch (seccionActual) {
      case 'dashboard':
        return <Dashboard onIrASeccion={setSeccionActual} />;
      case 'proveedores':
        return <Proveedores />;
      case 'perfiles':
        return <PerfilesImportacion />;
      case 'asistente':
        return <AsistenteImportacion />;
      default:
        return null;
    }
  }

  return (
    <Layout
      seccionActual={seccionActual}
      onCambiarSeccion={setSeccionActual}
      usuario={usuario}
      onCerrarSesion={cerrarSesion}
    >
      {renderizarSeccion()}
    </Layout>
  );
}
