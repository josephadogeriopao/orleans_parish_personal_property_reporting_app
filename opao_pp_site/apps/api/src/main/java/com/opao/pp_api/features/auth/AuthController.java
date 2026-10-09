package com.opao.pp_api.features.auth;

import com.opao.pp_api.features.auth.dto.request.ForgotPasswordRequest;
import com.opao.pp_api.features.auth.dto.request.LoginRequest;
import com.opao.pp_api.features.auth.dto.request.RegisterRequest;
import com.opao.pp_api.features.auth.dto.request.ResendVerificationRequest;
import com.opao.pp_api.features.auth.dto.request.ResetPasswordRequest;
import com.opao.pp_api.features.auth.dto.request.VerifyEmailRequest;
import com.opao.pp_api.features.auth.dto.response.GenericAuthResponse;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<GenericAuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<GenericAuthResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        return ResponseEntity.ok(authService.verifyEmail(request));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<GenericAuthResponse> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        return ResponseEntity.ok(authService.resendVerification(request));
    }

    @PostMapping("/login")
    public ResponseEntity<GenericAuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<GenericAuthResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<GenericAuthResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }
}
