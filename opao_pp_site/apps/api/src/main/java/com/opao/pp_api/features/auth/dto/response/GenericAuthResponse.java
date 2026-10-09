package com.opao.pp_api.features.auth.dto.response;

import java.util.Map;

public record GenericAuthResponse(
    String status, 
    String message, 
    Map<String, Object> data
) {
    public static GenericAuthResponse success(String message, Map<String, Object> data) {
        return new GenericAuthResponse("SUCCESS", message, data);
    }

    public static GenericAuthResponse failed(String message) {
        return new GenericAuthResponse("FAILED", message, null);
    }
}
