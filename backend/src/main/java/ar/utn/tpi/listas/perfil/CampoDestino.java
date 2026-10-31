package ar.utn.tpi.listas.perfil;

/**
 * Columna del formato que acepta el sistema destino. Un campo no {@code exportado} es auxiliar:
 * participa de los cálculos (por ejemplo, el precio de costo) pero no sale en el archivo final.
 */
public record CampoDestino(String clave, String etiqueta, TipoDato tipo, boolean obligatorio, boolean exportado) {
}
