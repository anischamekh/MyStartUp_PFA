package tn.iteam.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import tn.iteam.backend.exception.BusinessException;
import tn.iteam.backend.service.UserSnapshotService;
import tn.iteam.common.security.JwtUserPrincipal;

@ExtendWith(MockitoExtension.class)
class CurrentUserProviderTest {

    @Mock
    private UserSnapshotService userSnapshotService;

    @InjectMocks
    private CurrentUserProvider currentUserProvider;

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void requireCurrentUser_throwsWhenNotAuthenticated() {
        assertThrows(BusinessException.class, () -> currentUserProvider.requireCurrentUser());
    }

    @Test
    void requireCurrentUser_returnsPrincipalAndEnsuresSnapshot() {
        JwtUserPrincipal principal = new JwtUserPrincipal(5L, "user", "EMPLOYEE", "user");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, null));

        JwtUserPrincipal result = currentUserProvider.requireCurrentUser();

        assertEquals(5L, result.userId());
        verify(userSnapshotService).requireById(5L);
    }
}
