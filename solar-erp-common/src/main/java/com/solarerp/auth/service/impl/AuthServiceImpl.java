package com.solarerp.auth.service.impl;

import com.solarerp.auth.dto.LoginRequest;
import com.solarerp.auth.dto.LoginResponse;
import com.solarerp.auth.entity.User;
import com.solarerp.auth.exception.InvalidCredentialsException;
import com.solarerp.auth.exception.TooManyRequestsException;
import com.solarerp.auth.repository.UserRepository;
import com.solarerp.auth.service.AuthService;
import com.solarerp.auth.service.JwtService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LogManager.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ConcurrentHashMap<String, Deque<Instant>> ipFailures = new ConcurrentHashMap<>();

    @Value("${app.security.login-abuse.rate-limit.window-minutes:1}")
    private int rateLimitWindowMinutes = 1;

    @Value("${app.security.login-abuse.rate-limit.max-attempts:20}")
    private int rateLimitMaxAttempts = 20;

    @Value("${app.security.login-abuse.lockout.max-failed-attempts:5}")
    private int lockoutMaxFailedAttempts = 5;

    @Value("${app.security.login-abuse.lockout.duration-minutes:15}")
    private int lockoutDurationMinutes = 15;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public LoginResponse login(LoginRequest request, String clientIp) {
        String sourceIp = normalizeIp(clientIp);
        if (isRateLimited(sourceIp)) {
            log.warn("Auth rate limited identifier={} ip={}", request.identifier(), sourceIp);
            throw new TooManyRequestsException("Too many login attempts. Please retry later.");
        }

        Optional<User> optionalUser = userRepository.findByUsername(request.identifier())
                .or(() -> userRepository.findByEmail(request.identifier()));

        if (optionalUser.isEmpty()) {
            recordFailure(sourceIp);
            log.warn("Auth failed identifier={} ip={} reason=user_not_found", request.identifier(), sourceIp);
            throw new InvalidCredentialsException("Invalid username or password");
        }

        User user = optionalUser.get();
        if (!user.isActive()) {
            log.warn("Auth failed userId={} ip={} reason=inactive", user.getId(), sourceIp);
            throw new InvalidCredentialsException("Account is disabled");
        }

        if (isAccountLocked(user)) {
            recordFailure(sourceIp);
            log.warn("Auth failed userId={} ip={} reason=account_locked lockedUntil={}",
                    user.getId(), sourceIp, user.getLockedUntil());
            throw new TooManyRequestsException("Account temporarily locked due to failed attempts");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            recordUserFailure(user);
            recordFailure(sourceIp);
            log.warn("Auth failed userId={} ip={} reason=bad_password failedAttempts={} lockedUntil={}",
                    user.getId(), sourceIp, user.getFailedLoginAttempts(), user.getLockedUntil());
            throw new InvalidCredentialsException("Invalid username or password");
        }

        resetFailures(user);

        String token = jwtService.generateToken(user);
        log.info("Auth success userId={} ip={}", user.getId(), sourceIp);
        return new LoginResponse(token, user.getUsername(), user.getRole().name());
    }

    private boolean isRateLimited(String ip) {
        Instant threshold = Instant.now().minus(rateLimitWindowMinutes, ChronoUnit.MINUTES);
        Deque<Instant> attempts = ipFailures.computeIfAbsent(ip, key -> new ConcurrentLinkedDeque<>());
        pruneOldAttempts(attempts, threshold);
        return attempts.size() >= rateLimitMaxAttempts;
    }

    private void recordFailure(String ip) {
        Deque<Instant> attempts = ipFailures.computeIfAbsent(ip, key -> new ConcurrentLinkedDeque<>());
        attempts.addLast(Instant.now());
    }

    private void pruneOldAttempts(Deque<Instant> attempts, Instant threshold) {
        while (!attempts.isEmpty() && attempts.peekFirst().isBefore(threshold)) {
            attempts.pollFirst();
        }
    }

    private boolean isAccountLocked(User user) {
        return user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now());
    }

    private void recordUserFailure(User user) {
        int nextAttempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(nextAttempts);

        if (nextAttempts >= lockoutMaxFailedAttempts) {
            user.setLockedUntil(Instant.now().plus(lockoutDurationMinutes, ChronoUnit.MINUTES));
            user.setFailedLoginAttempts(0);
        }

        userRepository.save(user);
    }

    private void resetFailures(User user) {
        if (user.getFailedLoginAttempts() != 0 || user.getLockedUntil() != null) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }
    }

    private String normalizeIp(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            return "unknown";
        }
        return clientIp.trim();
    }
}
