package ar.utn.tpi.listas.motor;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.jdbc.core.simple.JdbcClient;

import ar.utn.tpi.listas.BaseDePrueba;
import ar.utn.tpi.listas.perfil.RepositorioPerfiles;

/**
 * Reutilización del perfil: el mismo perfil de seed.sql sobre todas las versiones que el negocio recibió
 * de Ferretería y Paint Roller (dataset-example/dataset-versiones, una carpeta por fecha de recepción).
 */
class VersionesListasTest {

    private static final Path VERSIONES = Path.of("..", "dataset-example", "dataset-versiones");
    private static final long FERRETERIA = 1;
    private static final long PAINT_ROLLER = 2;
    private static final String PAINT = "Lista de precios Mayorista - PAINT ROLLER.xlsx";

    private static ServicioImportacion servicio;

    @BeforeAll
    static void crearServicio() {
        servicio = new ServicioImportacion(new RepositorioPerfiles(JdbcClient.create(BaseDePrueba.dataSource())));
    }

    @ParameterizedTest(name = "Ferretería {0}")
    @CsvSource(delimiter = '|', textBlock = """
            06-8-2026/Lista Ferreteria 29 07.xlsx  | 1405 | 362 | false
            19-8-2026/Lista Ferreteria 19 08.xlsx  | 1412 | 361 | false
            08-9-2026/Lista Ferreteria 07 09.xlsx  | 1403 | 361 | true
            22-9-2026/Lista Ferreteria 21 09.xlsx  | 1396 | 362 | true
            06-10-2026/Lista Ferreteria 05 10.xlsx | 1392 | 361 | false
            13-10-2026/Lista Ferreteria 05 10.xlsx | 1392 | 361 | false
            26-10-2026/Lista Ferreteria 16 10.xlsx | 1394 | 361 | false
            02-11-2026/Lista Ferreteria 02 11.xlsx | 1383 | 362 | false
            """)
    void ferreteria(String archivo, int validas, int vacias, boolean encabezadoEnFila8) throws IOException {
        ResultadoImportacion r = procesar(FERRETERIA, archivo);

        assertThat(r.estado()).isEqualTo(EstadoImportacion.PREVIEW);
        assertThat(r.resumen()).isEqualTo(new Resumen(validas, vacias + 180, vacias, 180, 0));
        if (encabezadoEnFila8) {
            // Dentro del margen del perfil: aviso, no bloqueo.
            assertThat(r.diferencias()).singleElement().satisfies(d -> {
                assertThat(d.tipo()).isEqualTo(TipoDiferencia.ENCABEZADO_DESPLAZADO);
                assertThat(d.bloqueante()).isFalse();
            });
        } else {
            assertThat(r.diferencias()).isEmpty();
        }
    }

    @ParameterizedTest(name = "Paint Roller {0}")
    @CsvSource(delimiter = '|', textBlock = """
            05-9-2026  | 168 | 66 81
            02-10-2026 | 166 | 66 81 214 215
            29-10-2026 | 161 | 66 81 163 168 176 183 209 214 215
            02-11-2026 | 159 | 66 81 163 168 176 183 184 185 209 214 215
            """)
    void paintRoller(String carpeta, int validas, String filasConError) throws IOException {
        ResultadoImportacion r = procesar(PAINT_ROLLER, carpeta + "/" + PAINT);

        assertThat(r.estado()).isEqualTo(EstadoImportacion.PREVIEW);
        assertThat(r.diferencias()).isEmpty();
        assertThat(r.resumen().validas()).isEqualTo(validas);
        assertThat(r.filas()).filteredOn(f -> f.estado() == EstadoFila.ERROR)
                .extracting(f -> String.valueOf(f.numeroFila()))
                .containsExactly(filasConError.split(" "));
        // La sección ACCESORIOS usa la columna Codigo para la marca.
        assertThat(r.repetidos()).extracting(CodigoRepetido::codigo).contains("RAPIFIX", "FIJAPEL");
    }

    @ParameterizedTest(name = "Corrección manual {0}")
    @CsvSource({
            "15-8-2026/Lista de precios Mayorista N°42 - PAINT ROLLERCORRECION - Con medidas.xlsx",
            "15-8-2026/Lista_de_precios_Mayorista_N_42_-_PAINT_ROLLER_CORRECION - Corregida por claude.xlsx"})
    void unaCorreccionConOtrosEncabezadosSeRechaza(String archivo) throws IOException {
        ResultadoImportacion r = procesar(PAINT_ROLLER, archivo);

        assertThat(r.estado()).isEqualTo(EstadoImportacion.RECHAZADA);
        assertThat(r.filas()).isEmpty();
        assertThat(r.diferencias()).hasSize(6).allMatch(Diferencia::bloqueante);
    }

    private static ResultadoImportacion procesar(long perfil, String archivo) throws IOException {
        try (InputStream in = Files.newInputStream(VERSIONES.resolve(archivo))) {
            return servicio.preview(perfil, archivo, in);
        }
    }
}
