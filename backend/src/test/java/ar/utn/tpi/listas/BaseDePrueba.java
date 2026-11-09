package ar.utn.tpi.listas;

import java.io.IOException;
import java.io.UncheckedIOException;

import javax.sql.DataSource;

import org.springframework.test.context.DynamicPropertyRegistry;

import ar.utn.tpi.listas.base.InicializadorBase;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

/**
 * Un PostgreSQL embebido por ejecución de las pruebas, con schema.sql y seed.sql ya aplicados.
 * Lo comparten las pruebas de Spring (con {@link #propiedades}) y las del motor (con {@link #dataSource}).
 */
public final class BaseDePrueba {

    private static EmbeddedPostgres pg;

    private BaseDePrueba() {
    }

    public static synchronized DataSource dataSource() {
        if (pg == null) {
            try {
                pg = EmbeddedPostgres.start();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            InicializadorBase.inicializar(pg.getPostgresDatabase());
        }
        return pg.getPostgresDatabase();
    }

    /** Para {@code @DynamicPropertySource}: apunta el DataSource de Spring a la base embebida. */
    public static void propiedades(DynamicPropertyRegistry registro) {
        dataSource();
        registro.add("spring.datasource.url", () -> pg.getJdbcUrl("postgres", "postgres"));
        registro.add("spring.datasource.username", () -> "postgres");
        registro.add("spring.datasource.password", () -> "postgres");
    }
}
