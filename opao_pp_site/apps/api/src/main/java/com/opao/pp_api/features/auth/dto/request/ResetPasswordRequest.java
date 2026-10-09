package com.opao.pp_api.features.auth.dto.request;

public record ResetPasswordRequest(
    String verificationCode, 
    String newPassword
) {}
