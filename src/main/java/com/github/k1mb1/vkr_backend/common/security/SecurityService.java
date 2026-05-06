package com.github.k1mb1.vkr_backend.common.security;

import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component("securityService")
public class SecurityService {

    public boolean isSameUser(UUID id) {
        if (id == null) {
            return false;
        }
        var authentication =
            SecurityContextHolder.getContext().getAuthentication();

        if (
            authentication == null ||
            !(authentication.getPrincipal() instanceof Jwt jwt)
        ) {
            return false;
        }
        return id.toString().equals(jwt.getSubject());
    }
}
