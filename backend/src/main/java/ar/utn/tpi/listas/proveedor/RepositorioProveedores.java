package ar.utn.tpi.listas.proveedor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioProveedores {

    private static final String SELECT = """
            SELECT pr.id, pr.nombre, pr.activo, count(p.id) AS perfiles
            FROM proveedor pr LEFT JOIN perfil_importacion p ON p.proveedor_id = pr.id""";

    private final JdbcClient jdbc;

    public RepositorioProveedores(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<Proveedor> listar() {
        return jdbc.sql(SELECT + " GROUP BY pr.id ORDER BY pr.nombre").query(this::mapear).list();
    }

    public Proveedor buscar(long id) {
        return jdbc.sql(SELECT + " WHERE pr.id = ? GROUP BY pr.id").param(id).query(this::mapear).optional()
                .orElseThrow(() -> new ProveedorNoEncontradoException(id));
    }

    public Proveedor crear(DatosProveedor datos) {
        GeneratedKeyHolder clave = new GeneratedKeyHolder();
        try {
            jdbc.sql("INSERT INTO proveedor (nombre, activo) VALUES (?, ?)")
                    .params(datos.nombre().strip(), datos.activo())
                    .update(clave, "id");
        } catch (DuplicateKeyException e) {
            throw repetido(datos);
        }
        return buscar(clave.getKeyAs(Long.class));
    }

    public Proveedor editar(long id, DatosProveedor datos) {
        int filas;
        try {
            filas = jdbc.sql("UPDATE proveedor SET nombre = ?, activo = ? WHERE id = ?")
                    .params(datos.nombre().strip(), datos.activo(), id)
                    .update();
        } catch (DuplicateKeyException e) {
            throw repetido(datos);
        }
        if (filas == 0) {
            throw new ProveedorNoEncontradoException(id);
        }
        return buscar(id);
    }

    /** Solo se borra un proveedor sin perfiles; si tiene, se desactiva. */
    public void borrar(long id) {
        Proveedor p = buscar(id);
        if (p.perfiles() > 0) {
            throw new ProveedorEnUsoException(p.nombre() + " tiene " + p.perfiles()
                    + " perfil(es) de importación: desactivalo en lugar de borrarlo");
        }
        jdbc.sql("DELETE FROM proveedor WHERE id = ?").param(id).update();
    }

    private static ProveedorEnUsoException repetido(DatosProveedor datos) {
        return new ProveedorEnUsoException("Ya existe un proveedor llamado " + datos.nombre().strip());
    }

    private Proveedor mapear(ResultSet rs, int n) throws SQLException {
        return new Proveedor(rs.getLong("id"), rs.getString("nombre"), rs.getBoolean("activo"), rs.getInt("perfiles"));
    }
}
