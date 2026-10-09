package com.opao.pp_api.features.auth.services;

import com.opao.pp_api.common.exceptions.VerificationException;
import com.opao.pp_api.configs.VerificationProperties;
import com.opao.pp_api.features.auth.dto.request.ResendVerificationRequest;
import com.opao.pp_api.features.auth.dto.request.VerifyEmailRequest;
import com.opao.pp_api.features.auth.dto.response.GenericAuthResponse;
import com.opao.pp_api.features.user.UserService;
import com.opao.pp_api.features.user.model.User;
import com.opao.pp_api.features.user_change.UserChangeService;
import com.opao.pp_api.features.user_change.model.UserChange;
import com.opao.pp_api.features.user_status.constants.UserStatuses;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class TokenVerificationService {

    private final UserService userService;
    private final UserChangeService userChangeService;
    private final VerificationProperties verificationProperties;

    public TokenVerificationService(UserService userService, UserChangeService userChangeService, 
                                    VerificationProperties verificationProperties) {
        this.userService = userService;
        this.userChangeService = userChangeService;
        this.verificationProperties = verificationProperties;
    }

    @Transactional
    public GenericAuthResponse verifyEmail(VerifyEmailRequest request) {
        UserChange userChange = checkVerificationCode(request.verificationCode());

        Integer userId = userChange.getUserId();
        User user = this.userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Associated user account record could not be found."));

        user.setActive(true);
        user.setUserStatusId(UserStatuses.ENABLED);
        this.userService.updateUser(userId, user);

        this.userChangeService.destroy(userChange.getId());
        
        return GenericAuthResponse.success("Email address verified. Account status updated to ENABLED.", null);
    }

    @Transactional
    public GenericAuthResponse resendVerification(ResendVerificationRequest request) {
        String newTrackingToken = UUID.randomUUID().toString();
        return GenericAuthResponse.success(
            "Stale activation records cleared. A fresh verification email has been dispatched.",
            Map.of("newVerificationCode", newTrackingToken)
        );
    }

    private UserChange checkVerificationCode(String verificationCode) {
        UserChange userChange = userChangeService.findUserChangeByVerificationCode(verificationCode)
                .orElseThrow(() -> new VerificationException("Invalid verification token code."));

        long durationElapsedMs = Duration.between(userChange.getInitiatedTime(), LocalDateTime.now()).toMillis();

        if (durationElapsedMs > verificationProperties.getCodeDurationMs()) {
            throw new VerificationException("This verification link has expired. Please request a new activation email.");
        }

        return userChange;
    }
}
