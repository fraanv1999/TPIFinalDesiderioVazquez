package ar.utn.tpi.listas.motor;

public enum TipoDiferencia {
    HOJA_INEXISTENTE, ENCABEZADO_MODIFICADO, COLUMNA_FALTANTE, POSICION_INESPERADA,
    /** Todos los encabezados están, pero en otra fila dentro del margen del perfil. Es un aviso. */
    ENCABEZADO_DESPLAZADO
}
