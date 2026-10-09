package com.opao.pp_api.features.auth;

import com.opao.pp_api.features.auth.dto.request.ForgotPasswordRequest;
import com.opao.pp_api.features.auth.dto.request.LoginRequest;
import com.opao.pp_api.features.auth.dto.request.RegisterRequest;
import com.opao.pp_api.features.auth.dto.request.ResendVerificationRequest;
import com.opao.pp_api.features.auth.dto.request.ResetPasswordRequest;
import com.opao.pp_api.features.auth.dto.request.VerifyEmailRequest;
import com.opao.pp_api.features.auth.dto.response.GenericAuthResponse;
import com.opao.pp_api.features.user.UserService;
import com.opao.pp_api.features.user_change.model.UserChange;
import com.opao.pp_api.features.user_change_type.constants.UserChangeTypes;
import com.opao.pp_api.features.user.model.User;
import com.opao.pp_api.features.user_change.UserChangeService;
import com.opao.pp_api.features.user_role.constants.UserRoles;
import com.opao.pp_api.features.user_status.constants.UserStatuses;
import com.opao.pp_api.services.ArgonService;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final ArgonService argonService;
    private final UserService userService;
    private final UserChangeService userChangeService;

    public AuthService( ArgonService argonService, UserService userService, UserChangeService userChangeService) {

        this.argonService = argonService;
        this.userService = userService; 
        this.userChangeService = userChangeService;
    }

    @Transactional 
    public GenericAuthResponse register(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.emailAddress());
        user.setActive(false);
        user.setPhoneNumber(request.phoneNumber());  
        String rawPassword = request.password();
        String hashedPassword = this.argonService.hashPassword(rawPassword);
        user.setPassword(hashedPassword);
        user.setFullName(request.fullName());
        user.setUserStatusId(UserStatuses.DISABLED);
        user.setUserRoleId(UserRoles.TAX_PREPARER);


            User createdUser = this.userService.create(user);   
            UserChange userChange = new UserChange();
            userChange.setUserId(createdUser.getId());
            userChange.setUserChangeTypeId(UserChangeTypes.ACTIVATE.getId()); 
            String trackingToken = UUID.randomUUID().toString();
            userChange.setVerificationCode(trackingToken);
            this.userChangeService.create(userChange);

        return GenericAuthResponse.success(
            "Account registered successfully. Verification token generated.",
            Map.of("username", request.username(), "verificationCode", trackingToken)
        );

    }

    // Handles logic for: verifyEmailAddress(...)
    public GenericAuthResponse verifyEmail(VerifyEmailRequest request) {
        // TODO: Validate token, check password via Argon2 matches, set status to ENABLED
        return GenericAuthResponse.success("Email address verified. Account status updated to ENABLED.", null);
    }

    // Handles logic for: resendVerificationEmail(...)
    public GenericAuthResponse resendVerification(ResendVerificationRequest request) {
        // TODO: Clear old tokens, persist new token, trigger outbound mail service
        String newTrackingToken = UUID.randomUUID().toString();
        
        return GenericAuthResponse.success(
            "Stale activation records cleared. A fresh verification email has been dispatched.",
            Map.of("newVerificationCode", newTrackingToken)
        );
    }

    // Handles logic for: login(...)
    public GenericAuthResponse login(LoginRequest request) {
        // TODO: Authenticate via AuthenticationManager, verify status rules, increment failure counters if bad
        // For Next.js presentation, mock the dual-token footprint:
        return GenericAuthResponse.success(
            "Authentication successful.",
            Map.of(
                "accessToken", "mock-eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                "refreshToken", "mock-refresh-uuid-7483-9204..."
            )
        );
    }

    // Handles logic for: submitResetPasswordRequest(...)
    public GenericAuthResponse forgotPassword(ForgotPasswordRequest request) {
        // TODO: Check email exists, store a verification code, fire reset notification instructions
        String resetToken = UUID.randomUUID().toString();
        
        return GenericAuthResponse.success(
            "Password recovery workflow triggered. Notification has been dispatched.",
            Map.of("resetToken", resetToken)
        );
    }

    // Handles logic for: resetPassword(...)
    public GenericAuthResponse resetPassword(ResetPasswordRequest request) {
        // TODO: Validate reset token, use Argon2 to hash new password, overwrite entity, set failedLogins to 0
        return GenericAuthResponse.success("Password has been overwritten securely. Account unlocked.", null);
    }
}
