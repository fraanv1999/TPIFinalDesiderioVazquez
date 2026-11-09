package ar.utn.tpi.listas.proveedor;

/**
 * Proveedor que envía listas. Solo nombre y estado: los datos de contacto están en el sistema destino.
 * {@code perfiles} es la cantidad de perfiles de importación que tiene.
 */
public record Proveedor(long id, String nombre, boolean activo, int perfiles) {
}
