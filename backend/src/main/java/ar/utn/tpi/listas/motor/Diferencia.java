package ar.utn.tpi.listas.motor;

/**
 * Algo del archivo que no coincide con el perfil. {@code columna} es null si la diferencia es de toda
 * la hoja. Si alguna es {@code bloqueante}, la importación se rechaza; si no, es un aviso.
 */
public record Diferencia(TipoDiferencia tipo, boolean bloqueante, String columna, String esperado, String encontrado,
                         String mensaje) {
}
