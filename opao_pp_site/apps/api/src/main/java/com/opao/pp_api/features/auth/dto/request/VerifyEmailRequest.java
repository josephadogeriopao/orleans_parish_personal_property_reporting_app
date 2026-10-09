package com.opao.pp_api.features.auth.dto.request;

public record VerifyEmailRequest(
    String verificationCode, 
    String password
) {}