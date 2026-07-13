package com.github.k1mb1.vkr_backend.auth.config;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import com.github.k1mb1.vkr_backend.auth.JwtAuthConverter;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.SupplierJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    static final String[] PUBLIC_ENDPOINTS = {
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/v3/api-docs",
        // Публичная страница check-in по QR: студент не аутентифицирован.
        // Доступ открыт, но защищён кодом аудитории и поиском вместо полного ростера.
        "/api/check-in-sessions/public/**",
    };

    /** Preflight-кеш CORS (Access-Control-Max-Age), 1 час. */
    private static final long CORS_MAX_AGE_SECONDS = 3600L;

    private final JwtAuthConverter jwtAuthConverter;

    private final String[] allowedOrigins;

    private final String issuerUri;

    public SecurityConfig(
            JwtAuthConverter jwtAuthConverter,
            @Value("${app.cors.allowed-origins}") String[] allowedOrigins,
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}") String issuerUri) {
        this.jwtAuthConverter = jwtAuthConverter;
        this.allowedOrigins = allowedOrigins;
        this.issuerUri = issuerUri;
    }

    /**
     * Explicit decoder so the bean is present unconditionally — Spring Boot's
     * auto-configured one is created only when {@code issuer-uri} is set at AOT
     * build time, which isn't the case for the native image (the value arrives
     * from the environment at runtime). {@link SupplierJwtDecoder} defers the
     * OIDC discovery network call to the first token decode, so the context
     * starts even when the issuer isn't yet reachable.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return new SupplierJwtDecoder(
                () -> NimbusJwtDecoder.withIssuerLocation(issuerUri).build());
    }

    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        return http.cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        .requestMatchers(PUBLIC_ENDPOINTS)
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(STATELESS))
                .oauth2ResourceServer(oauth2ResourceServer ->
                        oauth2ResourceServer.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)))
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(allowedOrigins));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Все заголовки включая Authorization
        config.setAllowedHeaders(List.of("*"));

        // Разрешить отправку cookies / Authorization header
        config.setAllowCredentials(true);

        // Кешировать preflight на 1 час
        config.setMaxAge(CORS_MAX_AGE_SECONDS);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
