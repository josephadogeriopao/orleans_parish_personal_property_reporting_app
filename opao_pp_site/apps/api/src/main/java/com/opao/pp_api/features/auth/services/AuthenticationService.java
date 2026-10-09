package com.opao.pp_api.features.auth.services;
import java.time.LocalDateTime;
import com.opao.pp_api.services.ArgonService;
import org.springframework.stereotype.Service;
import com.opao.pp_api.common.types.LoginResult;

import com.opao.pp_api.features.auth.dto.request.LoginRequest;
import com.opao.pp_api.features.auth.dto.response.GenericAuthResponse;
import com.opao.pp_api.features.user.UserService;
import com.opao.pp_api.features.user.model.User;

import java.util.Map;

@Service
public class AuthenticationService {

    private final ArgonService argonService;
    private final UserService userService;

    public AuthenticationService(UserService userService, ArgonService argonService) {
        this.userService = userService;
        this.argonService = argonService;
    }

    public GenericAuthResponse login(LoginRequest request) {
        //LoginResult result = LoginResult.FAILED;
        User user = this.userService.getUserByEmailAddress(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Associated user profile data context can not be linked."));
        if(argonService.verifyPassword(request.getPassword(), user.getPassword())) {
            user.setLastLoginTime(LocalDateTime.now());
            user.setFailedLogins(0);
            this.userService.updateUser(user.getId(), user);  
            //result = LoginResult.SUCCESS;
        } else {
            throw new IllegalArgumentException("Invalid credentials.");
        }
        


        return GenericAuthResponse.success(
            "Authentication successful.",
            Map.of(
                "accessToken", "mock-eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                "refreshToken", "mock-refresh-uuid-7483-9204..."
            )
        );
    }
}
