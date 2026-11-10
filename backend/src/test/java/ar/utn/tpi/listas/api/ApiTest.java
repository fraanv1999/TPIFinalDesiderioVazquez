package ar.utn.tpi.listas.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import ar.utn.tpi.listas.BaseDePrueba;

/** Pruebas de la API contra el PostgreSQL embebido. Cada pedido va como el admin del seed, salvo que se indique otro. */
@SpringBootTest
abstract class ApiTest {

    static final String ADMIN = "admin@tpi.local";
    static final String OPERADOR = "operador@tpi.local";

    @DynamicPropertySource
    static void base(DynamicPropertyRegistry registro) {
        BaseDePrueba.propiedades(registro);
    }

    @Autowired
    WebApplicationContext contexto;

    MockMvc mvc;

    /** false: cada pedido indica sus credenciales (el default pisaría las del pedido). */
    boolean comoAdmin() {
        return true;
    }

    @BeforeEach
    void configurarMvc() {
        var builder = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity());
        if (comoAdmin()) {
            builder.defaultRequest(get("/").with(httpBasic(ADMIN, "admin123")));
        }
        mvc = builder.build();
    }
}
