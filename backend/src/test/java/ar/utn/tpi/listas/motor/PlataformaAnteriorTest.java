package ar.utn.tpi.listas.motor;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.springframework.jdbc.core.simple.JdbcClient;

import ar.utn.tpi.listas.BaseDePrueba;
import ar.utn.tpi.listas.perfil.RepositorioPerfiles;

/**
 * Las planillas corregidas a mano en la plataforma anterior traen el costo (C) y, al lado, lo que
 * calculó Excel (D a G e I). Perfiles 3 a 5 de database/seed.sql.
 * Las reglas de cada perfil tienen que dar los mismos valores.
 * Bulones trae 5 filas que el motor rechaza a propósito: stock "10KG" (4) y un precio de costo 0.
 * Además, en las filas 216 a 218 la fórmula del ingreso bruto se pisó a mano con un número fijo
 * (D216 = 9700 con un costo de 10290): ahí el motor da distinto y el error es de la planilla.
 */
class PlataformaAnteriorTest {

    private static final Path CARPETA = Path.of("..", "dataset-example", "dataset-previous-platform");
    /** Excel guarda doubles: se tolera la diferencia de redondeo de la última cifra. */
    private static final BigDecimal TOLERANCIA = new BigDecimal("0.000001");
    /** Columna del Excel → campo calculado por el perfil. */
    private static final Map<String, String> CALCULADOS = Map.of(
            "D", "ingreso_bruto",
            "E", "ganancia",
            "F", "iva",
            "G", "costo_mas_iva",
            "I", "precio_venta");

    private static final RepositorioPerfiles PERFILES = new RepositorioPerfiles(JdbcClient.create(BaseDePrueba.dataSource()));

    @ParameterizedTest(name = "perfil {0}: {1}")
    @CsvSource({
            "3, KALLAYCORREGIDO.xlsx,             17,  0, ''",
            "4, LISTA BULONESCORRECION.2026.xlsx, 368, 5, '216 217 218'",
            "5, MATIENZO LISTACORRECCION.xlsx,    25,  0, ''"})
    void lasReglasDanLoMismoQueExcel(long perfil, String archivo, int validas, int errores, String pisadasAMano)
            throws IOException {
        ResultadoImportacion r;
        try (InputStream in = Files.newInputStream(CARPETA.resolve(archivo))) {
            r = new ServicioImportacion(PERFILES).preview(perfil, archivo, in);
        }
        assertThat(r.estado()).isEqualTo(EstadoImportacion.PREVIEW);
        assertThat(r.resumen().errores()).isEqualTo(errores);
        assertThat(r.resumen().validas()).isEqualTo(validas);

        Set<Integer> distintas = new TreeSet<>();
        int comparados = 0;
        for (FilaResultado f : r.filas()) {
            if (f.estado() != EstadoFila.VALIDA) continue;
            for (var c : CALCULADOS.entrySet()) {
                if (!(f.datosCrudos().get(c.getKey()) instanceof BigDecimal excel)) continue;
                BigDecimal motor = (BigDecimal) f.datosResultado().get(c.getValue());
                comparados++;
                if (motor == null || motor.subtract(excel).abs().compareTo(TOLERANCIA) > 0) {
                    distintas.add(f.numeroFila());
                }
            }
        }
        assertThat(comparados).isGreaterThanOrEqualTo(validas * 4);
        assertThat(distintas).containsExactlyElementsOf(filas(pisadasAMano));
    }

    private static List<Integer> filas(String numeros) {
        return numeros.isBlank() ? List.of() : Arrays.stream(numeros.split(" ")).map(Integer::valueOf).toList();
    }
}
