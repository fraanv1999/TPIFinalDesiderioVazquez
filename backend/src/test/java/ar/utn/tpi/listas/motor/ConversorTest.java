package ar.utn.tpi.listas.motor;

import static ar.utn.tpi.listas.motor.Datos.COSTO;
import static ar.utn.tpi.listas.motor.Datos.NOMBRE;
import static ar.utn.tpi.listas.motor.Datos.STOCK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import ar.utn.tpi.listas.perfil.Transformacion;

class ConversorTest {

    private static final List<Transformacion> NINGUNA = List.of();

    @Test
    void trimYMayusculas() throws Exception {
        assertThat(Conversor.convertir("  pila aa ", List.of(Transformacion.TRIM, Transformacion.MAYUSCULAS), NOMBRE))
                .isEqualTo("PILA AA");
    }

    @Test
    void unTextoEnBlancoQuedaVacio() throws Exception {
        assertThat(Conversor.convertir("   ", NINGUNA, NOMBRE)).isNull();
        assertThat(Conversor.convertir(null, NINGUNA, COSTO)).isNull();
    }

    @Test
    void unNumeroEnUnCampoTextoSeEscribeSinNotacionCientifica() throws Exception {
        assertThat(Conversor.convertir(new BigDecimal("1E+3"), NINGUNA, NOMBRE)).isEqualTo("1000");
    }

    @ParameterizedTest
    @CsvSource(delimiter = ';', value = {
            "1.474,41; 1474.41",
            "1474,41; 1474.41",
            "1474.41; 1474.41",
            "1474; 1474",
            "$ 1.200; 1200",
            "-3,5; -3.5"})
    void aDecimalAceptaNumerosEscritosComoTexto(String texto, String esperado) throws Exception {
        assertThat(Conversor.convertir(texto, List.of(Transformacion.A_DECIMAL), COSTO)).isEqualTo(new BigDecimal(esperado));
    }

    @Test
    void sinADecimalUnTextoEnUnCampoNumericoEsError() {
        assertThatThrownBy(() -> Conversor.convertir("1474,41", NINGUNA, COSTO))
                .isInstanceOf(ValorInvalidoException.class)
                .hasMessage("Precio de costo: '1474,41' no es un número");
    }

    @Test
    void unTextoQueNoEsNumeroEsError() {
        assertThatThrownBy(() -> Conversor.convertir("SIN STOCK", List.of(Transformacion.A_DECIMAL), COSTO))
                .isInstanceOf(ValorInvalidoException.class)
                .hasMessageContaining("'SIN STOCK' no es un número");
    }

    @Test
    void elStockTieneQueSerEntero() throws Exception {
        assertThat(Conversor.convertir(new BigDecimal("10"), NINGUNA, STOCK)).isEqualTo(new BigDecimal("10"));
        assertThat(Conversor.convertir("10", List.of(Transformacion.A_ENTERO), STOCK)).isEqualTo(new BigDecimal("10"));
        assertThatThrownBy(() -> Conversor.convertir(new BigDecimal("2.5"), NINGUNA, STOCK))
                .hasMessage("Stock: 2.5 no es un número entero");
        assertThatThrownBy(() -> Conversor.convertir("10KG", List.of(Transformacion.A_ENTERO), STOCK))
                .hasMessage("Stock: '10KG' no es un número entero");
    }
}
