package com.opao.pp_api.features.auth.services;

import org.springframework.stereotype.Service;

import com.opao.pp_api.features.auth.dto.request.LoginRequest;
import com.opao.pp_api.features.auth.dto.response.GenericAuthResponse;
import java.util.Map;

@Service
public class AuthenticationService {

    public GenericAuthResponse login(LoginRequest request) {
        return GenericAuthResponse.success(
            "Authentication successful.",
            Map.of(
                "accessToken", "mock-eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                "refreshToken", "mock-refresh-uuid-7483-9204..."
            )
        );
    }
}
