package com.opao.pp_api.features.auth.dto.request;

public record LoginRequest(
    String username, 
    String password
) {}
