package com.solarerp.auth.service;

import com.solarerp.auth.dto.LoginRequest;
import com.solarerp.auth.dto.LoginResponse;
import com.solarerp.auth.entity.User;
import com.solarerp.auth.entity.UserRole;
import com.solarerp.auth.exception.InvalidCredentialsException;
import com.solarerp.auth.repository.UserRepository;
import com.solarerp.auth.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private LoginRequest validRequest;

    private final String RAW_PASSWORD = "admin123";
    private final String ENCODED_PASSWORD = "$2a$encoded";
    private final String JWT_TOKEN = "mock-jwt-token";

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("admin");
        user.setEmail("admin@solarerp.com");
        user.setPassword(ENCODED_PASSWORD);
        user.setRole(UserRole.ADMIN);
        user.setActive(true);

        validRequest = new LoginRequest("admin", RAW_PASSWORD);
    }

    @Nested
    @DisplayName("login()")
    class LoginTests {

        @Test
        @DisplayName("Returns token when valid username login")
        void login_validUsername_returnsToken() {
            when(userRepository.findByUsername("admin"))
                    .thenReturn(Optional.of(user));

            when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD))
                    .thenReturn(true);

            when(jwtService.generateToken(user))
                    .thenReturn(JWT_TOKEN);

            LoginResponse response = authService.login(validRequest);

            assertThat(response).isNotNull();
            assertThat(response.token()).isEqualTo(JWT_TOKEN);
            assertThat(response.username()).isEqualTo("admin");
            assertThat(response.role()).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("Returns token when valid email login")
        void login_validEmail_returnsToken() {
            LoginRequest emailRequest =
                    new LoginRequest("admin@solarerp.com", RAW_PASSWORD);

            when(userRepository.findByUsername("admin@solarerp.com"))
                    .thenReturn(Optional.empty());

            when(userRepository.findByEmail("admin@solarerp.com"))
                    .thenReturn(Optional.of(user));

            when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD))
                    .thenReturn(true);

            when(jwtService.generateToken(user))
                    .thenReturn(JWT_TOKEN);

            LoginResponse response = authService.login(emailRequest);

            assertThat(response.token()).isEqualTo(JWT_TOKEN);
        }

        @Test
        @DisplayName("Throws exception when user not found")
        void login_userNotFound_throwsException() {
            when(userRepository.findByUsername("admin"))
                    .thenReturn(Optional.empty());

            when(userRepository.findByEmail("admin"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(validRequest))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessageContaining("Invalid username or password");
        }

        @Test
        @DisplayName("Throws exception when password incorrect")
        void login_wrongPassword_throwsException() {
            when(userRepository.findByUsername("admin"))
                    .thenReturn(Optional.of(user));

            when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD))
                    .thenReturn(false);

            assertThatThrownBy(() -> authService.login(validRequest))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessageContaining("Invalid username or password");
        }

        @Test
        @DisplayName("Throws exception when user inactive")
        void login_inactiveUser_throwsException() {
            user.setActive(false);

            when(userRepository.findByUsername("admin"))
                    .thenReturn(Optional.of(user));

            assertThatThrownBy(() -> authService.login(validRequest))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessageContaining("Account is disabled");
        }

        @Test
        @DisplayName("Calls jwtService to generate token")
        void login_validCredentials_callsJwtService() {
            when(userRepository.findByUsername("admin"))
                    .thenReturn(Optional.of(user));

            when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD))
                    .thenReturn(true);

            when(jwtService.generateToken(user))
                    .thenReturn(JWT_TOKEN);

            authService.login(validRequest);

            verify(jwtService, times(1)).generateToken(user);
        }
    }
}