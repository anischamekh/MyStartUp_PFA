package tn.iteam.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.common.security.JwtUserPrincipal;

class CurrentUserProviderTest {

    private final CurrentUserProvider provider = new CurrentUserProvider();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void requireCurrentUser_throwsWhenMissing() {
        assertThrows(BusinessException.class, provider::requireCurrentUser);
    }

    @Test
    void requireCurrentUser_returnsPrincipal() {
        JwtUserPrincipal principal = new JwtUserPrincipal(9L, "u", "EMPLOYEE", "u");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, null));
        assertEquals(9L, provider.requireCurrentUser().userId());
    }
}
