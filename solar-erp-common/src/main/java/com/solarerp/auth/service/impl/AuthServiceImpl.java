package com.solarerp.auth.service.impl;

import com.solarerp.auth.dto.LoginRequest;
import com.solarerp.auth.dto.LoginResponse;
import com.solarerp.auth.entity.User;
import com.solarerp.auth.exception.InvalidCredentialsException;
import com.solarerp.auth.repository.UserRepository;
import com.solarerp.auth.service.AuthService;
import com.solarerp.auth.service.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.identifier())
                .or(() -> userRepository.findByEmail(request.identifier()))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!user.isActive()) {
            throw new InvalidCredentialsException("Account is disabled");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String token = jwtService.generateToken(user);

        return new LoginResponse(token, user.getUsername(), user.getRole().name());
    }
}
