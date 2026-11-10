package ar.utn.tpi.listas.api;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.utn.tpi.listas.seguridad.UsuarioAutenticado;

@RestController
public class AuthController {

    /** El frontend lo usa para validar el login: con credenciales correctas devuelve el usuario. */
    @GetMapping("/api/auth/yo")
    public Usuario yo(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return new Usuario(usuario.id(), usuario.nombre(), usuario.email(), usuario.rol().name());
    }

    /** Público: lo consulta Render para saber si el servicio está vivo. */
    @GetMapping("/api/salud")
    public Map<String, String> salud() {
        return Map.of("estado", "ok");
    }

    public record Usuario(long id, String nombre, String email, String rol) {
    }
}
