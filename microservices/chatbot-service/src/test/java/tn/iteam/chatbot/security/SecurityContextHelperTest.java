package tn.iteam.chatbot.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class SecurityContextHelperTest {

    private final SecurityContextHelper helper = new SecurityContextHelper();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void currentRole_returnsNullWhenUnauthenticated() {
        assertNull(helper.currentRole());
        assertNull(helper.currentUsername());
    }

    @Test
    void currentRole_returnsFirstAuthority() {
        var auth = new UsernamePasswordAuthenticationToken(
                "user-1",
                null,
                List.of(new SimpleGrantedAuthority("MANAGER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertEquals("MANAGER", helper.currentRole());
        assertEquals("user-1", helper.currentUsername());
    }
}
