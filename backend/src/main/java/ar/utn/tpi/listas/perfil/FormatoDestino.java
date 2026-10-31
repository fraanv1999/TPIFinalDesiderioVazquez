package ar.utn.tpi.listas.perfil;

import java.util.List;
import java.util.Optional;

/** Formato del sistema destino. El orden de {@code campos} es el orden de las columnas. */
public record FormatoDestino(String nombre, List<CampoDestino> campos) {

    public Optional<CampoDestino> campo(String clave) {
        return campos.stream().filter(c -> c.clave().equals(clave)).findFirst();
    }
}
