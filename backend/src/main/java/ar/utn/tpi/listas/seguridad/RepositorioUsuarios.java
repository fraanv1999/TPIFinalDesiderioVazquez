package ar.utn.tpi.listas.seguridad;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

/** Spring Security busca acá al usuario por email; la contraseña se compara contra el hash BCrypt. */
@Repository
public class RepositorioUsuarios implements UserDetailsService {

    private final JdbcClient jdbc;

    public RepositorioUsuarios(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public UsuarioAutenticado loadUserByUsername(String email) {
        return jdbc.sql("""
                        SELECT id, nombre, email, password_hash, rol::text AS rol, activo
                        FROM usuario WHERE lower(email) = lower(?)""")
                .param(email.strip())
                .query((rs, n) -> new UsuarioAutenticado(rs.getLong("id"), rs.getString("nombre"), rs.getString("email"),
                        rs.getString("password_hash"), UsuarioAutenticado.Rol.valueOf(rs.getString("rol")),
                        rs.getBoolean("activo")))
                .optional()
                .orElseThrow(() -> new UsernameNotFoundException(email));
    }
}
