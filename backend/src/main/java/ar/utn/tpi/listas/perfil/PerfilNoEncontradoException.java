package ar.utn.tpi.listas.perfil;

public class PerfilNoEncontradoException extends RuntimeException {

    public PerfilNoEncontradoException(String id) {
        super("No existe el perfil '" + id + "'");
    }
}
