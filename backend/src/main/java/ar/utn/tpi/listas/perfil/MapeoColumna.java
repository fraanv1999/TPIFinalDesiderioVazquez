package ar.utn.tpi.listas.perfil;

import java.util.List;

/**
 * Columna del Excel → campo destino. {@code campo == null} indica una columna ignorada,
 * cuyo encabezado igual se controla para detectar cambios de formato.
 */
public record MapeoColumna(String columna, String encabezado, String campo, List<Transformacion> transformaciones) {

    public MapeoColumna {
        transformaciones = transformaciones == null ? List.of() : List.copyOf(transformaciones);
    }

    public boolean ignorada() {
        return campo == null;
    }
}
