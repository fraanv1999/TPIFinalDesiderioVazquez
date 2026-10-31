package ar.utn.tpi.listas.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ConfiguracionCors implements WebMvcConfigurer {

    @Value("${app.cors.origenes}")
    private String[] origenes;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOrigins(origenes).allowedMethods("GET", "POST");
    }
}
