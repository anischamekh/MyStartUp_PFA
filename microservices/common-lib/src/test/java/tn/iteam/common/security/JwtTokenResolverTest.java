package tn.iteam.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class JwtTokenResolverTest {

    @Test
    void resolveAccessToken_prefersBearerHeader() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer header-token");
        req.setCookies(new Cookie(JwtTokenResolver.ACCESS_TOKEN_COOKIE, "cookie-token"));

        assertEquals("header-token", JwtTokenResolver.resolveAccessToken(req));
    }

    @Test
    void resolveAccessToken_readsCookieWhenNoHeader() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new Cookie(JwtTokenResolver.ACCESS_TOKEN_COOKIE, "cookie-token"));

        assertEquals("cookie-token", JwtTokenResolver.resolveAccessToken(req));
    }

    @Test
    void resolveAccessToken_returnsNullWhenMissing() {
        assertNull(JwtTokenResolver.resolveAccessToken(new MockHttpServletRequest()));
    }
}
