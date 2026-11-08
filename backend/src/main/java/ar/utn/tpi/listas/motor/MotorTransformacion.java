package ar.utn.tpi.listas.motor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ar.utn.tpi.listas.excel.HojaExcel;
import ar.utn.tpi.listas.perfil.CampoDestino;
import ar.utn.tpi.listas.perfil.FormatoDestino;
import ar.utn.tpi.listas.perfil.MapeoColumna;
import ar.utn.tpi.listas.perfil.Perfil;
import ar.utn.tpi.listas.perfil.ReglaCalculo;
import ar.utn.tpi.listas.perfil.Transformacion;

/**
 * Clasifica, transforma, valida y calcula cada fila desde la de inicio de datos hasta la última.
 * Solo se miran las columnas mapeadas, y un 0 en un campo numérico cuenta como vacío.
 *
 * <ol>
 *   <li>Ninguna columna mapeada tiene valor → IGNORADA (VACIA).</li>
 *   <li>Sin campo clave y sin ningún número → IGNORADA (TITULO). Su texto pasa a ser la categoría de los
 *       productos siguientes y se reinicia RELLENAR_ABAJO.</li>
 *   <li>El resto es un producto: si falta el campo clave o un obligatorio, algo no se puede convertir,
 *       un número obligatorio no es mayor a 0 o un cálculo falla → ERROR. Si no → VALIDA.</li>
 * </ol>
 */
public final class MotorTransformacion {

    private final Perfil perfil;
    private final FormatoDestino formato;
    private final MapeoColumna clave;
    /** Último valor de cada columna con RELLENAR_ABAJO. */
    private final Map<String, Object> ultimos = new HashMap<>();
    private String categoria;

    private MotorTransformacion(Perfil perfil, FormatoDestino formato) {
        this.perfil = perfil;
        this.formato = formato;
        this.clave = perfil.columnaDe(perfil.campoClave()).orElseThrow();
    }

    public static List<FilaResultado> procesar(Perfil perfil, FormatoDestino formato, HojaExcel hoja, int filaInicio) {
        MotorTransformacion motor = new MotorTransformacion(perfil, formato);
        List<FilaResultado> filas = new ArrayList<>();
        for (int n = filaInicio; n <= hoja.ultimaFila(); n++) {
            filas.add(motor.procesarFila(n, hoja.fila(n)));
        }
        return filas;
    }

    private FilaResultado procesarFila(int numero, Map<String, Object> crudos) {
        if (perfil.mapeadas().stream().noneMatch(m -> tieneValor(crudos, m))) {
            return FilaResultado.ignorada(numero, MotivoIgnorada.VACIA, crudos);
        }
        if (!tieneValor(crudos, clave)
                && perfil.mapeadas().stream().noneMatch(m -> campo(m).tipo().esNumerico() && tieneValor(crudos, m))) {
            categoria = textoTitulo(crudos);
            ultimos.clear();
            return FilaResultado.ignorada(numero, MotivoIgnorada.TITULO, crudos);
        }

        Map<String, Object> valores = new LinkedHashMap<>();
        List<ErrorCampo> errores = new ArrayList<>();
        Set<String> leidos = new HashSet<>();
        for (MapeoColumna m : perfil.mapeadas()) {
            CampoDestino campo = campo(m);
            leidos.add(campo.clave());
            try {
                valores.put(campo.clave(), Conversor.convertir(celda(crudos, m), m.transformaciones(), campo));
            } catch (ValorInvalidoException e) {
                errores.add(new ErrorCampo(campo.clave(), e.getMessage()));
            }
        }
        if (perfil.campoCategoria() != null && valores.get(perfil.campoCategoria()) == null) {
            valores.put(perfil.campoCategoria(), categoria);
        }
        validar(valores, errores, leidos, true);
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
        if (errores.isEmpty()) {
            validar(valores, errores, leidos, false);
        }
        if (!errores.isEmpty()) {
            return new FilaResultado(numero, EstadoFila.ERROR, null, crudos, null, errores);
        }
        return new FilaResultado(numero, EstadoFila.VALIDA, null, crudos, ordenar(valores), List.of());
    }

    /** Valor de la celda; con RELLENAR_ABAJO, si está vacía toma el último valor de la columna. */
    private Object celda(Map<String, Object> crudos, MapeoColumna m) {
        if (!m.transformaciones().contains(Transformacion.RELLENAR_ABAJO)) {
            return crudos.get(m.columna());
        }
        if (tieneValor(crudos, m)) {
            ultimos.put(m.columna(), crudos.get(m.columna()));
        }
        return ultimos.get(m.columna());
    }

    /**
     * Campo clave y obligatorios presentes, y números obligatorios mayores a 0. Se valida en dos pasos:
     * primero los campos leídos del Excel y, después de los cálculos, los calculados.
     */
    private void validar(Map<String, Object> valores, List<ErrorCampo> errores, Set<String> leidos, boolean deLeidos) {
        Set<String> conError = new HashSet<>();
        errores.forEach(e -> conError.add(e.campo()));
        for (CampoDestino campo : formato.campos()) {
            boolean esClave = campo.clave().equals(perfil.campoClave());
            if (!campo.obligatorio() && !esClave
                    || conError.contains(campo.clave())
                    || leidos.contains(campo.clave()) != deLeidos) {
                continue;
            }
            Object valor = valores.get(campo.clave());
            if (valor == null) {
                errores.add(new ErrorCampo(campo.clave(), campo.etiqueta() + " vacío"));
            } else if (campo.obligatorio() && valor instanceof BigDecimal n && n.signum() <= 0) {
                errores.add(new ErrorCampo(campo.clave(), campo.etiqueta() + " debe ser mayor a 0"));
            }
        }
    }

    /** Texto de la primera columna mapeada con valor: nombre de la categoría. */
    private String textoTitulo(Map<String, Object> crudos) {
        return perfil.mapeadas().stream()
                .filter(m -> tieneValor(crudos, m))
                .map(m -> crudos.get(m.columna()))
                .map(v -> v instanceof BigDecimal n ? n.toPlainString() : v.toString().strip())
                .findFirst()
                .orElse(null);
    }

    /** Todos los campos del formato, en su orden; null si el perfil no los completa. */
    private Map<String, Object> ordenar(Map<String, Object> valores) {
        Map<String, Object> ordenados = new LinkedHashMap<>();
        formato.campos().forEach(c -> ordenados.put(c.clave(), valores.get(c.clave())));
        return ordenados;
    }

    private CampoDestino campo(MapeoColumna m) {
        return formato.campo(m.campo()).orElseThrow();
    }

    private boolean tieneValor(Map<String, Object> crudos, MapeoColumna m) {
        Object celda = crudos.get(m.columna());
        if (celda instanceof String s) return !s.isBlank();
        if (celda instanceof BigDecimal n) return n.signum() != 0 || !campo(m).tipo().esNumerico();
        return celda != null;
    }
}
