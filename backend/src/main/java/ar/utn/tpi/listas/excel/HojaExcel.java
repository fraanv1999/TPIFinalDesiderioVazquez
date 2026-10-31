package ar.utn.tpi.listas.excel;

import java.util.Map;
import java.util.SortedMap;

/**
 * Contenido de una hoja. Filas numeradas desde 1 y columnas por letra, como en Excel.
 * Cada valor es {@code String} o {@code BigDecimal}; las celdas vacías no aparecen.
 */
public record HojaExcel(String nombre, SortedMap<Integer, Map<String, Object>> filas) {

    public Map<String, Object> fila(int numero) {
        return filas.getOrDefault(numero, Map.of());
    }

    public Object celda(int fila, String columna) {
        return fila(fila).get(columna);
    }

    public int ultimaFila() {
        return filas.isEmpty() ? 0 : filas.lastKey();
    }
}
