package ar.utn.tpi.listas.importacion;

import java.time.OffsetDateTime;

import ar.utn.tpi.listas.motor.EstadoImportacion;

/** Una fila del historial. */
public record ImportacionResumen(long id,
                                 long perfilId,
                                 String perfil,
                                 String proveedor,
                                 String archivo,
                                 EstadoImportacion estado,
                                 int validas,
                                 int ignoradas,
                                 int errores,
                                 String usuario,
                                 OffsetDateTime creadoEn) {
}
