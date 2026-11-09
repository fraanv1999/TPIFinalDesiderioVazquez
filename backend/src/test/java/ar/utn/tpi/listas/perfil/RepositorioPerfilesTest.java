package ar.utn.tpi.listas.perfil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;

import ar.utn.tpi.listas.BaseDePrueba;

/** Lee los perfiles de database/seed.sql. */
class RepositorioPerfilesTest {

    private final RepositorioPerfiles repositorio = new RepositorioPerfiles(JdbcClient.create(BaseDePrueba.dataSource()));

    @Test
    void listaLosCincoPerfilesDelSeed() {
        assertThat(repositorio.perfiles()).extracting(Perfil::proveedor)
                .containsExactly("Bulones", "Ferretería", "Kallay", "Matienzo", "Paint Roller");
    }

    @Test
    void perfilCompletoConColumnasTransformacionesYReglas() {
        Perfil p = repositorio.perfil(2);
        assertThat(p.hoja()).isEqualTo(" Paint Roller ");
        assertThat(p.filaEncabezado()).isEqualTo(24);
        assertThat(p.margenEncabezado()).isEqualTo(3);
        assertThat(p.campoClave()).isEqualTo("codigo");
        assertThat(p.campoCategoria()).isEqualTo("categoria");
        assertThat(p.columnas()).extracting(MapeoColumna::columna).containsExactly("A", "B", "C", "D", "E", "F", "G");
        assertThat(p.columnas().get(0).transformaciones()).containsExactly(Transformacion.TRIM, Transformacion.RELLENAR_ABAJO);
        assertThat(p.columnas().get(3).ignorada()).isTrue();
        assertThat(p.reglas()).extracting(ReglaCalculo::operacion).containsExactly(Operacion.CONCATENAR, Operacion.COPIAR);
    }

    @Test
    void losPorcentajesSonFracciones() {
        assertThat(repositorio.perfil(3).reglas().get(0).valor()).isEqualTo(new BigDecimal("0.04"));
    }

    @Test
    void elFormatoTieneLosCamposEnOrden() {
        FormatoDestino f = repositorio.formatoDe(repositorio.perfil(1));
        assertThat(f.nombre()).isEqualTo("organizalo");
        assertThat(f.campos()).hasSize(11);
        assertThat(f.campos().get(0).clave()).isEqualTo("codigo");
        assertThat(f.campo("precio_costo").orElseThrow().exportado()).isFalse();
    }

    @Test
    void perfilInexistente() {
        assertThatThrownBy(() -> repositorio.perfil(999)).isInstanceOf(PerfilNoEncontradoException.class);
    }
}
