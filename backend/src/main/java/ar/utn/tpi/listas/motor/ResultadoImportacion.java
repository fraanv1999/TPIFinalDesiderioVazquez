package ar.utn.tpi.listas.motor;

import java.util.List;

public record ResultadoImportacion(String perfil,
                                   String archivo,
                                   EstadoImportacion estado,
                                   List<Diferencia> diferencias,
                                   Resumen resumen,
                                   List<FilaResultado> filas) {
}
