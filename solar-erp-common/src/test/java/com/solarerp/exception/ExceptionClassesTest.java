package com.solarerp.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.io.IOException;


import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Exception Classes Tests")
class ExceptionClassesTest {

    @Nested
    @DisplayName("ResourceNotFoundException")
    class ResourceNotFoundExceptionTests {

        @Test
        @DisplayName("Constructor with message sets message correctly")
        void constructor_withMessage_setsMessage() {
            ResourceNotFoundException ex =
                    new ResourceNotFoundException("Resource not found");

            assertThat(ex.getMessage())
                    .isEqualTo("Resource not found");
        }

        @Test
        @DisplayName("Constructor with resource and String id")
        void constructor_withResourceAndStringId_setsMessage() {
            ResourceNotFoundException ex =
                    new ResourceNotFoundException(
                            "Customer", "abc-123");

            assertThat(ex.getMessage())
                    .isEqualTo("Customer not found: abc-123");
        }

        @Test
        @DisplayName("Constructor with resource and UUID id")
        void constructor_withResourceAndUuidId_setsMessage() {
            UUID id = UUID.fromString(
                    "123e4567-e89b-12d3-a456-426614174000");
            ResourceNotFoundException ex =
                    new ResourceNotFoundException("Quotation", id);

            assertThat(ex.getMessage())
                    .isEqualTo("Quotation not found: "
                            + id.toString());
        }

        @Test
        @DisplayName("Is RuntimeException")
        void isRuntimeException() {
            ResourceNotFoundException ex =
                    new ResourceNotFoundException("test");

            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("ForbiddenException")
    class ForbiddenExceptionTests {

        @Test
        @DisplayName("Constructor sets message correctly")
        void constructor_setsMessage() {
            ForbiddenException ex = new ForbiddenException(
                    "You do not have permission");

            assertThat(ex.getMessage())
                    .isEqualTo("You do not have permission");
        }

        @Test
        @DisplayName("Is RuntimeException")
        void isRuntimeException() {
            ForbiddenException ex =
                    new ForbiddenException("test");

            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("BadRequestException")
    class BadRequestExceptionTests {

        @Test
        @DisplayName("Constructor sets message correctly")
        void constructor_setsMessage() {
            BadRequestException ex = new BadRequestException(
                    "Only DRAFT quotations can be deleted");

            assertThat(ex.getMessage())
                    .isEqualTo("Only DRAFT quotations can be deleted");
        }

        @Test
        @DisplayName("Is RuntimeException")
        void isRuntimeException() {
            BadRequestException ex =
                    new BadRequestException("test");

            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("DocumentGenerationException")
    class DocumentGenerationExceptionTests {

        @Test
        @DisplayName("Constructor with message sets message correctly")
        void constructor_withMessage_setsMessage() {
            DocumentGenerationException ex =
                    new DocumentGenerationException(
                            "Failed to generate document");

            assertThat(ex.getMessage())
                    .isEqualTo("Failed to generate document");
        }

        @Test
        @DisplayName("Constructor with message and cause")
        void constructor_withMessageAndCause_setsBoth() {
            RuntimeException cause =
                    new RuntimeException("Template missing");
            DocumentGenerationException ex =
                    new DocumentGenerationException(
                            "Failed to generate document", cause);

            assertThat(ex.getMessage())
                    .isEqualTo("Failed to generate document");
            assertThat(ex.getCause()).isEqualTo(cause);
            assertThat(ex.getCause().getMessage())
                    .isEqualTo("Template missing");
        }

        @Test
        @DisplayName("Is RuntimeException")
        void isRuntimeException() {
            DocumentGenerationException ex =
                    new DocumentGenerationException("test");

            assertThat(ex).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Cause is preserved correctly")
        void causeIsPreservedCorrectly() {
            IOException ioEx = new IOException("File not found");
            DocumentGenerationException ex =
                    new DocumentGenerationException(
                            "IO error", ioEx);

            assertThat(ex.getCause())
                    .isInstanceOf(IOException.class);
            assertThat(ex.getCause().getMessage())
                    .isEqualTo("File not found");
        }
    }

    @Nested
    @DisplayName("ErrorResponse")
    class ErrorResponseTests {

        @Test
        @DisplayName("of() creates response with all fields")
        void of_createsResponseWithAllFields() {
            ErrorResponse response = ErrorResponse.of(
                    404,
                    "Not Found",
                    "Customer not found",
                    "/api/v1/customers/123");

            assertThat(response.status()).isEqualTo(404);
            assertThat(response.error()).isEqualTo("Not Found");
            assertThat(response.message())
                    .isEqualTo("Customer not found");
            assertThat(response.path())
                    .isEqualTo("/api/v1/customers/123");
            assertThat(response.timestamp()).isNotNull();
        }

        @Test
        @DisplayName("of() sets timestamp to current time")
        void of_setsTimestampToCurrentTime() {
            long before = System.currentTimeMillis();
            ErrorResponse response = ErrorResponse.of(
                    500, "Error", "message", "/path");
            long after = System.currentTimeMillis();

            assertThat(response.timestamp().toEpochMilli())
                    .isBetween(before, after);
        }

        @Test
        @DisplayName("Different calls produce different timestamps")
        void of_differentCalls_produceDifferentTimestamps()
                throws InterruptedException {
            ErrorResponse response1 = ErrorResponse.of(
                    404, "Not Found", "msg1", "/path1");
            Thread.sleep(5);
            ErrorResponse response2 = ErrorResponse.of(
                    400, "Bad Request", "msg2", "/path2");

            assertThat(response1.timestamp())
                    .isNotEqualTo(response2.timestamp());
        }
    }
}
