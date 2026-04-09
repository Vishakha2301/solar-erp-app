package com.solarerp.customer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solarerp.customer.dto.CustomerRequest;
import com.solarerp.customer.dto.CustomerResponse;
import com.solarerp.customer.entity.CustomerType;
import com.solarerp.customer.service.CustomerService;
import com.solarerp.exception.GlobalExceptionHandler;
import com.solarerp.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerController Tests")
class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UUID customerId;
    private UUID userId;
    private CustomerResponse customerResponse;
    private CustomerRequest customerRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(customerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        customerId = UUID.randomUUID();
        userId = UUID.randomUUID();

        customerResponse = new CustomerResponse(
                customerId,
                CustomerType.INDIVIDUAL,
                "John Doe",
                null,
                "9876543210",
                "john@example.com",
                "123 Main Street",
                "Mumbai",
                "Maharashtra",
                "400001",
                null,
                true,
                Instant.now(),
                userId,
                List.of()
        );

        customerRequest = new CustomerRequest(
                CustomerType.INDIVIDUAL,
                "John Doe",
                null,
                "9876543210",
                "john@example.com",
                "123 Main Street",
                "Mumbai",
                "Maharashtra",
                "400001",
                null,
                List.of()
        );
    }

    @Nested
    @DisplayName("GET /api/v1/customers")
    class GetAllTests {

        @Test
        @DisplayName("Returns 200 with list of customers")
        void getAll_returns200WithCustomers() throws Exception {
            when(customerService.getAll())
                    .thenReturn(List.of(customerResponse));

            mockMvc.perform(get("/api/v1/customers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].name")
                            .value("John Doe"))
                    .andExpect(jsonPath("$[0].phone")
                            .value("9876543210"));
        }

        @Test
        @DisplayName("Returns 200 with empty list when no customers")
        void getAll_noCustomers_returns200WithEmptyList()
                throws Exception {
            when(customerService.getAll()).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/customers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/customers/{id}")
    class GetByIdTests {

        @Test
        @DisplayName("Returns 200 with customer when found")
        void getById_existingId_returns200() throws Exception {
            when(customerService.getById(customerId))
                    .thenReturn(customerResponse);

            mockMvc.perform(get("/api/v1/customers/{id}", customerId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id")
                            .value(customerId.toString()))
                    .andExpect(jsonPath("$.name")
                            .value("John Doe"));
        }

        @Test
        @DisplayName("Returns 404 when customer not found")
        void getById_notFound_returns404() throws Exception {
            when(customerService.getById(customerId))
                    .thenThrow(new ResourceNotFoundException(
                            "Customer", customerId));

            mockMvc.perform(get("/api/v1/customers/{id}", customerId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error")
                            .value("Not Found"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/customers/search")
    class SearchTests {

        @Test
        @DisplayName("Returns 200 with matching customers")
        void search_matchingName_returns200() throws Exception {
            when(customerService.search("John"))
                    .thenReturn(List.of(customerResponse));

            mockMvc.perform(get("/api/v1/customers/search")
                            .param("name", "John"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name")
                            .value("John Doe"));
        }

        @Test
        @DisplayName("Returns 200 with empty list when no match")
        void search_noMatch_returns200WithEmptyList()
                throws Exception {
            when(customerService.search("Unknown"))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/v1/customers/search")
                            .param("name", "Unknown"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/customers")
    class CreateTests {

        @Test
        @DisplayName("Returns 201 when customer created")
        void create_validRequest_returns201() throws Exception {
            when(customerService.create(any(), any()))
                    .thenReturn(customerResponse);

            Jwt jwt = mock(Jwt.class);
            when(jwt.getSubject()).thenReturn(userId.toString());

            mockMvc.perform(post("/api/v1/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper
                                    .writeValueAsString(customerRequest))
                            .requestAttr("jwt", jwt))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name")
                            .value("John Doe"));
        }

        @Test
        @DisplayName("Returns 400 when name is blank")
        void create_blankName_returns400() throws Exception {
            CustomerRequest invalidRequest = new CustomerRequest(
                    CustomerType.INDIVIDUAL,
                    "",
                    null,
                    "9876543210",
                    null, null, null, null, null, null,
                    List.of()
            );

            mockMvc.perform(post("/api/v1/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper
                                    .writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Returns 400 when phone is blank")
        void create_blankPhone_returns400() throws Exception {
            CustomerRequest invalidRequest = new CustomerRequest(
                    CustomerType.INDIVIDUAL,
                    "John Doe",
                    null,
                    "",
                    null, null, null, null, null, null,
                    List.of()
            );

            mockMvc.perform(post("/api/v1/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper
                                    .writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/customers/{id}")
    class UpdateTests {

        @Test
        @DisplayName("Returns 200 when customer updated")
        void update_validRequest_returns200() throws Exception {
            when(customerService.update(eq(customerId), any()))
                    .thenReturn(customerResponse);

            mockMvc.perform(put("/api/v1/customers/{id}", customerId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper
                                    .writeValueAsString(customerRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name")
                            .value("John Doe"));
        }

        @Test
        @DisplayName("Returns 404 when customer not found")
        void update_notFound_returns404() throws Exception {
            when(customerService.update(eq(customerId), any()))
                    .thenThrow(new ResourceNotFoundException(
                            "Customer", customerId));

            mockMvc.perform(put("/api/v1/customers/{id}", customerId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper
                                    .writeValueAsString(customerRequest)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/customers/{id}")
    class DeactivateTests {

        @Test
        @DisplayName("Returns 204 when customer deactivated")
        void deactivate_existingCustomer_returns204()
                throws Exception {
            doNothing().when(customerService).deactivate(customerId);

            mockMvc.perform(
                            delete("/api/v1/customers/{id}", customerId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Returns 404 when customer not found")
        void deactivate_notFound_returns404() throws Exception {
            doThrow(new ResourceNotFoundException(
                    "Customer", customerId))
                    .when(customerService).deactivate(customerId);

            mockMvc.perform(
                            delete("/api/v1/customers/{id}", customerId))
                    .andExpect(status().isNotFound());
        }
    }
}
