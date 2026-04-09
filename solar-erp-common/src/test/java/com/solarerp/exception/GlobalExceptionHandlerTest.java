package com.solarerp.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/test");
    }

    @Nested
    @DisplayName("ResourceNotFoundException")
    class ResourceNotFoundTests {

        @Test
        @DisplayName("Returns 404 with correct error response")
        void handleNotFound_returns404() {
            ResourceNotFoundException ex =
                    new ResourceNotFoundException("Customer", "abc-123");

            ResponseEntity<ErrorResponse> response =
                    handler.handleNotFound(ex, request);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(404);
            assertThat(response.getBody().error()).isEqualTo("Not Found");
            assertThat(response.getBody().message())
                    .contains("Customer");
            assertThat(response.getBody().path())
                    .isEqualTo("/api/v1/test");
            assertThat(response.getBody().timestamp()).isNotNull();
        }

        @Test
        @DisplayName("ResourceNotFoundException with message only")
        void handleNotFound_messageOnly_returns404() {
            ResourceNotFoundException ex =
                    new ResourceNotFoundException("Resource not found");

            ResponseEntity<ErrorResponse> response =
                    handler.handleNotFound(ex, request);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody().message())
                    .isEqualTo("Resource not found");
        }
    }

    @Nested
    @DisplayName("ForbiddenException")
    class ForbiddenTests {

        @Test
        @DisplayName("Returns 403 with correct error response")
        void handleForbidden_returns403() {
            ForbiddenException ex = new ForbiddenException(
                    "You do not have permission");

            ResponseEntity<ErrorResponse> response =
                    handler.handleForbidden(ex, request);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody().status()).isEqualTo(403);
            assertThat(response.getBody().error()).isEqualTo("Forbidden");
            assertThat(response.getBody().message())
                    .isEqualTo("You do not have permission");
        }
    }

    @Nested
    @DisplayName("BadRequestException")
    class BadRequestTests {

        @Test
        @DisplayName("Returns 400 with correct error response")
        void handleBadRequest_returns400() {
            BadRequestException ex = new BadRequestException(
                    "Only DRAFT quotations can be deleted");

            ResponseEntity<ErrorResponse> response =
                    handler.handleBadRequest(ex, request);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().status()).isEqualTo(400);
            assertThat(response.getBody().error())
                    .isEqualTo("Bad Request");
            assertThat(response.getBody().message())
                    .isEqualTo("Only DRAFT quotations can be deleted");
        }
    }

    @Nested
    @DisplayName("DocumentGenerationException")
    class DocumentGenerationTests {

        @Test
        @DisplayName("Returns 500 with correct error response")
        void handleDocumentGeneration_returns500() {
            DocumentGenerationException ex =
                    new DocumentGenerationException(
                            "Failed to generate document");

            ResponseEntity<ErrorResponse> response =
                    handler.handleDocumentGeneration(ex, request);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody().status()).isEqualTo(500);
            assertThat(response.getBody().error())
                    .isEqualTo("Document Generation Failed");
            assertThat(response.getBody().message())
                    .isEqualTo("Failed to generate document");
        }

        @Test
        @DisplayName("DocumentGenerationException with cause")
        void handleDocumentGeneration_withCause_returns500() {
            DocumentGenerationException ex =
                    new DocumentGenerationException(
                            "Template not found",
                            new RuntimeException("File missing"));

            ResponseEntity<ErrorResponse> response =
                    handler.handleDocumentGeneration(ex, request);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody().message())
                    .isEqualTo("Template not found");
        }
    }

    @Nested
    @DisplayName("MethodArgumentNotValidException")
    class ValidationTests {

        @Test
        @DisplayName("Returns 400 with field error messages")
        void handleValidation_returns400WithFieldErrors() {
            MethodArgumentNotValidException ex =
                    mock(MethodArgumentNotValidException.class);
            BindingResult bindingResult = mock(BindingResult.class);

            FieldError fieldError = new FieldError(
                    "customerRequest", "name", "Required");

            when(ex.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getFieldErrors())
                    .thenReturn(List.of(fieldError));

            ResponseEntity<ErrorResponse> response =
                    handler.handleValidation(ex, request);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().status()).isEqualTo(400);
            assertThat(response.getBody().error())
                    .isEqualTo("Validation Failed");
            assertThat(response.getBody().message())
                    .contains("Required");
        }

        @Test
        @DisplayName("Combines multiple field errors with comma")
        void handleValidation_multipleErrors_combinesWithComma() {
            MethodArgumentNotValidException ex =
                    mock(MethodArgumentNotValidException.class);
            BindingResult bindingResult = mock(BindingResult.class);

            FieldError nameError = new FieldError(
                    "customerRequest", "name", "Required");
            FieldError phoneError = new FieldError(
                    "customerRequest", "phone", "Required");

            when(ex.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getFieldErrors())
                    .thenReturn(List.of(nameError, phoneError));

            ResponseEntity<ErrorResponse> response =
                    handler.handleValidation(ex, request);

            assertThat(response.getBody().message())
                    .contains("Required")
                    .contains(",");
        }
    }

    @Nested
    @DisplayName("IllegalArgumentException")
    class IllegalArgumentTests {

        @Test
        @DisplayName("Returns 400 for illegal argument")
        void handleIllegalArgument_returns400() {
            IllegalArgumentException ex =
                    new IllegalArgumentException("Invalid enum value");

            ResponseEntity<ErrorResponse> response =
                    handler.handleIllegalArgument(ex, request);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().message())
                    .isEqualTo("Invalid enum value");
        }
    }

    @Nested
    @DisplayName("Generic Exception")
    class GenericExceptionTests {

        @Test
        @DisplayName("Returns 500 with generic message for unexpected exceptions")
        void handleGeneral_returns500WithGenericMessage() {
            Exception ex = new RuntimeException("Unexpected error");

            ResponseEntity<ErrorResponse> response =
                    handler.handleGeneral(ex, request);

            assertThat(response.getStatusCode())
                    .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody().status()).isEqualTo(500);
            assertThat(response.getBody().error())
                    .isEqualTo("Internal Server Error");
            assertThat(response.getBody().message())
                    .isEqualTo("An unexpected error occurred");
        }
    }

    @Nested
    @DisplayName("ErrorResponse")
    class ErrorResponseTests {

        @Test
        @DisplayName("ErrorResponse.of() sets all fields correctly")
        void errorResponseOf_setsAllFields() {
            ErrorResponse response = ErrorResponse.of(
                    404, "Not Found", "Resource missing", "/api/test");

            assertThat(response.status()).isEqualTo(404);
            assertThat(response.error()).isEqualTo("Not Found");
            assertThat(response.message()).isEqualTo("Resource missing");
            assertThat(response.path()).isEqualTo("/api/test");
            assertThat(response.timestamp()).isNotNull();
        }
    }
}
