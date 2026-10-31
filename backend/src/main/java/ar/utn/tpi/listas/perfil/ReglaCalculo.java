package ar.utn.tpi.listas.perfil;

import java.math.BigDecimal;

/** Un paso de la cadena de cálculo. Se aplican en el orden en que aparecen en el perfil. */
public record ReglaCalculo(String resultado, Operacion operacion, String base, String secundario, BigDecimal valor) {
}
