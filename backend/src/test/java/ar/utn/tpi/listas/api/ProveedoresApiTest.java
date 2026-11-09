package ar.utn.tpi.listas.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.jayway.jsonpath.JsonPath;

class ProveedoresApiTest extends ApiTest {

    private String crear(String json) throws Exception {
        return mvc.perform(post("/api/proveedores").contentType(MediaType.APPLICATION_JSON).content(json))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void listaLosDelSeedConSusPerfiles() throws Exception {
        mvc.perform(get("/api/proveedores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nombre == 'Paint Roller')].perfiles").value(org.hamcrest.Matchers.contains(1)));
    }

    @Test
    void altaEdicionYBaja() throws Exception {
        long id = ((Number) JsonPath.read(crear("{\"nombre\": \"  Acindar \", \"activo\": true}"), "$.id")).longValue();
        mvc.perform(get("/api/proveedores/{id}", id))
                .andExpect(jsonPath("$.nombre").value("Acindar"))
                .andExpect(jsonPath("$.perfiles").value(0));

        mvc.perform(put("/api/proveedores/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"Acindar SA\", \"activo\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Acindar SA"))
                .andExpect(jsonPath("$.activo").value(false));

        mvc.perform(delete("/api/proveedores/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/proveedores/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void elNombreEsObligatorioYUnico() throws Exception {
        mvc.perform(post("/api/proveedores").contentType(MediaType.APPLICATION_JSON).content("{\"nombre\": \" \", \"activo\": true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("El nombre es obligatorio"));
        mvc.perform(post("/api/proveedores").contentType(MediaType.APPLICATION_JSON).content("{\"nombre\": \"Kallay\", \"activo\": true}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Ya existe un proveedor llamado Kallay"));
    }

    @Test
    void unProveedorConPerfilesNoSeBorra() throws Exception {
        mvc.perform(delete("/api/proveedores/3"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Kallay tiene 1 perfil(es) de importación: desactivalo en lugar de borrarlo"));
    }
}
