package ar.utn.tpi.listas.motor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ar.utn.tpi.listas.excel.HojaExcel;
import ar.utn.tpi.listas.perfil.CampoDestino;
import ar.utn.tpi.listas.perfil.FormatoDestino;
import ar.utn.tpi.listas.perfil.MapeoColumna;
import ar.utn.tpi.listas.perfil.Perfil;
import ar.utn.tpi.listas.perfil.ReglaCalculo;

/**
 * Clasifica, transforma, valida y calcula cada fila desde {@code filaInicioDatos} hasta la última.
 *
 * <ol>
 *   <li>Sin ningún valor → IGNORADA (VACIA).</li>
 *   <li>Sin campo clave y sin ningún campo numérico mapeado → IGNORADA (TITULO): títulos y categorías.</li>
 *   <li>El resto es un producto: si algo no se puede convertir, falta un obligatorio, un número obligatorio
 *       no es mayor a 0 o un cálculo falla → ERROR. Si no → VALIDA.</li>
 * </ol>
 */
public final class MotorTransformacion {

    private MotorTransformacion() {
    }

    public static List<FilaResultado> procesar(Perfil perfil, FormatoDestino formato, HojaExcel hoja) {
        List<FilaResultado> filas = new ArrayList<>();
        for (int n = perfil.filaInicioDatos(); n <= hoja.ultimaFila(); n++) {
            filas.add(procesarFila(n, hoja.fila(n), perfil, formato));
        }
        return filas;
    }

    private static FilaResultado procesarFila(int numero, Map<String, Object> crudos, Perfil perfil, FormatoDestino formato) {
        if (crudos.isEmpty()) {
            return FilaResultado.ignorada(numero, MotivoIgnorada.VACIA, crudos);
        }
        if (!esProducto(crudos, perfil, formato)) {
            return FilaResultado.ignorada(numero, MotivoIgnorada.TITULO, crudos);
        }

        Map<String, Object> valores = new LinkedHashMap<>();
        List<ErrorCampo> errores = new ArrayList<>();
        for (MapeoColumna m : perfil.columnas()) {
            if (m.ignorada()) continue;
            CampoDestino campo = formato.campo(m.campo()).orElseThrow();
            try {
                Object valor = Conversor.convertir(crudos.get(m.columna()), m.transformaciones(), campo);
                validar(valor, campo, errores);
                valores.put(campo.clave(), valor);
            } catch (ValorInvalidoException e) {
                errores.add(new ErrorCampo(campo.clave(), e.getMessage()));
            }
        }
        if (errores.isEmpty()) {
            for (ReglaCalculo regla : perfil.reglas()) {
                try {
                    valores.put(regla.resultado(), Calculadora.aplicar(regla, valores, formato));
                } catch (ValorInvalidoException e) {
                    errores.add(new ErrorCampo(regla.resultado(), e.getMessage()));
                    break;
                }
            }
        }
        if (!errores.isEmpty()) {
            return new FilaResultado(numero, EstadoFila.ERROR, null, crudos, null, errores);
        }
        return new FilaResultado(numero, EstadoFila.VALIDA, null, crudos, ordenar(valores, formato), List.of());
    }

    private static boolean esProducto(Map<String, Object> crudos, Perfil perfil, FormatoDestino formato) {
        String columnaClave = perfil.columnaDe(perfil.campoClave()).orElseThrow().columna();
        if (tieneValor(crudos.get(columnaClave))) {
            return true;
        }
        return perfil.columnas().stream()
                .filter(m -> !m.ignorada() && formato.campo(m.campo()).orElseThrow().tipo().esNumerico())
                .anyMatch(m -> tieneValor(crudos.get(m.columna())));
    }

    private static void validar(Object valor, CampoDestino campo, List<ErrorCampo> errores) {
        if (!campo.obligatorio()) return;
        if (valor == null) {
            errores.add(new ErrorCampo(campo.clave(), campo.etiqueta() + " vacío"));
        } else if (valor instanceof BigDecimal n && n.signum() <= 0) {
            errores.add(new ErrorCampo(campo.clave(), campo.etiqueta() + " debe ser mayor a 0"));
        }
    }

    /** Todos los campos del formato, en su orden; null si el perfil no los completa. */
    private static Map<String, Object> ordenar(Map<String, Object> valores, FormatoDestino formato) {
        Map<String, Object> ordenados = new LinkedHashMap<>();
        formato.campos().forEach(c -> ordenados.put(c.clave(), valores.get(c.clave())));
        return ordenados;
    }

    private static boolean tieneValor(Object celda) {
        return celda != null && !(celda instanceof String s && s.isBlank());
    }
}
