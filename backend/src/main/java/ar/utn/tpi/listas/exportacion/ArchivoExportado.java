package ar.utn.tpi.listas.exportacion;

/** Un .xlsx generado, listo para descargar. */
public record ArchivoExportado(String nombre, byte[] contenido) {

    public static final String TIPO = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
}
