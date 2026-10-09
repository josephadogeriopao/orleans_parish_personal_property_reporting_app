package com.opao.pp_api.features.auth.dto.request;


public record ResendVerificationRequest(
    String expiredVerificationCode
) {}
