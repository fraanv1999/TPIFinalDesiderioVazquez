package ar.utn.tpi.listas.perfil;

public class PerfilNoEncontradoException extends RuntimeException {

    public PerfilNoEncontradoException(long id) {
        super("No existe el perfil " + id);
    }
}
