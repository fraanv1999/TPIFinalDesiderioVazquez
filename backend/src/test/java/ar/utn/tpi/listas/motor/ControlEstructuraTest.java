package ar.utn.tpi.listas.motor;

import static ar.utn.tpi.listas.motor.Datos.fila;
import static ar.utn.tpi.listas.motor.Datos.hoja;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import ar.utn.tpi.listas.excel.HojaExcel;
import ar.utn.tpi.listas.excel.LibroExcel;
import ar.utn.tpi.listas.perfil.Perfil;

/** Los 5 tipos de diferencia de docs/modulos.md, "Control de estructura". */
class ControlEstructuraTest {

    private final Perfil perfil = Datos.perfil(2, null, List.of());

    private Estructura verificar(HojaExcel hoja) {
        return ControlEstructura.verificar(perfil, new LibroExcel(List.of("Hoja1"), Optional.of(hoja)));
    }

    @Test
    void coincideSinDistinguirMayusculasTildesNiEspacios() {
        Estructura e = verificar(hoja(fila(2, "CODIGO", "  descripcion ", "precio", "OBSERVACIONES")));
        assertThat(e.diferencias()).isEmpty();
        assertThat(e.bloqueada()).isFalse();
    }

    @Test
    void hojaInexistente() {
        Estructura e = ControlEstructura.verificar(perfil, new LibroExcel(List.of("LSTPRE", "SCHNEIDER"), Optional.empty()));
        assertThat(e.bloqueada()).isTrue();
        assertThat(e.diferencias()).singleElement().satisfies(d -> {
            assertThat(d.tipo()).isEqualTo(TipoDiferencia.HOJA_INEXISTENTE);
            assertThat(d.encontrado()).isEqualTo("LSTPRE, SCHNEIDER");
        });
    }

    @Test
    void encabezadoModificado() {
        Estructura e = verificar(hoja(fila(2, "Código", "Descripción", "PRECIO U$S", "Observaciones")));
        assertThat(e.bloqueada()).isTrue();
        assertThat(e.diferencias()).singleElement().satisfies(d -> {
            assertThat(d.tipo()).isEqualTo(TipoDiferencia.ENCABEZADO_MODIFICADO);
            assertThat(d.columna()).isEqualTo("C");
            assertThat(d.encontrado()).isEqualTo("PRECIO U$S");
        });
    }

    @Test
    void columnaFaltante() {
        Estructura e = verificar(hoja(fila(2, "Código", "Descripción", "Precio")));
        assertThat(e.diferencias()).singleElement().satisfies(d -> {
            assertThat(d.tipo()).isEqualTo(TipoDiferencia.COLUMNA_FALTANTE);
            assertThat(d.columna()).isEqualTo("D");
            assertThat(d.bloqueante()).isTrue();
        });
    }

    @Test
    void posicionInesperada() {
        Estructura e = verificar(hoja(fila(2, "Código", "Descripción", "Observaciones", "Precio")));
        assertThat(e.diferencias()).extracting(Diferencia::tipo)
                .containsExactly(TipoDiferencia.POSICION_INESPERADA, TipoDiferencia.POSICION_INESPERADA);
        assertThat(e.diferencias().get(0).encontrado()).isEqualTo("D");
    }

    @Test
    void encabezadoDesplazadoDentroDelMargenEsUnAviso() {
        Estructura e = verificar(hoja(fila(3, "Código", "Descripción", "Precio", "Observaciones")));
        assertThat(e.bloqueada()).isFalse();
        assertThat(e.desplazamiento()).isEqualTo(1);
        assertThat(e.diferencias()).singleElement().satisfies(d -> {
            assertThat(d.tipo()).isEqualTo(TipoDiferencia.ENCABEZADO_DESPLAZADO);
            assertThat(d.mensaje()).contains("desde la fila 4");
        });
    }

    @Test
    void encabezadoDesplazadoHaciaArriba() {
        assertThat(verificar(hoja(fila(1, "Código", "Descripción", "Precio", "Observaciones"))).desplazamiento())
                .isEqualTo(-1);
    }

    @Test
    void fueraDelMargenSeRechaza() {
        Estructura e = verificar(hoja(fila(5, "Código", "Descripción", "Precio", "Observaciones")));
        assertThat(e.bloqueada()).isTrue();
        assertThat(e.diferencias()).extracting(Diferencia::tipo).containsOnly(TipoDiferencia.COLUMNA_FALTANTE);
    }
}
