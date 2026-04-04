package com.solarerp.auth.service;

import com.solarerp.auth.entity.User;

public interface JwtService {
    String generateToken(User user);
    String extractUsername(String token);
    boolean isTokenValid(String token);
}