import { useState } from 'react';
import type { SeccionApp } from '../types';
import Layout from '../components/layout/Layout';
import Dashboard from '../features/dashboard/Dashboard';
import Proveedores from '../features/proveedores/Proveedores';
import PerfilesImportacion from '../features/perfiles/PerfilesImportacion';
import AsistenteImportacion from '../features/asistente/AsistenteImportacion';

export default function App() {
  const [seccionActual, setSeccionActual] = useState<SeccionApp>('dashboard');

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
    <Layout seccionActual={seccionActual} onCambiarSeccion={setSeccionActual}>
      {renderizarSeccion()}
    </Layout>
  );
}