package com.opao.pp_api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;     
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.opao.pp_api.services.ArgonService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test/auth")
public class AuthTestController {

    private final ArgonService argonService;

    // Constructor injection automatically loads your custom ArgonService bean
    public AuthTestController(ArgonService argonService) {
        this.argonService = argonService;
    }

    /**
     * TEST ENDPOINT: GENERATE ARGON2 HASH
     * POST http://localhost:9000/api/test/auth/hash-password
     */
    @PostMapping("/hash-password")
    public ResponseEntity<Map<String, String>> testHashPassword(@RequestParam String password) {
        // 1. Generate the secure one-way hash string using configuration settings
        String secureHash = argonService.hashPassword(password);

        // 2. Perform a sanity check internal match verification pass
        boolean isMatchVerified = argonService.verifyPassword(password, secureHash);

        // 3. Construct a clear descriptive response payload map
        Map<String, String> response = new HashMap<>();
        response.put("input_password", password);
        response.put("generated_argon2_hash", secureHash);
        response.put("internal_verification_status", isMatchVerified ? "SUCCESS" : "FAILED");

        return ResponseEntity.ok(response);
    }
}
