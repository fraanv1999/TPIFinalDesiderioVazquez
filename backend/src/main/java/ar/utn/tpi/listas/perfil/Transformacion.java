package ar.utn.tpi.listas.perfil;

/**
 * Limpieza aplicada a una celda antes de convertirla al tipo del campo destino.
 * A_DECIMAL y A_ENTERO aceptan números escritos como texto ("1.474,41"); sin ellas,
 * un texto en un campo numérico es un error.
 */
public enum Transformacion {
    TRIM, MAYUSCULAS, A_DECIMAL, A_ENTERO
}
