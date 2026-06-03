package tn.iteam.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.iteam.backend.dto.LoginResponse;
import tn.iteam.backend.dto.SessionResponse;
import tn.iteam.backend.service.AuthService;
import tn.iteam.common.openapi.OpenApiExamples;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login and token refresh")
public class AuthController {

    private final AuthService authService;
    private final boolean secureCookie;

    public AuthController(AuthService authService, @Value("${app.cookie.secure:false}") boolean secureCookie) {
        this.authService = authService;
        this.secureCookie = secureCookie;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Sets HttpOnly JWT cookies; returns user profile only")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authenticated",
                    content = @Content(schema = @Schema(implementation = SessionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<SessionResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(value = OpenApiExamples.LOGIN_REQUEST)))
            @RequestBody Map<String, String> body,
            HttpServletResponse response) {
        String username = body.get("username");
        String password = body.get("password");
        if (username != null) {
            username = username.trim();
        }
        LoginResponse login = authService.login(username, password);
        attachCookies(response, login);
        return ResponseEntity.ok(toSession(login));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Rotates refresh token via HttpOnly cookie")
    @ApiResponse(responseCode = "200", description = "New token pair issued")
    public ResponseEntity<SessionResponse> refresh(
            @RequestBody(required = false) Map<String, String> body,
            @CookieValue(name = "refresh_token", required = false) String refreshCookie,
            HttpServletResponse response
    ) {
        String refreshToken = refreshCookie;
        if ((refreshToken == null || refreshToken.isBlank()) && body != null) {
            refreshToken = body.get("refreshToken");
        }
        LoginResponse login = authService.refresh(refreshToken);
        attachCookies(response, login);
        return ResponseEntity.ok(toSession(login));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Clears HttpOnly auth cookies")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        clearCookie(response, "access_token");
        clearCookie(response, "refresh_token");
        return ResponseEntity.noContent().build();
    }

    private SessionResponse toSession(LoginResponse login) {
        return new SessionResponse(
                login.userId(),
                login.username(),
                login.fullName(),
                login.role(),
                login.token()
        );
    }

    private void attachCookies(HttpServletResponse response, LoginResponse login) {
        response.addHeader("Set-Cookie", buildCookie("access_token", login.token(), 86400).toString());
        response.addHeader("Set-Cookie", buildCookie("refresh_token", login.refreshToken(), 604800).toString());
    }

    private void clearCookie(HttpServletResponse response, String name) {
        response.addHeader("Set-Cookie", buildCookie(name, "", 0).toString());
    }

    private ResponseCookie buildCookie(String name, String value, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .sameSite("Lax")
                .maxAge(maxAgeSeconds)
                .build();
    }
}
