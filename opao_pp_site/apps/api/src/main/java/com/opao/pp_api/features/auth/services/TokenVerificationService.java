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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class TokenVerificationService {

    private final UserService userService;
    private final UserChangeService userChangeService;
    private final VerificationProperties verificationProperties;

    // Injected email system values using standard Spring application properties
    @Value("${app.mail.send-confirmation:false}")
    private String sendConfirmationEmail;

    @Value("${app.mail.from-address:}")
    private String fromAddress;

    @Value("${app.mail.from-person:}")
    private String fromPerson;

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
        // Find the old token using your request wrapper data string 
        UserChange oldUserChange = userChangeService.findUserChangeByVerificationCode(request.expiredVerificationCode())
                .orElseThrow(() -> new VerificationException("No trace of the provided validation token was discovered."));

        // Fetch associated user object records safely
        Integer userId = oldUserChange.getUserId();
        User user = this.userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Associated user profile data context can not be linked."));

        // Populate and generate a fresh replacement payload tracking record
        String newTrackingToken = UUID.randomUUID().toString();
        UserChange newUserChange = new UserChange();
        newUserChange.setUserId(userId); // Matches Integer target standard architecture definitions
        newUserChange.setVerificationCode(newTrackingToken);
        newUserChange.setInitiatedTime(LocalDateTime.now()); // Swapped from java.util.Date to LocalDateTime
        newUserChange.setUserChangeTypeId(oldUserChange.getUserChangeTypeId());

        // Perform clean atomic persistent replacements
        this.userChangeService.create(newUserChange);
        this.userChangeService.destroy(oldUserChange.getId());

        // Standard Email dispatch flow checking against application context constraints 
        if ("true".equalsIgnoreCase(sendConfirmationEmail) && fromAddress != null && !fromAddress.isBlank()) {
            sendVerificationEmail(
                fromAddress, 
                fromPerson, 
                user.getEmail(),
                user.getFullName(), 
                newTrackingToken
            );
        }

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

    // Mock/Stash method placeholder block matching your code footprint signatures 
    private void sendVerificationEmail(String from, String person, String to, String name, String code) {
        // Provide standard email messaging engine infrastructure logic bindings here
    }
}
