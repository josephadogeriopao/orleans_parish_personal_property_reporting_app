package com.opao.pp_api.features.auth.dto.request;

import com.opao.pp_api.common.validation.ValidUsername;

import io.swagger.v3.oas.annotations.media.Schema;

import com.opao.pp_api.common.validation.ValidPassword;
import com.opao.pp_api.common.validation.ValidFullName;
import com.opao.pp_api.common.validation.ValidPhoneNumber;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.opao.pp_api.common.validation.ValidEmail;

public record RegisterRequest(
    @ValidUsername    
    @Schema(example = "johndoe", description = "The unique username for the new account")
    @JsonProperty("username")
    String username, 
    @ValidPassword
    @JsonProperty("password")
    @Schema(example = "P@ssw0rd!", description = "The password for the new account")
    String password, 
    @ValidFullName
    @JsonProperty("fullName")
    @Schema(example = "John Doe", description = "The full name of the account holder")
    String fullName, 
    @ValidPhoneNumber
    @JsonProperty("phoneNumber")
    @Schema(example = "+1234567890", description = "The phone number of the account holder")
    String phoneNumber, 
    @ValidEmail
    @JsonProperty("emailAddress")
    @Schema(example = "johndoe@example.com", description = "The email address of the account holder")
    String emailAddress
) {}
