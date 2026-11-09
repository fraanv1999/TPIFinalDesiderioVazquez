package ar.utn.tpi.listas.proveedor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Lo que se envía para crear o editar un proveedor. */
public record DatosProveedor(@NotBlank(message = "El nombre es obligatorio")
                             @Size(max = 120, message = "El nombre puede tener hasta 120 caracteres") String nombre,
                             boolean activo) {
}
