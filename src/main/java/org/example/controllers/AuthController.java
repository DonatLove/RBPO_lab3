package org.example.controllers;

import org.example.dto.AccountResponse;
import org.example.dto.RegistrationRequest;
import org.example.models.User;
import org.example.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken csrfToken) {
        return Map.of("token", csrfToken.getToken(), "headerName", csrfToken.getHeaderName());
    }

    @PostMapping("/register")
    public ResponseEntity<AccountResponse> register(@RequestBody RegistrationRequest request) {
        User user = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(user));
    }

    @GetMapping("/me")
    public AccountResponse me(@AuthenticationPrincipal UserDetails principal) {
        return toResponse(userService.findByUsername(principal.getUsername()));
    }

    private AccountResponse toResponse(User user) {
        return new AccountResponse(
                user.getId(), user.getUsername(), user.getFullName(), user.getAppRole(),
                user.isEnabled(), user.isLocked());
    }
}
