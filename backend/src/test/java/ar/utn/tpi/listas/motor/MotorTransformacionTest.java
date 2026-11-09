package ar.utn.tpi.listas.motor;

import static ar.utn.tpi.listas.motor.Datos.FORMATO;
import static ar.utn.tpi.listas.motor.Datos.fila;
import static ar.utn.tpi.listas.motor.Datos.hoja;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import ar.utn.tpi.listas.excel.HojaExcel;

/** Regla de clasificación de docs/modulos.md, "Clasificación de filas". */
class MotorTransformacionTest {

    private static List<FilaResultado> procesar(HojaExcel hoja) {
        return MotorTransformacion.procesar(Datos.perfil(), FORMATO, hoja, 3);
    }

    @Test
    void unaFilaSinValoresEnLasColumnasMapeadasEsVacia() {
        List<FilaResultado> filas = procesar(hoja(
                fila(3, null, null, null, "solo observaciones"),
                fila(4, "A1", "Pila", new BigDecimal("10"))));
        assertThat(filas.get(0).estado()).isEqualTo(EstadoFila.IGNORADA);
        assertThat(filas.get(0).motivoIgnorada()).isEqualTo(MotivoIgnorada.VACIA);
    }

    @Test
    void unCeroCuentaComoVacio() {
        List<FilaResultado> filas = procesar(hoja(fila(3, null, null, BigDecimal.ZERO)));
        assertThat(filas.get(0).motivoIgnorada()).isEqualTo(MotivoIgnorada.VACIA);
    }

    @Test
    void unTituloPasaASerLaCategoriaDeLosProductosSiguientes() {
        List<FilaResultado> filas = procesar(hoja(
                fila(3, null, "PILAS ALCALINAS"),
                fila(4, "EP52", "Pila AA", new BigDecimal("1474.41"))));
        assertThat(filas.get(0).motivoIgnorada()).isEqualTo(MotivoIgnorada.TITULO);
        assertThat(filas.get(1).estado()).isEqualTo(EstadoFila.VALIDA);
        assertThat(filas.get(1).datosResultado())
                .containsEntry("categoria", "PILAS ALCALINAS")
                .containsEntry("precio", new BigDecimal("1474.41"));
    }

    @Test
    void elResultadoTieneTodosLosCamposDelFormatoEnOrden() {
        FilaResultado f = procesar(hoja(fila(3, "A1", "Pila", new BigDecimal("10")))).get(0);
        assertThat(f.datosResultado()).containsOnlyKeys("codigo", "nombre", "categoria", "stock", "precio_costo", "iva", "precio");
        assertThat(f.datosResultado().get("stock")).isNull();
    }

    @Test
    void unPrecioQueNoEsNumeroEsError() {
        FilaResultado f = procesar(hoja(fila(3, "A1", "Rodillo", "SIN STOCK"))).get(0);
        assertThat(f.estado()).isEqualTo(EstadoFila.ERROR);
        assertThat(f.datosResultado()).isNull();
        assertThat(f.errores()).extracting(ErrorCampo::campo).containsExactly("precio_costo");
    }

    @Test
    void unPrecioSinCodigoEsError() {
        FilaResultado f = procesar(hoja(fila(3, null, "Rodillo", new BigDecimal("500")))).get(0);
        assertThat(f.estado()).isEqualTo(EstadoFila.ERROR);
        assertThat(f.errores()).extracting(ErrorCampo::mensaje).containsExactly("Codigo vacío");
    }

    @Test
    void unCodigoConPrecioCeroEsError() {
        FilaResultado f = procesar(hoja(fila(3, "FX10", "Fenix", BigDecimal.ZERO))).get(0);
        assertThat(f.estado()).isEqualTo(EstadoFila.ERROR);
        assertThat(f.errores()).extracting(ErrorCampo::mensaje).containsExactly("Precio de costo debe ser mayor a 0");
    }

    @Test
    void rellenarAbajoRepiteElUltimoValorYSeReiniciaEnCadaTitulo() {
        List<FilaResultado> filas = procesar(hoja(
                fila(3, "R1", "RODILLO LANAR", new BigDecimal("100")),
                fila(4, "R2", null, new BigDecimal("110")),
                fila(5, null, "PINCELES"),
                fila(6, "P1", null, new BigDecimal("50"))));
        assertThat(filas.get(1).datosResultado()).containsEntry("nombre", "RODILLO LANAR");
        assertThat(filas.get(3).estado()).isEqualTo(EstadoFila.ERROR);
        assertThat(filas.get(3).errores()).extracting(ErrorCampo::mensaje).containsExactly("Nombre vacío");
    }
}
