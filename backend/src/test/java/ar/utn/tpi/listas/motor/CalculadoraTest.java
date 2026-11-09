package ar.utn.tpi.listas.motor;

import static ar.utn.tpi.listas.motor.Datos.FORMATO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ar.utn.tpi.listas.perfil.Operacion;
import ar.utn.tpi.listas.perfil.ReglaCalculo;

/** Las 8 operaciones del conjunto cerrado. Los porcentajes son fracciones: 0.04 = 4 %. */
class CalculadoraTest {

    private final Map<String, Object> fila = new HashMap<>(Map.of(
            "precio_costo", new BigDecimal("1000"),
            "iva", new BigDecimal("210"),
            "nombre", "RODILLO",
            "codigo", "10 x 40"));

    private Object aplicar(Operacion op, String base, String secundario, String valor) throws Exception {
        return Calculadora.aplicar(new ReglaCalculo("precio", op, base, secundario,
                valor == null ? null : new BigDecimal(valor)), fila, FORMATO);
    }

    @Test
    void copiar() throws Exception {
        assertThat(aplicar(Operacion.COPIAR, "precio_costo", null, null)).isEqualTo(new BigDecimal("1000"));
    }

    @Test
    void valorFijo() throws Exception {
        assertThat(aplicar(Operacion.VALOR_FIJO, null, null, "99.5")).isEqualTo(new BigDecimal("99.5"));
    }

    @Test
    void sumarYRestarPorcentaje() throws Exception {
        assertThat((BigDecimal) aplicar(Operacion.SUMAR_PORCENTAJE, "precio_costo", null, "0.04"))
                .isEqualByComparingTo("1040");
        assertThat((BigDecimal) aplicar(Operacion.RESTAR_PORCENTAJE, "precio_costo", null, "0.1"))
                .isEqualByComparingTo("900");
    }

    @Test
    void multiplicarYDividir() throws Exception {
        assertThat((BigDecimal) aplicar(Operacion.MULTIPLICAR, "precio_costo", null, "0.21")).isEqualByComparingTo("210");
        assertThat((BigDecimal) aplicar(Operacion.DIVIDIR, "precio_costo", null, "3"))
                .isEqualByComparingTo("333.3333333333333");
    }

    @Test
    void dividirPorCeroEsError() {
        assertThatThrownBy(() -> aplicar(Operacion.DIVIDIR, "precio_costo", null, "0"))
                .isInstanceOf(ValorInvalidoException.class)
                .hasMessage("división por cero al calcular Precio");
    }

    @Test
    void sumarCampo() throws Exception {
        assertThat((BigDecimal) aplicar(Operacion.SUMAR_CAMPO, "precio_costo", "iva", null)).isEqualByComparingTo("1210");
    }

    @Test
    void concatenarUneConUnEspacioYSalteaLosVacios() throws Exception {
        assertThat(aplicar(Operacion.CONCATENAR, "nombre", "codigo", null)).isEqualTo("RODILLO 10 x 40");
        assertThat(aplicar(Operacion.CONCATENAR, "nombre", "categoria", null)).isEqualTo("RODILLO");
        assertThat(aplicar(Operacion.CONCATENAR, "categoria", "stock", null)).isNull();
    }

    @Test
    void sinPrecisionPerdida() throws Exception {
        fila.put("precio_costo", new BigDecimal("11.55"));
        assertThat((BigDecimal) aplicar(Operacion.SUMAR_PORCENTAJE, "precio_costo", null, "0.04"))
                .isEqualByComparingTo("12.012");
    }

    @Test
    void faltaLaBase() {
        assertThatThrownBy(() -> aplicar(Operacion.SUMAR_PORCENTAJE, "stock", null, "0.04"))
                .isInstanceOf(ValorInvalidoException.class)
                .hasMessage("falta Stock para el cálculo");
    }
}
