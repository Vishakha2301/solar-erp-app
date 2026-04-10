package com.solarerp.auth.service;

import com.solarerp.auth.entity.User;
import com.solarerp.auth.entity.UserRole;
import com.solarerp.auth.service.impl.JwtServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtServiceImpl Tests")
class JwtServiceImplTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private JwtDecoder jwtDecoder;

    @InjectMocks
    private JwtServiceImpl jwtService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("admin");
        user.setEmail("admin@solarerp.com");
        user.setRole(UserRole.ADMIN);
        user.setActive(true);
    }

    private Jwt buildJwt(String username, Instant expiry) {
        return new Jwt(
                "mock-token",
                Instant.now(),
                expiry,
                Map.of("alg", "HS256"),
                Map.of(
                        "username", username,
                        "sub", user.getId().toString()
                )
        );
    }

    @Nested
    @DisplayName("generateToken()")
    class GenerateTokenTests {

        @Test
        @DisplayName("Generates non-null token")
        void generateToken_validUser_returnsToken() {
            Jwt jwt = buildJwt("admin", Instant.now().plusSeconds(3600));

            when(jwtEncoder.encode(any()))
                    .thenReturn(jwt);

            String token = jwtService.generateToken(user);

            assertThat(token).isEqualTo("mock-token");
        }
    }

    @Nested
    @DisplayName("extractUsername()")
    class ExtractUsernameTests {

        @Test
        @DisplayName("Extracts username from token")
        void extractUsername_validToken_returnsUsername() {
            Jwt jwt = buildJwt("admin", Instant.now().plusSeconds(3600));

            when(jwtDecoder.decode("token"))
                    .thenReturn(jwt);

            String username = jwtService.extractUsername("token");

            assertThat(username).isEqualTo("admin");
        }
    }

    @Nested
    @DisplayName("isTokenValid()")
    class IsTokenValidTests {

        @Test
        @DisplayName("Returns true for valid token")
        void isTokenValid_validToken_returnsTrue() {
            Jwt jwt = buildJwt("admin", Instant.now().plusSeconds(3600));

            when(jwtDecoder.decode("token"))
                    .thenReturn(jwt);

            assertThat(jwtService.isTokenValid("token")).isTrue();
        }

        @Test
        @DisplayName("Returns false for expired token")
        void isTokenValid_expiredToken_returnsFalse() {
            Jwt jwt = buildJwt("admin", Instant.now().minusSeconds(10));

            when(jwtDecoder.decode("token"))
                    .thenReturn(jwt);

            assertThat(jwtService.isTokenValid("token")).isFalse();
        }

        @Test
        @DisplayName("Returns false when decoding fails")
        void isTokenValid_invalidToken_returnsFalse() {
            when(jwtDecoder.decode("bad-token"))
                    .thenThrow(new JwtException("Invalid"));

            assertThat(jwtService.isTokenValid("bad-token")).isFalse();
        }
    }
}