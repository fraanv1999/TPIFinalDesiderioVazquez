package ar.utn.tpi.listas.base;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import ar.utn.tpi.listas.BaseDePrueba;

@SpringBootTest
class InicializadorBaseTest {

    @DynamicPropertySource
    static void base(DynamicPropertyRegistry registro) {
        BaseDePrueba.propiedades(registro);
    }

    @Autowired
    JdbcClient jdbc;

    @Test
    void laAplicacionArrancaConElEsquemaYLosDatosDeEjemplo() {
        assertThat(jdbc.sql("SELECT count(*) FROM perfil_importacion").query(Long.class).single()).isEqualTo(5);
        assertThat(jdbc.sql("SELECT count(*) FROM proveedor").query(Long.class).single()).isGreaterThanOrEqualTo(5);
    }

    @Test
    void siElEsquemaYaExisteNoSeVuelveAEjecutar() {
        assertThat(InicializadorBase.inicializar(BaseDePrueba.dataSource())).isFalse();
    }
}
