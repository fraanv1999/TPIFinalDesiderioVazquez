package ar.utn.tpi.listas.seguridad;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** Usuario de la tabla {@code usuario}. El id queda a mano para registrar quién hizo cada importación. */
public record UsuarioAutenticado(long id, String nombre, String email, String passwordHash, Rol rol, boolean activo)
        implements UserDetails {

    public enum Rol {
        ADMIN, OPERADOR
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }
}
