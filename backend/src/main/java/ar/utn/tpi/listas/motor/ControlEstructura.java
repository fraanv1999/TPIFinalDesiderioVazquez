package ar.utn.tpi.listas.motor;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ar.utn.tpi.listas.excel.LibroExcel;
import ar.utn.tpi.listas.perfil.MapeoColumna;
import ar.utn.tpi.listas.perfil.Perfil;

/**
 * Compara el archivo con el contrato del perfil antes de procesar filas.
 * Los encabezados se comparan sin distinguir mayúsculas, tildes ni espacios repetidos.
 */
public final class ControlEstructura {

    private ControlEstructura() {
    }

    public static List<Diferencia> verificar(Perfil perfil, LibroExcel libro) {
        if (libro.hoja().isEmpty()) {
            return List.of(new Diferencia(TipoDiferencia.HOJA_INEXISTENTE, null, perfil.hoja(),
                    String.join(", ", libro.hojas()),
                    "No existe la hoja '" + perfil.hoja() + "'. Hojas del archivo: " + String.join(", ", libro.hojas())));
        }
        Map<String, Object> encabezados = libro.hoja().get().fila(perfil.filaEncabezado());
        List<Diferencia> diferencias = new ArrayList<>();
        for (MapeoColumna m : perfil.columnas()) {
            Object encontrado = encabezados.get(m.columna());
            if (normalizar(encontrado).equals(normalizar(m.encabezado()))) {
                continue;
            }
            String otraColumna = buscar(encabezados, m.encabezado());
            if (otraColumna != null) {
                diferencias.add(new Diferencia(TipoDiferencia.POSICION_INESPERADA, m.columna(), m.encabezado(), otraColumna,
                        "'" + m.encabezado() + "' se esperaba en la columna " + m.columna() + " y está en la " + otraColumna));
            } else if (encontrado == null) {
                diferencias.add(new Diferencia(TipoDiferencia.COLUMNA_FALTANTE, m.columna(), m.encabezado(), null,
                        "Falta la columna '" + m.encabezado() + "' (" + m.columna() + perfil.filaEncabezado() + " está vacía)"));
            } else {
                diferencias.add(new Diferencia(TipoDiferencia.ENCABEZADO_MODIFICADO, m.columna(), m.encabezado(), texto(encontrado),
                        "En " + m.columna() + perfil.filaEncabezado() + " se esperaba '" + m.encabezado()
                                + "' y dice '" + texto(encontrado) + "'"));
            }
        }
        return diferencias;
    }

    private static String buscar(Map<String, Object> encabezados, String esperado) {
        return encabezados.entrySet().stream()
                .filter(e -> normalizar(e.getValue()).equals(normalizar(esperado)))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private static String texto(Object valor) {
        return valor instanceof BigDecimal n ? n.toPlainString() : String.valueOf(valor);
    }

    static String normalizar(Object valor) {
        if (valor == null) {
            return "";
        }
        String sinTildes = Normalizer.normalize(texto(valor), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.strip().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }
}
