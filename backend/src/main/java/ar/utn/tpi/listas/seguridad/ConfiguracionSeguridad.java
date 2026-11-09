package ar.utn.tpi.listas.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import tools.jackson.databind.json.JsonMapper;

/**
 * HTTP Basic sin sesión: el frontend manda las credenciales en cada pedido.
 * Cualquier usuario activo opera las importaciones; solo un ADMIN da de alta, edita o borra proveedores.
 * Un 401 no lleva {@code WWW-Authenticate}, así el navegador no muestra su propio cuadro de login.
 */
@Configuration
public class ConfiguracionSeguridad {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Bean
    SecurityFilterChain seguridad(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/salud").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/**").authenticated()
                        .requestMatchers("/api/proveedores/**").hasRole("ADMIN")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().denyAll())
                .httpBasic(basic -> basic.authenticationEntryPoint(noAutenticado()))
                .exceptionHandling(e -> e.authenticationEntryPoint(noAutenticado()).accessDeniedHandler(sinPermiso()))
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static AuthenticationEntryPoint noAutenticado() {
        return (pedido, respuesta, e) -> escribir(respuesta, HttpStatus.UNAUTHORIZED, "Email o contraseña incorrectos");
    }

    private static AccessDeniedHandler sinPermiso() {
        return (pedido, respuesta, e) -> escribir(respuesta, HttpStatus.FORBIDDEN, "Tu usuario no tiene permiso para esta acción");
    }

    private static void escribir(jakarta.servlet.http.HttpServletResponse respuesta, HttpStatus estado, String detalle)
            throws java.io.IOException {
        respuesta.setStatus(estado.value());
        respuesta.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        respuesta.setCharacterEncoding("UTF-8");
        respuesta.getWriter().write(JSON.writeValueAsString(ProblemDetail.forStatusAndDetail(estado, detalle)));
    }
}
