package ar.utn.tpi.listas.importacion;

public class ImportacionNoEncontradaException extends RuntimeException {

    public ImportacionNoEncontradaException(long id) {
        super("No existe la importación " + id);
    }
}
