package com.opao.pp_api.features.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;

public record VerifyEmailRequest(
    @NotBlank(message = "Verification code cannot be blank")
    @JsonProperty("verificationCode")
    // @Pattern(regexp = "^[0-9]{6}$", message = "Verification code must be a 6-digit number")
    String verificationCode
) {}