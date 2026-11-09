package ar.utn.tpi.listas.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import ar.utn.tpi.listas.excel.ArchivoInvalidoException;
import ar.utn.tpi.listas.importacion.EstadoInvalidoException;
import ar.utn.tpi.listas.importacion.ImportacionNoEncontradaException;
import ar.utn.tpi.listas.perfil.PerfilInvalidoException;
import ar.utn.tpi.listas.perfil.PerfilNoEncontradoException;
import ar.utn.tpi.listas.proveedor.ProveedorEnUsoException;
import ar.utn.tpi.listas.proveedor.ProveedorNoEncontradoException;

/** Los errores llegan al frontend como ProblemDetail, con {@code detail} listo para mostrar. */
@RestControllerAdvice
public class ManejoErrores {

    @ExceptionHandler({PerfilNoEncontradoException.class, ImportacionNoEncontradaException.class,
            ProveedorNoEncontradoException.class})
    ProblemDetail noEncontrado(RuntimeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(PerfilInvalidoException.class)
    ProblemDetail perfilInvalido(PerfilInvalidoException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
    }

    @ExceptionHandler(ArchivoInvalidoException.class)
    ProblemDetail archivoInvalido(ArchivoInvalidoException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler({EstadoInvalidoException.class, ProveedorEnUsoException.class})
    ProblemDetail conflicto(RuntimeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    /** Datos inválidos en el cuerpo: se informa el primer motivo. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail datosInvalidos(MethodArgumentNotValidException e) {
        String motivo = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getDefaultMessage())
                .findFirst()
                .orElse("Datos inválidos");
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, motivo);
    }
}
