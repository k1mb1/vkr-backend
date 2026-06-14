package com.github.k1mb1.vkr_backend.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class NativeImageHints implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Register UUID[] for reflection, needed by Hibernate/Spring Data
        hints.reflection().registerType(UUID[].class, MemberCategory.values());
        hints.reflection().registerType(UUID.class, MemberCategory.values());
    }
}
