package com.solarerp.auth.service;

import com.solarerp.auth.entity.User;
import com.solarerp.auth.entity.UserRole;
import com.solarerp.auth.service.impl.JwtServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtServiceImpl Tests")
class JwtServiceImplTest {

    private JwtServiceImpl jwtService;
    private User user;

    // Test secret key — 256 bit base64 encoded
    private static final String TEST_SECRET =
            "dGVzdC1zZWNyZXQta2V5LWZvci11bml0LXRlc3Rpbmctc29sYXItZXJwLTI1Ng==";
    private static final long EXPIRY_MS = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl();
        ReflectionTestUtils.setField(
                jwtService, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(
                jwtService, "expirationMs", EXPIRY_MS);

        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("admin");
        user.setEmail("admin@solarerp.com");
        user.setRole(UserRole.ADMIN);
        user.setActive(true);
    }

    @Nested
    @DisplayName("generateToken()")
    class GenerateTokenTests {

        @Test
        @DisplayName("Generates non-null token for valid user")
        void generateToken_validUser_returnsNonNullToken() {
            String token = jwtService.generateToken(user);

            assertThat(token).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("Generated token has 3 parts separated by dots")
        void generateToken_validUser_hasThreeParts() {
            String token = jwtService.generateToken(user);

            assertThat(token.split("\\.")).hasSize(3);
        }

        @Test
        @DisplayName("Different users get different tokens")
        void generateToken_differentUsers_getDifferentTokens() {
            User anotherUser = new User();
            anotherUser.setId(UUID.randomUUID());
            anotherUser.setUsername("manager");
            anotherUser.setEmail("manager@solarerp.com");
            anotherUser.setRole(UserRole.MANAGER);
            anotherUser.setActive(true);

            String token1 = jwtService.generateToken(user);
            String token2 = jwtService.generateToken(anotherUser);

            assertThat(token1).isNotEqualTo(token2);
        }
    }

    @Nested
    @DisplayName("extractSubject()")
    class ExtractSubjectTests {

        @Test
        @DisplayName("Extracts user ID as subject from token")
        void extractSubject_validToken_returnsUserId() {
            String token = jwtService.generateToken(user);

            String subject = jwtService.extractSubject(token);

            assertThat(subject)
                    .isEqualTo(user.getId().toString());
        }
    }

    @Nested
    @DisplayName("isTokenValid()")
    class IsTokenValidTests {

        @Test
        @DisplayName("Returns true for freshly generated token")
        void isTokenValid_freshToken_returnsTrue() {
            String token = jwtService.generateToken(user);

            assertThat(jwtService.isTokenValid(token)).isTrue();
        }

        @Test
        @DisplayName("Returns false for malformed token")
        void isTokenValid_malformedToken_returnsFalse() {
            assertThat(jwtService.isTokenValid("not.a.token"))
                    .isFalse();
        }

        @Test
        @DisplayName("Returns false for empty token")
        void isTokenValid_emptyToken_returnsFalse() {
            assertThat(jwtService.isTokenValid("")).isFalse();
        }

        @Test
        @DisplayName("Returns false for expired token")
        void isTokenValid_expiredToken_returnsFalse() {
            // Set expiry to 1ms to force expiry
            ReflectionTestUtils.setField(
                    jwtService, "expirationMs", 1L);
            String token = jwtService.generateToken(user);

            // Wait for token to expire
            try { Thread.sleep(10); }
            catch (InterruptedException ignored) {}

            assertThat(jwtService.isTokenValid(token)).isFalse();
        }
    }

    @Nested
    @DisplayName("extractUsername()")
    class ExtractUsernameTests {

        @Test
        @DisplayName("Extracts username from valid token")
        void extractUsername_validToken_returnsUsername() {
            String token = jwtService.generateToken(user);

            String username = jwtService.extractUsername(token);

            assertThat(username).isEqualTo("admin");
        }
    }
}
