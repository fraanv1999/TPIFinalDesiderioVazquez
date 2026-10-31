package ar.utn.tpi.listas.motor;

/** Algo del archivo que no coincide con el perfil. {@code columna} es null si falta la hoja. */
public record Diferencia(TipoDiferencia tipo, String columna, String esperado, String encontrado, String mensaje) {
}
