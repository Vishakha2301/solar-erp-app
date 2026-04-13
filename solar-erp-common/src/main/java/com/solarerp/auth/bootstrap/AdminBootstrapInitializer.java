package com.solarerp.auth.bootstrap;

import com.solarerp.auth.entity.User;
import com.solarerp.auth.entity.UserRole;
import com.solarerp.auth.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminBootstrapInitializer implements ApplicationRunner {

    private static final Logger log = LogManager.getLogger(AdminBootstrapInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.bootstrap-admin.enabled:false}")
    private boolean enabled;

    @Value("${app.security.bootstrap-admin.username:}")
    private String username;

    @Value("${app.security.bootstrap-admin.email:}")
    private String email;

    @Value("${app.security.bootstrap-admin.password:}")
    private String password;

    @Value("${app.security.bootstrap-admin.role:ADMIN}")
    private String role;

    public AdminBootstrapInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }

        if (userRepository.count() > 0) {
            log.info("Skipping bootstrap admin creation because users already exist");
            return;
        }

        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            throw new IllegalStateException("Bootstrap admin is enabled but username/email/password is missing");
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setEmail(email.trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(parseRole(role));
        user.setActive(true);

        userRepository.save(user);
        log.warn("Bootstrap admin user created for initial setup. Disable app.security.bootstrap-admin.enabled after first login.");
    }

    private UserRole parseRole(String roleValue) {
        try {
            return UserRole.valueOf(roleValue.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Invalid bootstrap admin role: " + roleValue, ex);
        }
    }
}
