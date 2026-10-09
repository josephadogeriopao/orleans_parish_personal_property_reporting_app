package com.opao.pp_api.features.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.opao.pp_api.common.validation.ValidEmail;
import com.opao.pp_api.common.validation.ValidPassword;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @ValidEmail 
    @JsonProperty("emailAddress")
    private String email; 

    @ValidPassword 
    @JsonProperty("password")
    private String password;
}
