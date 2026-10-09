package com.opao.pp_api.features.auth.services;

import com.opao.pp_api.features.auth.dto.request.RegisterRequest;
import com.opao.pp_api.features.auth.dto.response.GenericAuthResponse;
import com.opao.pp_api.features.user.UserService;
import com.opao.pp_api.features.user.model.User;
import com.opao.pp_api.features.user_change.UserChangeService;
import com.opao.pp_api.features.user_change.model.UserChange;
import com.opao.pp_api.features.user_change_type.constants.UserChangeTypes;
import com.opao.pp_api.features.user_role.constants.UserRoles;
import com.opao.pp_api.features.user_status.constants.UserStatuses;
import com.opao.pp_api.services.ArgonService;
import com.opao.pp_api.services.EmailService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class UserRegistrationService {

    private final ArgonService argonService;
    private final UserService userService;
    private final UserChangeService userChangeService;
    private final EmailService emailService;

    public UserRegistrationService(ArgonService argonService, UserService userService, 
                                   UserChangeService userChangeService, EmailService emailService) {
        this.argonService = argonService;
        this.userService = userService;
        this.userChangeService = userChangeService;
        this.emailService = emailService;
    }

    @Transactional
    public GenericAuthResponse register(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.emailAddress());
        user.setActive(false);
        user.setPhoneNumber(request.phoneNumber());  
        user.setPassword(this.argonService.hashPassword(request.password()));
        user.setFullName(request.fullName());
        user.setUserStatusId(UserStatuses.DISABLED);
        user.setUserRoleId(UserRoles.TAX_PREPARER);

        User createdUser = this.userService.create(user);   
        
        UserChange userChange = new UserChange();
        userChange.setUserId(createdUser.getId());
        userChange.setUserChangeTypeId(UserChangeTypes.ACTIVATE.getId()); 
        String trackingToken = UUID.randomUUID().toString();
        userChange.setVerificationCode(trackingToken);
        userChange.setInitiatedTime(LocalDateTime.now()); 
        this.userChangeService.create(userChange);

        this.emailService.sendVerificationEmail(createdUser.getEmail(), createdUser.getFullName(), trackingToken);

        return GenericAuthResponse.success(
            "Account registered successfully. Verification email has been sent.",
            Map.of("username", request.username(), "verificationCode", trackingToken)
        );
    }
}
