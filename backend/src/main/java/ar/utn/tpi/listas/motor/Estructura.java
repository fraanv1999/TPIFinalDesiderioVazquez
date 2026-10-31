package ar.utn.tpi.listas.motor;

import java.util.List;

/**
 * Resultado del control de estructura. {@code desplazamiento} es cuántas filas se movieron los
 * encabezados (y, con ellos, el inicio de los datos) respecto del perfil.
 */
public record Estructura(List<Diferencia> diferencias, int desplazamiento) {

    public boolean bloqueada() {
        return diferencias.stream().anyMatch(Diferencia::bloqueante);
    }
}
