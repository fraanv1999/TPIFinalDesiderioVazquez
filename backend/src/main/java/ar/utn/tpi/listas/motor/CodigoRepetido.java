package ar.utn.tpi.listas.motor;

import java.util.List;

/** Aviso: el mismo valor del campo clave aparece en varias filas válidas. */
public record CodigoRepetido(String codigo, List<Integer> filas) {
}
