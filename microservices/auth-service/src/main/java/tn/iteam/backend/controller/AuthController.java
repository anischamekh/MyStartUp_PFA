package tn.iteam.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import tn.iteam.common.openapi.OpenApiExamples;
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
import tn.iteam.backend.service.AuthService;

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
    @Operation(summary = "Authenticate user", description = "Returns access + refresh tokens and sets HttpOnly cookies")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authenticated",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class),
                            examples = @ExampleObject(value = OpenApiExamples.LOGIN_RESPONSE))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<LoginResponse> login(
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
        return ResponseEntity.ok(login);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Rotates refresh token; uses cookie or body")
    @ApiResponse(responseCode = "200", description = "New token pair issued")
    public ResponseEntity<LoginResponse> refresh(
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
        return ResponseEntity.ok(login);
    }

    private void attachCookies(HttpServletResponse response, LoginResponse login) {
        ResponseCookie accessCookie = ResponseCookie.from("access_token", login.token())
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .sameSite("Lax")
                .maxAge(86400)
                .build();
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", login.refreshToken())
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .sameSite("Lax")
                .maxAge(604800)
                .build();
        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());
    }
}
