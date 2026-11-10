package ar.utn.tpi.listas.base;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

/**
 * Si la base está vacía, ejecuta database/schema.sql y seed.sql (se copian al jar en {@code db/}).
 * Si ya tiene las tablas, no toca nada: los datos se conservan entre reinicios y despliegues.
 */
@Component
public class InicializadorBase implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(InicializadorBase.class);

    private final DataSource dataSource;
    private final boolean habilitado;

    public InicializadorBase(DataSource dataSource, @Value("${app.base.inicializar:true}") boolean habilitado) {
        this.dataSource = dataSource;
        this.habilitado = habilitado;
    }

    @Override
    public void afterPropertiesSet() {
        if (habilitado) {
            inicializar(dataSource);
        }
    }

    /** @return true si creó el esquema, false si ya existía. */
    public static boolean inicializar(DataSource dataSource) {
        Boolean existe = JdbcClient.create(dataSource)
                .sql("SELECT to_regclass('public.perfil_importacion') IS NOT NULL")
                .query(Boolean.class)
                .single();
        if (existe) {
            return false;
        }
        log.info("Base vacía: se ejecutan schema.sql y seed.sql");
        ResourceDatabasePopulator scripts = new ResourceDatabasePopulator(
                new ClassPathResource("db/schema.sql"), new ClassPathResource("db/seed.sql"));
        // Los .sql están en UTF-8; sin esto, en Windows se leen como Cp1252 y los acentos se rompen.
        scripts.setSqlScriptEncoding("UTF-8");
        scripts.execute(dataSource);
        return true;
    }
}
