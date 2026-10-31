package ar.utn.tpi.listas.motor;

/** Motivo por el que una fila se rechaza. {@code campo} es la clave del campo destino. */
public record ErrorCampo(String campo, String mensaje) {
}
