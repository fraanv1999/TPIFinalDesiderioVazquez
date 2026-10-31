package ar.utn.tpi.listas.perfil;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de UNA hoja de un proveedor: dónde están los encabezados, desde qué fila se examinan
 * los datos, qué representa cada columna y qué cálculos aplicar.
 * {@code campoClave} identifica una fila de producto (ver docs/modulos.md, "Clasificación de filas").
 */
public record Perfil(String id,
                     String proveedor,
                     String nombre,
                     String formato,
                     String hoja,
                     int filaEncabezado,
                     int filaInicioDatos,
                     String campoClave,
                     List<MapeoColumna> columnas,
                     List<ReglaCalculo> reglas) {

    public Perfil {
        columnas = List.copyOf(columnas);
        reglas = reglas == null ? List.of() : List.copyOf(reglas);
    }

    public Optional<MapeoColumna> columnaDe(String campo) {
        return columnas.stream().filter(m -> campo.equals(m.campo())).findFirst();
    }
}
