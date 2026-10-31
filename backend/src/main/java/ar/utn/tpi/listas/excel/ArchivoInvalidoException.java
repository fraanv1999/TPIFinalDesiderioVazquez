package ar.utn.tpi.listas.excel;

public class ArchivoInvalidoException extends RuntimeException {

    public ArchivoInvalidoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
