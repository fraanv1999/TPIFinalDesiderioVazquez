package ar.utn.tpi.listas.importacion;

/** La operación no corresponde al estado actual de la importación (por ejemplo, confirmar una rechazada). */
public class EstadoInvalidoException extends RuntimeException {

    public EstadoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
