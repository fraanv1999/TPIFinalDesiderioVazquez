package ar.utn.tpi.listas.api;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.jayway.jsonpath.JsonPath;

class ImportacionesApiTest extends ApiTest {

    private static final Path PROVEEDORES = Path.of("..", "dataset-example", "dataset-providers");
    private static final Path ANTERIOR = Path.of("..", "dataset-example", "dataset-previous-platform");

    private long subir(long perfil, Path archivo) throws Exception {
        MockMultipartFile parte = new MockMultipartFile("archivo", archivo.getFileName().toString(),
                "application/octet-stream", Files.readAllBytes(archivo));
        String json = mvc.perform(multipart("/api/importaciones/preview").file(parte).param("perfil", String.valueOf(perfil)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(json, "$.id")).longValue();
    }

    @Test
    void laVistaPreviaSeGuardaYSePuedeVolverALeer() throws Exception {
        long id = subir(1, PROVEEDORES.resolve("Lista Ferreteria 19 08.xlsx"));

        mvc.perform(get("/api/importaciones/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PREVIEW"))
                .andExpect(jsonPath("$.resumen.validas").value(1412))
                .andExpect(jsonPath("$.resumen.titulos").value(180))
                .andExpect(jsonPath("$.filas.length()").value(1953))
                // Fila 12: EP52, con los campos en el orden del formato destino aunque JSONB los reordene
                .andExpect(jsonPath("$.filas[4].numeroFila").value(12))
                .andExpect(jsonPath("$.filas[4].datosResultado.codigo").value("EP52"))
                .andExpect(jsonPath("$.filas[4].datosResultado.precio").value(1474.41))
                .andExpect(jsonPath("$.filas[4].datosResultado.categoria").value("PILAS ENERGIZER ALCALINAS"));

        mvc.perform(get("/api/importaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + id + ")].proveedor").value(contains("Ferretería")))
                .andExpect(jsonPath("$[?(@.id == " + id + ")].usuario").value(contains("Administrador")));
    }

    @Test
    void losErroresDeFilaSeGuardanConSuCampo() throws Exception {
        long id = subir(4, ANTERIOR.resolve("LISTA BULONESCORRECION.2026.xlsx"));
        mvc.perform(get("/api/importaciones/{id}", id))
                .andExpect(jsonPath("$.resumen.errores").value(5))
                .andExpect(jsonPath("$.filas[?(@.numeroFila == 148)].errores[0].campo").value(contains("stock")))
                .andExpect(jsonPath("$.filas[?(@.numeroFila == 148)].errores[0].mensaje")
                        .value(contains("stock: '10KG' no es un número entero")));
    }

    @Test
    void confirmarSoloUnaVistaPrevia() throws Exception {
        long id = subir(3, ANTERIOR.resolve("KALLAYCORREGIDO.xlsx"));
        mvc.perform(post("/api/importaciones/{id}/confirmar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"));
        mvc.perform(post("/api/importaciones/{id}/confirmar", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("La importación " + id
                        + " está CONFIRMADA: solo se puede confirmar o cancelar una vista previa"));
        mvc.perform(post("/api/importaciones/{id}/cancelar", id)).andExpect(status().isConflict());
    }

    @Test
    void cancelar() throws Exception {
        long id = subir(5, ANTERIOR.resolve("MATIENZO LISTACORRECCION.xlsx"));
        mvc.perform(post("/api/importaciones/{id}/cancelar", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));
    }

    @Test
    void unaEstructuraDistintaSeGuardaComoRechazada() throws Exception {
        // Perfil de Ferretería (hoja LSTPRE) con la planilla de Kallay (hoja Hoja1)
        long id = subir(1, ANTERIOR.resolve("KALLAYCORREGIDO.xlsx"));
        mvc.perform(get("/api/importaciones/{id}", id))
                .andExpect(jsonPath("$.estado").value("RECHAZADA"))
                .andExpect(jsonPath("$.diferencias[0].tipo").value("HOJA_INEXISTENTE"))
                .andExpect(jsonPath("$.diferencias[0].mensaje").value("No existe la hoja 'LSTPRE'. Hojas del archivo: Hoja1"))
                .andExpect(jsonPath("$.filas.length()").value(0));
        mvc.perform(post("/api/importaciones/{id}/confirmar", id)).andExpect(status().isConflict());
    }

    @Test
    void importacionInexistente() throws Exception {
        mvc.perform(get("/api/importaciones/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe la importación 999999"));
    }

    @Test
    void perfiles() throws Exception {
        mvc.perform(get("/api/perfiles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(hasItem(2)));
        mvc.perform(get("/api/perfiles/2"))
                .andExpect(jsonPath("$.hoja").value(" Paint Roller "));
    }
}
