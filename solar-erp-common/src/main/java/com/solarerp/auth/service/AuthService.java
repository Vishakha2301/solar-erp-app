package com.solarerp.auth.service;

import com.solarerp.auth.dto.LoginRequest;
import com.solarerp.auth.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}