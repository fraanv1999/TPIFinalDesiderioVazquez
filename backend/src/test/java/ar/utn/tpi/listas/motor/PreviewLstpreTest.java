package ar.utn.tpi.listas.motor;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ar.utn.tpi.listas.perfil.RepositorioPerfiles;

/** Corte vertical: la lista real de Ferretería (hoja LSTPRE) de punta a punta hasta la vista previa. */
class PreviewLstpreTest {

    private static final Path LSTPRE = Path.of("..", "dataset-example", "dataset-providers", "Lista Ferreteria 19 08.xlsx");

    private static ResultadoImportacion resultado;

    @BeforeAll
    static void importar() throws IOException {
        ServicioImportacion servicio = new ServicioImportacion(new RepositorioPerfiles());
        try (InputStream in = Files.newInputStream(LSTPRE)) {
            resultado = servicio.preview("ferreteria-lstpre", "Lista Ferreteria 19 08.xlsx", in);
        }
    }

    @Test
    void laEstructuraCoincide() {
        assertThat(resultado.estado()).isEqualTo(EstadoImportacion.PREVIEW);
        assertThat(resultado.diferencias()).isEmpty();
    }

    @Test
    void clasificaTodasLasFilas() {
        assertThat(resultado.resumen()).isEqualTo(new Resumen(1412, 541, 361, 180, 0));
    }

    @Test
    void unTituloEsLaCategoriaDeLosProductosSiguientes() {
        assertThat(fila(10).motivoIgnorada()).isEqualTo(MotivoIgnorada.TITULO);
        assertThat(fila(10).datosCrudos()).containsEntry("B", "PILAS ENERGIZER ALCALINAS");

        FilaResultado ep52 = fila(12);
        assertThat(ep52.estado()).isEqualTo(EstadoFila.VALIDA);
        assertThat(ep52.datosResultado())
                .containsEntry("codigo", "EP52")
                .containsEntry("categoria", "PILAS ENERGIZER ALCALINAS")
                .containsEntry("precio_costo", new BigDecimal("1474.41"))
                .containsEntry("precio", new BigDecimal("1474.41"));
    }

    private static FilaResultado fila(int numero) {
        return resultado.filas().stream().filter(f -> f.numeroFila() == numero).findFirst().orElseThrow();
    }
}
