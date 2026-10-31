package ar.utn.tpi.listas.motor;

/** Un valor no se pudo convertir o calcular. El mensaje se muestra al usuario. */
class ValorInvalidoException extends Exception {

    ValorInvalidoException(String mensaje) {
        super(mensaje);
    }
}
