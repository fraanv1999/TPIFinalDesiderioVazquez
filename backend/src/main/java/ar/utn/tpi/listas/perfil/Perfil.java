package ar.utn.tpi.listas.perfil;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de UNA hoja de un proveedor: dónde están los encabezados, desde qué fila se examinan
 * los datos, qué representa cada columna y qué cálculos aplicar.
 * {@code campoClave} identifica una fila de producto y {@code campoCategoria}, si está, recibe el
 * texto de las filas título (ver docs/modulos.md, "Clasificación de filas").
 * {@code margenEncabezado} acepta los encabezados hasta esa cantidad de filas más arriba o más abajo.
 */
public record Perfil(String id,
                     String proveedor,
                     String nombre,
                     String formato,
                     String hoja,
                     int filaEncabezado,
                     int margenEncabezado,
                     int filaInicioDatos,
                     String campoClave,
                     String campoCategoria,
                     List<MapeoColumna> columnas,
                     List<ReglaCalculo> reglas) {

    public Perfil {
        columnas = List.copyOf(columnas);
        reglas = reglas == null ? List.of() : List.copyOf(reglas);
    }

    public Optional<MapeoColumna> columnaDe(String campo) {
        return columnas.stream().filter(m -> campo.equals(m.campo())).findFirst();
    }

    public List<MapeoColumna> mapeadas() {
        return columnas.stream().filter(m -> !m.ignorada()).toList();
    }
}
