package ar.utn.tpi.listas.proveedor;

public class ProveedorNoEncontradoException extends RuntimeException {

    public ProveedorNoEncontradoException(long id) {
        super("No existe el proveedor " + id);
    }
}
