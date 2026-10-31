package ar.utn.tpi.listas.motor;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ar.utn.tpi.listas.excel.HojaExcel;
import ar.utn.tpi.listas.excel.LibroExcel;
import ar.utn.tpi.listas.perfil.MapeoColumna;
import ar.utn.tpi.listas.perfil.Perfil;

/**
 * Compara el archivo con el contrato del perfil antes de procesar filas.
 * Los encabezados se comparan sin distinguir mayúsculas, tildes ni espacios repetidos.
 * Si no coinciden en {@code filaEncabezado}, se buscan iguales hasta {@code margenEncabezado} filas
 * más arriba o más abajo: si aparecen completos, es un aviso y los datos se corren lo mismo.
 */
public final class ControlEstructura {

    private ControlEstructura() {
    }

    public static Estructura verificar(Perfil perfil, LibroExcel libro) {
        if (libro.hoja().isEmpty()) {
            return new Estructura(List.of(new Diferencia(TipoDiferencia.HOJA_INEXISTENTE, true, null, perfil.hoja(),
                    String.join(", ", libro.hojas()),
                    "No existe la hoja '" + perfil.hoja() + "'. Hojas del archivo: " + String.join(", ", libro.hojas()))), 0);
        }
        HojaExcel hoja = libro.hoja().get();
        List<Diferencia> diferencias = comparar(perfil, hoja, perfil.filaEncabezado());
        if (diferencias.isEmpty()) {
            return new Estructura(List.of(), 0);
        }
        // Se prueba primero la fila más cercana: +1, -1, +2, -2...
        for (int d = 1; d <= perfil.margenEncabezado(); d++) {
            for (int desplazamiento : new int[] {d, -d}) {
                int fila = perfil.filaEncabezado() + desplazamiento;
                if (fila >= 1 && comparar(perfil, hoja, fila).isEmpty()) {
                    return new Estructura(List.of(new Diferencia(TipoDiferencia.ENCABEZADO_DESPLAZADO, false, null,
                            "fila " + perfil.filaEncabezado(), "fila " + fila,
                            "Los encabezados están en la fila " + fila + " y no en la " + perfil.filaEncabezado()
                                    + ". Los datos se leen desde la fila " + (perfil.filaInicioDatos() + desplazamiento))),
                            desplazamiento);
                }
            }
        }
        return new Estructura(diferencias, 0);
    }

    private static List<Diferencia> comparar(Perfil perfil, HojaExcel hoja, int filaEncabezado) {
        Map<String, Object> encabezados = hoja.fila(filaEncabezado);
        List<Diferencia> diferencias = new ArrayList<>();
        for (MapeoColumna m : perfil.columnas()) {
            Object encontrado = encabezados.get(m.columna());
            if (normalizar(encontrado).equals(normalizar(m.encabezado()))) {
                continue;
            }
            String otraColumna = buscar(encabezados, m.encabezado());
            if (otraColumna != null) {
                diferencias.add(new Diferencia(TipoDiferencia.POSICION_INESPERADA, true, m.columna(), m.encabezado(), otraColumna,
                        "'" + m.encabezado() + "' se esperaba en la columna " + m.columna() + " y está en la " + otraColumna));
            } else if (encontrado == null) {
                diferencias.add(new Diferencia(TipoDiferencia.COLUMNA_FALTANTE, true, m.columna(), m.encabezado(), null,
                        "Falta la columna '" + m.encabezado() + "' (" + m.columna() + filaEncabezado + " está vacía)"));
            } else {
                diferencias.add(new Diferencia(TipoDiferencia.ENCABEZADO_MODIFICADO, true, m.columna(), m.encabezado(),
                        texto(encontrado), "En " + m.columna() + filaEncabezado + " se esperaba '" + m.encabezado()
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
