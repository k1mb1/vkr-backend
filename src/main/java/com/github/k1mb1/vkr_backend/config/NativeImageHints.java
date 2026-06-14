package com.github.k1mb1.vkr_backend.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.util.UUID;

/**
 * Reflection hints required when running as a GraalVM native image.
 * Registered via {@code @ImportRuntimeHints} on the application class — a
 * {@code RuntimeHintsRegistrar} is only invoked during AOT processing when
 * imported that way (annotating it as a {@code @Component} has no effect).
 */
public class NativeImageHints implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Hibernate/Spring Data reflectively instantiates java.util.UUID[]
        hints.reflection().registerType(UUID.class, MemberCategory.values());
        hints.reflection().registerType(UUID[].class, MemberCategory.values());

        // Spring Security OAuth2 JWT decoding
        hints.reflection().registerType(JwtDecoder.class, MemberCategory.values());
        hints.reflection().registerType(NimbusJwtDecoder.class, MemberCategory.values());
    }
}
