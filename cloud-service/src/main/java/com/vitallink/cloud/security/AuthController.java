package com.vitallink.cloud.security;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller pentru autentificare JWT.
 *
 * Utilizatori demo pentru prezentarea la comisie:
 * - dispatcher / dispatch123  → rol DISPATCHER
 * - admin      / admin123     → rol ADMIN
 *
 * În producție aceștia ar fi stocați în baza de date
 * cu parole hash-uite BCrypt.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final JwtUtils jwtUtils;

    public AuthController(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    /**
     * POST /api/auth/login
     * Body: { "username": "dispatcher", "password": "dispatch123" }
     * Response: { "token": "eyJ..." }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        // Validare credențiale demo
        if (isValidUser(username, password)) {
            String role = username.equals("admin") ? "ADMIN" : "DISPATCHER";
            String token = jwtUtils.generateToken(username, role);

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "username", username,
                    "role", role,
                    "message", "Autentificare reușită"
            ));
        }

        return ResponseEntity.status(401).body(Map.of(
                "error", "Credențiale invalide"
        ));
    }

    /**
     * GET /api/auth/validate
     * Verifică dacă token-ul curent este valid.
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validate(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtils.validateToken(token)) {
                String username = jwtUtils.getUsernameFromToken(token);
                return ResponseEntity.ok(Map.of(
                        "valid", true,
                        "username", username
                ));
            }
        }
        return ResponseEntity.status(401).body(Map.of("valid", false));
    }

    private boolean isValidUser(String username, String password) {
        if (username == null || password == null) return false;
        return (username.equals("dispatcher") && password.equals("dispatch123"))
                || (username.equals("admin") && password.equals("admin123"));
    }
}