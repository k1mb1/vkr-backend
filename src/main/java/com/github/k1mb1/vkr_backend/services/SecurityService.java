package com.github.k1mb1.vkr_backend.services;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("securityService")
public class SecurityService {
    public boolean isSameUser(Authentication authentication, UUID id) {
        var subject = authentication.getName();
        return subject != null && subject.equals(id.toString());
    }
}