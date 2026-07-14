package com.github.k1mb1.vkr_backend.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.k1mb1.vkr_backend.auth.JwtAuthConverter;
import com.github.k1mb1.vkr_backend.auth.config.SecurityConfig;
import com.github.k1mb1.vkr_backend.common.web.GlobalExceptionHandler;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * Base for {@code @WebMvcTest} slices: wires the real security filter chain
 * ({@link SecurityConfig}) and the error contract ({@link GlobalExceptionHandler})
 * so the tests exercise what a request actually hits — authentication, bean
 * validation and the {@code ErrorDto} mapping — with the service layer mocked.
 *
 * <p>Method security ({@code @PreAuthorize}) lives on the services and is verified
 * by {@code AuthorizationServiceTest}; here the services are mocks, so these slices
 * assert the web contract only (status, validation, error shape, auth entry point).
 */
@Import({SecurityConfig.class, JwtAuthConverter.class, GlobalExceptionHandler.class})
@TestPropertySource(
        properties = {
            "app.cors.allowed-origins=http://localhost",
            "spring.security.oauth2.resourceserver.jwt.issuer-uri="
        })
abstract class AbstractControllerTest {

    @Autowired
    protected MockMvc mvc;

    /** For serialising request bodies only — the slice's own converters handle responses. */
    protected static final ObjectMapper json = new ObjectMapper();

    /** An authenticated non-admin teacher (JWT with {@code sub}); no admin role. */
    protected static RequestPostProcessor teacher() {
        return jwt().jwt(builder -> builder.subject(UUID.randomUUID().toString()));
    }

    /** An authenticated admin (realm role {@code admin} → {@code ROLE_ADMIN}). */
    protected static RequestPostProcessor admin() {
        return jwt().jwt(builder -> builder.subject(UUID.randomUUID().toString())
                        .claim("realm_access", Map.of("roles", List.of("admin"))))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
}
