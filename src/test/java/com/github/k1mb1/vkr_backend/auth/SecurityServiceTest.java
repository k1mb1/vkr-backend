package com.github.k1mb1.vkr_backend.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

class SecurityServiceTest {

    final SecurityService service = new SecurityService();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateWithSubject(String subject, String... roles) {
        var jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(subject)
                .claim("sub", subject)
                .build();
        var authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(jwt);
        var authorities =
                List.of(roles).stream().map(SimpleGrantedAuthority::new).toList();
        org.mockito.Mockito.lenient().doReturn(authorities).when(authentication).getAuthorities();
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void currentSubjectIdEmptyWhenNotAuthenticated() {
        assertThat(service.currentSubjectId()).isEmpty();
    }

    @Test
    void currentSubjectIdReadsSubFromJwt() {
        var id = UUID.randomUUID();
        authenticateWithSubject(id.toString());

        assertThat(service.currentSubjectId()).contains(id);
    }

    @Test
    void currentSubjectIdEmptyWhenSubNotUuid() {
        authenticateWithSubject("not-a-uuid");

        assertThat(service.currentSubjectId()).isEmpty();
    }

    @Test
    void currentSubjectIdEmptyWhenPrincipalIsNotJwt() {
        var authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("anonymous");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThat(service.currentSubjectId()).isEmpty();
    }

    @Test
    void isSameUserTrueForMatchingSubject() {
        var id = UUID.randomUUID();
        authenticateWithSubject(id.toString());

        assertThat(service.isSameUser(id)).isTrue();
        assertThat(service.isSameUser(UUID.randomUUID())).isFalse();
    }

    @Test
    void isSameUserFalseForNullId() {
        authenticateWithSubject(UUID.randomUUID().toString());

        assertThat(service.isSameUser(null)).isFalse();
    }

    @Test
    void isAdminTrueOnlyWithAdminRole() {
        authenticateWithSubject(UUID.randomUUID().toString(), SecurityService.ROLE_ADMIN);
        assertThat(service.isAdmin()).isTrue();
    }

    @Test
    void isAdminFalseWithoutAdminRole() {
        authenticateWithSubject(UUID.randomUUID().toString(), "ROLE_TEACHER");
        assertThat(service.isAdmin()).isFalse();
    }

    @Test
    void isAdminFalseWhenNotAuthenticated() {
        assertThat(service.isAdmin()).isFalse();
    }
}
