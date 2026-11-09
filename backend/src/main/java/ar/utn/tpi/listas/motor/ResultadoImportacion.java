package ar.utn.tpi.listas.motor;

import java.util.List;

/**
 * {@code diferencias} incluye avisos no bloqueantes; {@code repetidos} son avisos sobre las filas válidas.
 * {@code id} es null hasta que la importación se guarda.
 */
public record ResultadoImportacion(Long id,
                                   long perfil,
                                   String archivo,
                                   EstadoImportacion estado,
                                   List<Diferencia> diferencias,
                                   Resumen resumen,
                                   List<CodigoRepetido> repetidos,
                                   List<FilaResultado> filas) {

    public ResultadoImportacion conId(long id) {
        return new ResultadoImportacion(id, perfil, archivo, estado, diferencias, resumen, repetidos, filas);
    }
}
