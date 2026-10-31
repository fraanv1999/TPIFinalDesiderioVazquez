package ar.utn.tpi.listas.motor;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Map;

import ar.utn.tpi.listas.perfil.FormatoDestino;
import ar.utn.tpi.listas.perfil.ReglaCalculo;

/**
 * Aplica una regla de cálculo sobre los valores de una fila. Sin redondeo: la precisión completa se
 * conserva hasta la exportación. Los porcentajes son fracciones (0.04 = 4 %).
 */
final class Calculadora {

    private Calculadora() {
    }

    static Object aplicar(ReglaCalculo regla, Map<String, Object> valores, FormatoDestino formato)
            throws ValorInvalidoException {
        BigDecimal v = regla.valor();
        return switch (regla.operacion()) {
            case COPIAR -> valores.get(regla.base());
            case VALOR_FIJO -> v;
            case SUMAR_PORCENTAJE -> numero(regla.base(), valores, formato).multiply(BigDecimal.ONE.add(v));
            case RESTAR_PORCENTAJE -> numero(regla.base(), valores, formato).multiply(BigDecimal.ONE.subtract(v));
            case MULTIPLICAR -> numero(regla.base(), valores, formato).multiply(v);
            case DIVIDIR -> {
                if (v.signum() == 0) {
                    throw new ValorInvalidoException("división por cero al calcular " + etiqueta(regla.resultado(), formato));
                }
                yield numero(regla.base(), valores, formato).divide(v, MathContext.DECIMAL64);
            }
            case SUMAR_CAMPO -> numero(regla.base(), valores, formato).add(numero(regla.secundario(), valores, formato));
            case CONCATENAR -> texto(valores.get(regla.base())) + texto(valores.get(regla.secundario()));
        };
    }

    private static BigDecimal numero(String campo, Map<String, Object> valores, FormatoDestino formato)
            throws ValorInvalidoException {
        Object valor = valores.get(campo);
        if (valor instanceof BigDecimal n) {
            return n;
        }
        throw new ValorInvalidoException("falta " + etiqueta(campo, formato) + " para el cálculo");
    }

    private static String texto(Object valor) {
        if (valor == null) return "";
        return valor instanceof BigDecimal n ? n.toPlainString() : valor.toString();
    }

    private static String etiqueta(String campo, FormatoDestino formato) {
        return formato.campo(campo).map(c -> c.etiqueta()).orElse(campo);
    }
}
