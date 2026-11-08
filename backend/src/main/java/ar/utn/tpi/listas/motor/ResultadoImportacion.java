package ar.utn.tpi.listas.motor;

import java.util.List;

/** {@code diferencias} incluye avisos no bloqueantes; {@code repetidos} son avisos sobre las filas válidas. */
public record ResultadoImportacion(String perfil,
                                   String archivo,
                                   EstadoImportacion estado,
                                   List<Diferencia> diferencias,
                                   Resumen resumen,
                                   List<CodigoRepetido> repetidos,
                                   List<FilaResultado> filas) {
}
