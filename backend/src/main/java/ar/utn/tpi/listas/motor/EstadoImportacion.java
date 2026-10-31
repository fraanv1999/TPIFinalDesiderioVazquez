package ar.utn.tpi.listas.motor;

public enum EstadoImportacion {
    /** Filas procesadas, a la espera de confirmación. */
    PREVIEW,
    /** La estructura no coincide con el perfil: no se procesó ninguna fila. */
    RECHAZADA
}
