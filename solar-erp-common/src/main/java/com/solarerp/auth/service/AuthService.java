package com.solarerp.auth.service;

import com.solarerp.auth.dto.LoginRequest;
import com.solarerp.auth.dto.LoginResponse;

public interface AuthService {

    default LoginResponse login(LoginRequest request) {
        return login(request, "unknown");
    }

    LoginResponse login(LoginRequest request, String clientIp);
}
