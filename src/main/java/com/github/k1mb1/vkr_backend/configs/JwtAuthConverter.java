package com.github.k1mb1.vkr_backend.configs;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toUnmodifiableSet;

@Slf4j
@Component
public class JwtAuthConverter
    implements Converter<Jwt, AbstractAuthenticationToken> {

    final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(
        @NonNull
        final Jwt jwt
    ) {
        val authorities = Stream.concat(
            jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
            extractResourceRoles(jwt).stream()
        ).collect(toUnmodifiableSet());

        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }

    /**
     * Грубые (глобальные) роли — единственное, что мы берём из identity-провайдера.
     * Тонкие права (какой преподаватель к какому предмету/группам допущен) живут в БД
     * приложения и резолвятся отдельно по {@code sub}. Из Keycloak realm-роль
     * {@code admin} превращается в authority {@code ROLE_ADMIN} (для {@code hasRole('ADMIN')}).
     */
    @SuppressWarnings("unchecked")
    private Collection<? extends GrantedAuthority> extractResourceRoles(
        @NonNull
        final Jwt jwt
    ) {
        val realmAccess = jwt.getClaim("realm_access");
        if (!(realmAccess instanceof Map<?, ?> claims)) {
            return List.of();
        }
        val roles = claims.get("roles");
        if (!(roles instanceof Collection<?> roleList)) {
            return List.of();
        }
        return roleList.stream()
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
            .collect(toUnmodifiableSet());
    }
}
