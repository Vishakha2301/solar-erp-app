package com.solarerp.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Auth Integration Tests")
class AuthIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    class LoginTests {

        @Test
        @DisplayName("Returns 200 with token for valid credentials")
        void login_validCredentials_returns200WithToken() {
            Map<String, String> request = Map.of(
                    "identifier", "admin",
                    "password", "admin123");

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl() + "/api/v1/auth/login",
                    request,
                    Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get("token"))
                    .isNotNull()
                    .isInstanceOf(String.class);
            assertThat(response.getBody().get("username"))
                    .isEqualTo("admin");
        }

        @Test
        @DisplayName("Returns 400 for invalid credentials")
        void login_invalidCredentials_returns400() {
            Map<String, String> request = Map.of(
                    "identifier", "admin",
                    "password", "wrongpassword");

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl() + "/api/v1/auth/login",
                    request,
                    Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().get("status"))
                    .isEqualTo(400);
        }

        @Test
        @DisplayName("Returns 400 for non-existent user")
        void login_nonExistentUser_returns400() {
            Map<String, String> request = Map.of(
                    "identifier", "nonexistent",
                    "password", "password123");

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl() + "/api/v1/auth/login",
                    request,
                    Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Returns 401 for protected endpoint without token")
        void protectedEndpoint_withoutToken_returns401() {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    baseUrl() + "/api/v1/customers",
                    Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Returns 401 for protected endpoint with invalid token")
        void protectedEndpoint_withInvalidToken_returns401() {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth("invalid.jwt.token");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl() + "/api/v1/customers",
                    HttpMethod.GET,
                    entity,
                    Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("Token contains expected fields")
        void login_validCredentials_tokenContainsExpectedFields() {
            Map<String, String> request = Map.of(
                    "identifier", "admin",
                    "password", "admin123");

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl() + "/api/v1/auth/login",
                    request,
                    Map.class);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).containsKeys(
                    "token", "username", "role");
        }

        @Test
        @DisplayName("Login with email also works")
        void login_withEmail_returns200WithToken() {
            Map<String, String> request = Map.of(
                    "identifier", "admin@solarerp.com",
                    "password", "admin123");

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl() + "/api/v1/auth/login",
                    request,
                    Map.class);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().get("token"))
                    .isNotNull();
        }
    }
}
