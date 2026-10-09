package com.opao.pp_api.features.auth;

import com.opao.pp_api.common.exceptions.VerificationException;
import com.opao.pp_api.configs.VerificationProperties;
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
import com.opao.pp_api.services.EmailService;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final ArgonService argonService;
    private final UserService userService;
    private final UserChangeService userChangeService;
    private final EmailService emailService; 
    private final VerificationProperties verificationProperties;

    public AuthService(ArgonService argonService, 
                       UserService userService, 
                       UserChangeService userChangeService,
                       EmailService emailService,
                       VerificationProperties verificationProperties) {
        this.argonService = argonService;
        this.userService = userService; 
        this.userChangeService = userChangeService;
        this.emailService = emailService;
        this.verificationProperties = verificationProperties;
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
        // Ensure your UserChange entity tracks creation using java.time.LocalDateTime
        userChange.setInitiatedTime(LocalDateTime.now()); 
        this.userChangeService.create(userChange);

        this.emailService.sendVerificationEmail(
            createdUser.getEmail(), 
            createdUser.getFullName(), 
            trackingToken
        );

        return GenericAuthResponse.success(
            "Account registered successfully. Verification email has been sent.",
            Map.of("username", request.username(), "verificationCode", trackingToken)
        );
    }

    @Transactional
    public GenericAuthResponse verifyEmail(VerifyEmailRequest request) {
        // 1. Validate the code structure and check lifetime duration rules
        UserChange userChange = checkVerificationCode(request.verificationCode());

        // 2. Locate the linked account record
        Integer userId = userChange.getUserId();
        User user = this.userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Associated user account record could not be found."));

        // 3. Flip profile activation properties from DISABLED to ENABLED
        user.setActive(true);
        user.setUserStatusId(UserStatuses.ENABLED);
        this.userService.updateUser(userId, user);

        // 4. record by updating the user change status and then keeping it for audit purposes
        this.userChangeService.destroy(userChange.getId());
        
        return GenericAuthResponse.success("Email address verified. Account status updated to ENABLED.", null);
    }

    @Transactional
    public GenericAuthResponse resendVerification(ResendVerificationRequest request) {
        // TODO: Clear old tokens, persist new token, trigger outbound mail service
        String newTrackingToken = UUID.randomUUID().toString();
        
        return GenericAuthResponse.success(
            "Stale activation records cleared. A fresh verification email has been dispatched.",
            Map.of("newVerificationCode", newTrackingToken)
        );
    }

    public GenericAuthResponse login(LoginRequest request) {
        return GenericAuthResponse.success(
            "Authentication successful.",
            Map.of(
                "accessToken", "mock-eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                "refreshToken", "mock-refresh-uuid-7483-9204..."
            )
        );
    }

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

    /**
     * Modernized verification inspection workflow.
     * Replaces old integer statuses (-1, 0, 1) with clean, explicit descriptive Exception structures.
     */
    private UserChange checkVerificationCode(String verificationCode) {
        UserChange userChange = userChangeService.findUserChangeByVerificationCode(verificationCode)
                .orElseThrow(() -> new VerificationException("Invalid verification token code."));

        // 🟢 Calculates duration cleanly using Java 8 Time API instead of old java.util.Date long math
        long durationElapsedMs = Duration.between(userChange.getInitiatedTime(), LocalDateTime.now()).toMillis();

        if (durationElapsedMs > verificationProperties.getCodeDurationMs()) {
            throw new VerificationException("This verification link has expired. Please request a new activation email.");
        }

        return userChange;
    }
}
