package ar.utn.tpi.listas.api;

import static org.hamcrest.Matchers.contains;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.jayway.jsonpath.JsonPath;

class SeguridadApiTest extends ApiTest {

    private static final RequestPostProcessor OPERADOR_OK = httpBasic(OPERADOR, "operador123");
    private static final RequestPostProcessor ADMIN_OK = httpBasic(ADMIN, "admin123");

    @Override
    boolean comoAdmin() {
        return false;
    }

    @Test
    void sinCredencialesEs401SinElCuadroDelNavegador() throws Exception {
        mvc.perform(get("/api/perfiles").with(anonymous()))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("WWW-Authenticate"))
                .andExpect(jsonPath("$.detail").value("Email o contraseña incorrectos"));
    }

    @Test
    void contrasenaIncorrecta() throws Exception {
        mvc.perform(get("/api/auth/yo").with(httpBasic(ADMIN, "otra")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void yoDevuelveElUsuarioAutenticado() throws Exception {
        mvc.perform(get("/api/auth/yo").with(httpBasic("OPERADOR@tpi.local", "operador123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Operador"))
                .andExpect(jsonPath("$.rol").value("OPERADOR"));
    }

    @Test
    void laSaludEsPublica() throws Exception {
        mvc.perform(get("/api/salud").with(anonymous())).andExpect(status().isOk());
    }

    @Test
    void unOperadorNoAdministraProveedores() throws Exception {
        mvc.perform(get("/api/proveedores").with(OPERADOR_OK)).andExpect(status().isOk());
        mvc.perform(post("/api/proveedores").with(OPERADOR_OK).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"Nuevo\", \"activo\": true}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Tu usuario no tiene permiso para esta acción"));
    }

    @Test
    void laImportacionQuedaANombreDeQuienLaHizo() throws Exception {
        Path kallay = Path.of("..", "dataset-example", "dataset-previous-platform", "KALLAYCORREGIDO.xlsx");
        MockMultipartFile parte = new MockMultipartFile("archivo", "KALLAYCORREGIDO.xlsx", "application/octet-stream",
                Files.readAllBytes(kallay));
        String json = mvc.perform(multipart("/api/importaciones/preview").file(parte).param("perfil", "3").with(OPERADOR_OK))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long id = ((Number) JsonPath.read(json, "$.id")).longValue();

        mvc.perform(get("/api/importaciones").with(ADMIN_OK))
                .andExpect(jsonPath("$[?(@.id == " + id + ")].usuario").value(contains("Operador")));
    }

    @Test
    void elPreflightDeCorsNoPideCredenciales() throws Exception {
        mvc.perform(options("/api/perfiles").with(anonymous())
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }
}
