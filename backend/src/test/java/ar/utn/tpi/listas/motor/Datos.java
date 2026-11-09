package ar.utn.tpi.listas.motor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ar.utn.tpi.listas.excel.HojaExcel;
import ar.utn.tpi.listas.perfil.CampoDestino;
import ar.utn.tpi.listas.perfil.FormatoDestino;
import ar.utn.tpi.listas.perfil.MapeoColumna;
import ar.utn.tpi.listas.perfil.Perfil;
import ar.utn.tpi.listas.perfil.ReglaCalculo;
import ar.utn.tpi.listas.perfil.TipoDato;
import ar.utn.tpi.listas.perfil.Transformacion;

/** Formato, perfiles y hojas armados en memoria para las pruebas unitarias. */
final class Datos {

    static final CampoDestino CODIGO = new CampoDestino("codigo", "Codigo", TipoDato.TEXTO, false, true);
    static final CampoDestino NOMBRE = new CampoDestino("nombre", "Nombre", TipoDato.TEXTO, true, true);
    static final CampoDestino CATEGORIA = new CampoDestino("categoria", "Categoria", TipoDato.TEXTO, false, true);
    static final CampoDestino STOCK = new CampoDestino("stock", "Stock", TipoDato.ENTERO, false, true);
    static final CampoDestino COSTO = new CampoDestino("precio_costo", "Precio de costo", TipoDato.DECIMAL, true, false);
    static final CampoDestino IVA = new CampoDestino("iva", "IVA", TipoDato.DECIMAL, false, true);
    static final CampoDestino PRECIO = new CampoDestino("precio", "Precio", TipoDato.DECIMAL, true, true);

    static final FormatoDestino FORMATO = new FormatoDestino("prueba",
            List.of(CODIGO, NOMBRE, CATEGORIA, STOCK, COSTO, IVA, PRECIO));

    private Datos() {
    }

    static MapeoColumna columna(String letra, String encabezado, String campo, Transformacion... t) {
        return new MapeoColumna(letra, encabezado, campo, List.of(t));
    }

    /** Encabezados en la fila 2, datos desde la 3: A código, B nombre, C precio, D ignorada. */
    static Perfil perfil(int margen, String campoCategoria, List<ReglaCalculo> reglas) {
        return new Perfil("prueba", "Proveedor", "Lista", "prueba", "Hoja1", 2, margen, 3, "codigo", campoCategoria,
                List.of(columna("A", "Código", "codigo", Transformacion.TRIM),
                        columna("B", "Descripción", "nombre", Transformacion.TRIM, Transformacion.RELLENAR_ABAJO),
                        columna("C", "Precio", "precio_costo", Transformacion.A_DECIMAL),
                        columna("D", "Observaciones", null)),
                reglas);
    }

    static Perfil perfil() {
        return perfil(0, "categoria", List.of(new ReglaCalculo("precio", ar.utn.tpi.listas.perfil.Operacion.COPIAR,
                "precio_costo", null, null)));
    }

    /** Hoja con filas {@code numero → [A, B, C, ...]}; los null son celdas vacías. */
    static HojaExcel hoja(Object[]... filas) {
        TreeMap<Integer, Map<String, Object>> mapa = new TreeMap<>();
        for (Object[] fila : filas) {
            Map<String, Object> celdas = new LinkedHashMap<>();
            for (int i = 1; i < fila.length; i++) {
                if (fila[i] != null) {
                    celdas.put(String.valueOf((char) ('A' + i - 1)), fila[i]);
                }
            }
            if (!celdas.isEmpty()) {
                mapa.put((Integer) fila[0], celdas);
            }
        }
        return new HojaExcel("Hoja1", mapa);
    }

    static Object[] fila(Object... numeroYCeldas) {
        return numeroYCeldas;
    }
}
