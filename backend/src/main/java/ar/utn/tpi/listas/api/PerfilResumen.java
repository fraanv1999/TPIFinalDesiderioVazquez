package ar.utn.tpi.listas.api;

import ar.utn.tpi.listas.perfil.Perfil;

public record PerfilResumen(long id, String proveedor, String nombre, String hoja) {

    static PerfilResumen de(Perfil p) {
        return new PerfilResumen(p.id(), p.proveedor(), p.nombre(), p.hoja());
    }
}
