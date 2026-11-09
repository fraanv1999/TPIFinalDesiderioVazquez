package ar.utn.tpi.listas.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/** El frontend corre en otro origen (Vite en desarrollo, static site en Render). Lo aplica Spring Security. */
@Configuration
public class ConfiguracionCors {

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.origenes}") List<String> origenes) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(origenes);
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cors.setExposedHeaders(List.of("Content-Disposition"));
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/api/**", cors);
        return fuente;
    }
}
