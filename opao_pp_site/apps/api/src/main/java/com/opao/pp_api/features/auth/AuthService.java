package com.opao.pp_api.features.auth;

import com.opao.pp_api.features.auth.dto.request.*;
import com.opao.pp_api.features.auth.dto.response.GenericAuthResponse;
import com.opao.pp_api.features.auth.services.*;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationService authenticationService;
    private final CredentialRecoveryService credentialRecoveryService;
    private final TokenVerificationService tokenVerificationService;
    private final UserRegistrationService userRegistrationService;  

    public AuthService(AuthenticationService authenticationService,
                       CredentialRecoveryService credentialRecoveryService,
                       TokenVerificationService tokenVerificationService,
                       UserRegistrationService userRegistrationService) {
        this.authenticationService = authenticationService;
        this.credentialRecoveryService = credentialRecoveryService;
        this.tokenVerificationService = tokenVerificationService;
        this.userRegistrationService = userRegistrationService;                 
    }

    public GenericAuthResponse register(RegisterRequest request) {
        return this.userRegistrationService.register(request);
    }

    public GenericAuthResponse verifyEmail(VerifyEmailRequest request) {
        return this.tokenVerificationService.verifyEmail(request);
    }

    public GenericAuthResponse resendVerification(ResendVerificationRequest request) {
        return this.tokenVerificationService.resendVerification(request);
    }

    public GenericAuthResponse login(LoginRequest request) {
        return this.authenticationService.login(request);
    }

    public GenericAuthResponse forgotPassword(ForgotPasswordRequest request) {
        return this.credentialRecoveryService.forgotPassword(request);
    }

    public GenericAuthResponse resetPassword(ResetPasswordRequest request) {
        return this.credentialRecoveryService.resetPassword(request);
    }
}
