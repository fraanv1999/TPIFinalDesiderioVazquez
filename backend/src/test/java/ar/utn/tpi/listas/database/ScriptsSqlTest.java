package ar.utn.tpi.listas.database;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.jupiter.api.Test;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

/** database/schema.sql y seed.sql corren sin errores en un PostgreSQL real (embebido). */
class ScriptsSqlTest {

    private static final Path DATABASE = Path.of("..", "database");

    @Test
    void schemaYSeedSeEjecutan() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.start();
             Connection con = pg.getPostgresDatabase().getConnection();
             Statement st = con.createStatement()) {
            st.execute(Files.readString(DATABASE.resolve("schema.sql")));
            st.execute(Files.readString(DATABASE.resolve("seed.sql")));

            assertEquals(5, contar(st, "SELECT count(*) FROM perfil_importacion"));
            assertEquals(20, contar(st, "SELECT count(*) FROM campo_destino"));
            // Ningún perfil usa campos de otro formato destino.
            assertEquals(0, contar(st, """
                    SELECT count(*) FROM mapeo_columna m
                    JOIN perfil_importacion p ON p.id = m.perfil_id
                    JOIN campo_destino c ON c.id = m.campo_destino_id
                    WHERE c.formato_destino_id <> p.formato_destino_id"""));
            assertEquals(0, contar(st, """
                    SELECT count(*) FROM regla_calculo r
                    JOIN perfil_importacion p ON p.id = r.perfil_id
                    JOIN campo_destino c ON c.id IN (r.campo_resultado_id, r.campo_base_id, r.campo_secundario_id)
                    WHERE c.formato_destino_id <> p.formato_destino_id"""));
        }
    }

    private static long contar(Statement st, String sql) throws Exception {
        try (ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getLong(1);
        }
    }
}
