package ar.utn.tpi.listas.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.jayway.jsonpath.JsonPath;

class ExportacionApiTest extends ApiTest {

    private static final Path DATASET = Path.of("..", "dataset-example");

    private long subir(long perfil, String archivo) throws Exception {
        Path ruta = DATASET.resolve(archivo);
        MockMultipartFile parte = new MockMultipartFile("archivo", ruta.getFileName().toString(),
                "application/octet-stream", Files.readAllBytes(ruta));
        String json = mvc.perform(multipart("/api/importaciones/preview").file(parte).param("perfil", String.valueOf(perfil)))
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(json, "$.id")).longValue();
    }

    private Sheet descargar(String url, long id) throws Exception {
        byte[] xlsx = mvc.perform(get(url, id))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andReturn().getResponse().getContentAsByteArray();
        Workbook libro = WorkbookFactory.create(new ByteArrayInputStream(xlsx));
        return libro.getSheetAt(0);
    }

    private static Row buscar(Sheet hoja, String codigo) {
        for (Row fila : hoja) {
            if (codigo.equals(texto(fila, 0))) return fila;
        }
        throw new AssertionError("No está " + codigo);
    }

    private static String texto(Row fila, int columna) {
        return new DataFormatter().formatCellValue(fila.getCell(columna));
    }

    @Test
    void exportaLasFilasValidasEnElFormatoOrganizalo() throws Exception {
        long id = subir(1, "dataset-providers/Lista Ferreteria 19 08.xlsx");
        mvc.perform(post("/api/importaciones/{id}/confirmar", id)).andExpect(status().isOk());

        mvc.perform(get("/api/importaciones/{id}/exportar", id))
                .andExpect(header().string("Content-Disposition",
                        containsString("filename=\"ferreteria-lista-general-" + id + ".xlsx\"")));
        Sheet hoja = descargar("/api/importaciones/{id}/exportar", id);

        Row encabezado = hoja.getRow(0);
        // precio_costo es auxiliar: no se exporta
        assertThat(encabezado.getLastCellNum()).isEqualTo((short) 10);
        assertThat(texto(encabezado, 0)).isEqualTo("Codigo");
        assertThat(texto(encabezado, 9)).isEqualTo("Precio");
        assertThat(hoja.getLastRowNum()).isEqualTo(1412);

        Row ep52 = buscar(hoja, "EP52");
        assertThat(texto(ep52, 3)).isEqualTo("PILAS ENERGIZER ALCALINAS");
        assertThat(ep52.getCell(9).getNumericCellValue()).isEqualTo(1474.41);
    }

    @Test
    void losDecimalesSeRedondeanAlExportar() throws Exception {
        long id = subir(3, "dataset-previous-platform/KALLAYCORREGIDO.xlsx");
        mvc.perform(post("/api/importaciones/{id}/confirmar", id)).andExpect(status().isOk());
        Row k4002 = descargar("/api/importaciones/{id}/exportar", id).getRow(1);
        assertThat(texto(k4002, 0)).isEqualTo("K4002");
        // precio de venta: 46869.108 en el motor (sin redondeo)
        assertThat(k4002.getCell(8).getNumericCellValue()).isEqualTo(46869.11);
        assertThat(texto(k4002, 7)).isEqualTo("4");
    }

    @Test
    void soloSeExportaUnaImportacionConfirmada() throws Exception {
        long id = subir(5, "dataset-previous-platform/MATIENZO LISTACORRECCION.xlsx");
        mvc.perform(get("/api/importaciones/{id}/exportar", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("La importación " + id
                        + " está PREVIEW: solo se exporta una importación confirmada"));
    }

    @Test
    void elReporteDeErroresTraeElMotivoYLaFilaOriginal() throws Exception {
        long id = subir(4, "dataset-previous-platform/LISTA BULONESCORRECION.2026.xlsx");
        mvc.perform(get("/api/importaciones/{id}/errores", id))
                .andExpect(header().string("Content-Disposition", containsString("bulones-corregido-" + id + "-errores.xlsx")));
        Sheet errores = descargar("/api/importaciones/{id}/errores", id);

        assertThat(texto(errores.getRow(0), 0)).isEqualTo("Fila");
        assertThat(texto(errores.getRow(0), 2)).isEqualTo("A");
        assertThat(errores.getLastRowNum()).isEqualTo(5);
        Row f148 = errores.getRow(1);
        assertThat(texto(f148, 0)).isEqualTo("148");
        assertThat(texto(f148, 1)).isEqualTo("stock: '10KG' no es un número entero");
        assertThat(texto(f148, 2)).isEqualTo("F0144");
    }

    @Test
    void unaImportacionRechazadaInformaLasDiferenciasEnAvisos() throws Exception {
        long id = subir(1, "dataset-previous-platform/KALLAYCORREGIDO.xlsx");
        byte[] xlsx = mvc.perform(get("/api/importaciones/{id}/errores", id)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        Sheet avisos = WorkbookFactory.create(new ByteArrayInputStream(xlsx)).getSheet("Avisos");
        assertThat(texto(avisos.getRow(1), 0)).isEqualTo("HOJA_INEXISTENTE");
        assertThat(texto(avisos.getRow(1), 1)).isEqualTo("Sí");
    }
}
