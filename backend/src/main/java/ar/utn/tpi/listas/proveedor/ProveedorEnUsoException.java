package ar.utn.tpi.listas.proveedor;

/** Nombre repetido, o baja de un proveedor que tiene perfiles. */
public class ProveedorEnUsoException extends RuntimeException {

    public ProveedorEnUsoException(String mensaje) {
        super(mensaje);
    }
}
