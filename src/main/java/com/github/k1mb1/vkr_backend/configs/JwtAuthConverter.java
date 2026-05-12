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

import java.util.Collection;
import java.util.Collections;
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
        val authorities = Stream.concat(jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
                                        extractResourceRoles(jwt).stream()
        ).collect(toUnmodifiableSet());

        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }

    private Collection<? extends GrantedAuthority> extractResourceRoles(
        @NonNull
        final Jwt jwt
    ) {
        return Collections.emptySet();
    }
}
