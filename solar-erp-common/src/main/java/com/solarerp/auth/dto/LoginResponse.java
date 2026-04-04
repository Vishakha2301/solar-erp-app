package com.solarerp.auth.dto;

public record LoginResponse(
        String token,
        String username,
        String role
) {}
