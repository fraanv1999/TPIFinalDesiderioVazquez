package ar.utn.tpi.listas.perfil;

/** El perfil está mal configurado (campo inexistente, regla incompleta...). */
public class PerfilInvalidoException extends RuntimeException {

    public PerfilInvalidoException(String perfil, String detalle) {
        super("Perfil '" + perfil + "': " + detalle);
    }
}
