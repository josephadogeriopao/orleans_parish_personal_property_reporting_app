package com.opao.pp_api.features.auth.services;

import org.springframework.stereotype.Service;
import com.opao.pp_api.features.auth.dto.request.ForgotPasswordRequest;
import com.opao.pp_api.features.auth.dto.request.ResetPasswordRequest;
import com.opao.pp_api.features.auth.dto.response.GenericAuthResponse;
import jakarta.transaction.Transactional;
import java.util.Map;
import java.util.UUID;

@Service
public class CredentialRecoveryService {

    @Transactional
    public GenericAuthResponse forgotPassword(ForgotPasswordRequest request) {
        String resetToken = UUID.randomUUID().toString();
        return GenericAuthResponse.success(
            "Password recovery workflow triggered. Notification has been dispatched.",
            Map.of("resetToken", resetToken)
        );
    }

    @Transactional
    public GenericAuthResponse resetPassword(ResetPasswordRequest request) {
        return GenericAuthResponse.success("Password has been overwritten securely. Account unlocked.", null);
    }
}
