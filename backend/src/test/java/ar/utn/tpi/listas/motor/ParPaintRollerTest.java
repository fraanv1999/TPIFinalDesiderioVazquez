package ar.utn.tpi.listas.motor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import ar.utn.tpi.listas.excel.HojaExcel;
import ar.utn.tpi.listas.excel.LectorExcel;
import ar.utn.tpi.listas.perfil.RepositorioPerfiles;

/**
 * Par original / corregido de Paint Roller (02/10). La corrección manual completó el nombre del grupo
 * en cada fila y le agregó la medida: el motor lo reproduce con RELLENAR_ABAJO y CONCATENAR.
 * Los archivos son de un cliente y no están en el repo: se leen de {@code DATASET_EXTERNO}.
 */
@EnabledIfEnvironmentVariable(named = "DATASET_EXTERNO", matches = ".+")
class ParPaintRollerTest {

    private static final String HOJA = " Paint Roller ";

    @Test
    void elMotorReproduceLaCorreccionManual() throws IOException {
        Path carpeta = Path.of(System.getenv("DATASET_EXTERNO"), "02-10-2026");
        Path original = carpeta.resolve("Lista de precios Mayorista - PAINT ROLLER.xlsx");
        Path corregido = carpeta.resolve("Lista_de_precios_Mayorista_-_PAINT_ROLLER_CORRECION.xlsx");
        assumeTrue(Files.exists(original) && Files.exists(corregido), "faltan los archivos del 02/10");

        ResultadoImportacion r;
        try (InputStream in = Files.newInputStream(original)) {
            r = new ServicioImportacion(new RepositorioPerfiles()).preview("paint-roller", original.toString(), in);
        }
        HojaExcel manual;
        try (InputStream in = Files.newInputStream(corregido)) {
            manual = LectorExcel.leer(in, HOJA).hoja().orElseThrow();
        }

        assertThat(r.estado()).isEqualTo(EstadoImportacion.PREVIEW);
        List<String> distintos = new ArrayList<>();
        int validas = 0;
        for (FilaResultado f : r.filas()) {
            if (f.estado() != EstadoFila.VALIDA) continue;
            validas++;
            int n = f.numeroFila();
            String nombre = (String) f.datosResultado().get("nombre");
            if (!normalizar(nombre).equals(normalizar(manual.celda(n, "A")))) {
                distintos.add("A" + n + ": manual '" + manual.celda(n, "A") + "', motor '" + nombre + "'");
            }
            if (((BigDecimal) f.datosResultado().get("precio")).compareTo((BigDecimal) manual.celda(n, "F")) != 0) {
                distintos.add("F" + n + ": manual " + manual.celda(n, "F") + ", motor " + f.datosResultado().get("precio"));
            }
        }
        assertThat(validas).isEqualTo(166);
        assertThat(distintos).isEmpty();
        // Filas que la corrección manual tuvo que resolver a mano: precio sin código y "SIN STOCK".
        assertThat(r.filas()).filteredOn(f -> f.estado() == EstadoFila.ERROR)
                .extracting(FilaResultado::numeroFila).containsExactly(66, 81, 214, 215);
        assertThat(r.repetidos()).extracting(CodigoRepetido::codigo).contains("RAPIFIX");
    }

    /** Espacios repetidos dentro del nombre ("PINCEL V4  - SERIE 40") no cuentan como diferencia. */
    private static String normalizar(Object celda) {
        return celda == null ? null : celda.toString().strip().replaceAll("\\s+", " ");
    }
}
