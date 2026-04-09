package com.solarerp.costing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solarerp.costing.dto.CostingContextDto;
import com.solarerp.costing.dto.CostingSnapshotDto;
import com.solarerp.costing.dto.SavedCostingRequest;
import com.solarerp.costing.dto.SavedCostingResponse;
import com.solarerp.costing.service.CostingService;
import com.solarerp.exception.ForbiddenException;
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
@DisplayName("CostingController Tests")
class CostingControllerTest {

    @Mock
    private CostingService costingService;

    @InjectMocks
    private CostingController costingController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UUID costingId;
    private UUID userId;
    private SavedCostingResponse costingResponse;
    private SavedCostingRequest costingRequest;
    private CostingContextDto contextDto;
    private CostingSnapshotDto snapshotDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(costingController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        costingId = UUID.randomUUID();
        userId = UUID.randomUUID();

        contextDto = new CostingContextDto(
                5.0, "Rooftop", "3PH", "RCC",
                "Main Roof", false);

        snapshotDto = new CostingSnapshotDto(
                300000.0, 0.0, 15000.0,
                10000.0, 5000.0, 7500.0,
                337500.0, 367537.5, 73.5);

        costingResponse = new SavedCostingResponse(
                costingId,
                Instant.now(),
                userId,
                contextDto,
                snapshotDto);

        costingRequest = new SavedCostingRequest(
                contextDto, snapshotDto);
    }

    @Nested
    @DisplayName("GET /api/v1/costings")
    class GetAllTests {

        @Test
        @DisplayName("Returns 200 with list of costings")
        void getAll_returns200WithCostings() throws Exception {
            when(costingService.getAll())
                    .thenReturn(List.of(costingResponse));

            mockMvc.perform(get("/api/v1/costings"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id")
                            .value(costingId.toString()))
                    .andExpect(
                            jsonPath("$[0].context.plantCapacity")
                                    .value(5.0))
                    .andExpect(
                            jsonPath("$[0].snapshot.grandTotal")
                                    .value(337500.0));
        }

        @Test
        @DisplayName("Returns 200 with empty list when no costings")
        void getAll_noCostings_returns200WithEmptyList()
                throws Exception {
            when(costingService.getAll()).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/costings"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/costings/{id}")
    class GetByIdTests {

        @Test
        @DisplayName("Returns 200 with costing when found")
        void getById_existingId_returns200() throws Exception {
            when(costingService.getById(costingId))
                    .thenReturn(costingResponse);

            mockMvc.perform(
                            get("/api/v1/costings/{id}", costingId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id")
                            .value(costingId.toString()))
                    .andExpect(jsonPath("$.context.roofIdentifier")
                            .value("Main Roof"));
        }

        @Test
        @DisplayName("Returns 404 when costing not found")
        void getById_notFound_returns404() throws Exception {
            when(costingService.getById(costingId))
                    .thenThrow(new ResourceNotFoundException(
                            "Costing", costingId));

            mockMvc.perform(
                            get("/api/v1/costings/{id}", costingId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/costings")
    class CreateTests {

        @Test
        @DisplayName("Returns 201 when costing created")
        void create_validRequest_returns201() throws Exception {
            when(costingService.create(any(), any()))
                    .thenReturn(costingResponse);

            mockMvc.perform(post("/api/v1/costings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper
                                    .writeValueAsString(costingRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id")
                            .value(costingId.toString()));
        }

        @Test
        @DisplayName("Returns 400 when context is null")
        void create_nullContext_returns400() throws Exception {
            SavedCostingRequest invalidRequest =
                    new SavedCostingRequest(null, snapshotDto);

            mockMvc.perform(post("/api/v1/costings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper
                                    .writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Returns 400 when snapshot is null")
        void create_nullSnapshot_returns400() throws Exception {
            SavedCostingRequest invalidRequest =
                    new SavedCostingRequest(contextDto, null);

            mockMvc.perform(post("/api/v1/costings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper
                                    .writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/costings/{id}")
    class UpdateTests {

        @Test
        @DisplayName("Returns 200 when costing updated")
        void update_validRequest_returns200() throws Exception {
            when(costingService.update(eq(costingId), any(), any()))
                    .thenReturn(costingResponse);

            mockMvc.perform(
                            put("/api/v1/costings/{id}", costingId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper
                                            .writeValueAsString(
                                                    costingRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id")
                            .value(costingId.toString()));
        }

        @Test
        @DisplayName("Returns 404 when costing not found")
        void update_notFound_returns404() throws Exception {
            when(costingService.update(eq(costingId), any(), any()))
                    .thenThrow(new ResourceNotFoundException(
                            "Costing", costingId));

            mockMvc.perform(
                            put("/api/v1/costings/{id}", costingId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper
                                            .writeValueAsString(
                                                    costingRequest)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Returns 403 when non-owner updates")
        void update_nonOwner_returns403() throws Exception {
            when(costingService.update(eq(costingId), any(), any()))
                    .thenThrow(new ForbiddenException(
                            "You do not have permission"));

            mockMvc.perform(
                            put("/api/v1/costings/{id}", costingId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper
                                            .writeValueAsString(
                                                    costingRequest)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/costings/{id}")
    class DeleteTests {

        @Test
        @DisplayName("Returns 204 when costing deleted")
        void delete_existingCosting_returns204() throws Exception {
            doNothing().when(costingService)
                    .delete(eq(costingId), any());

            mockMvc.perform(
                            delete("/api/v1/costings/{id}", costingId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Returns 404 when costing not found")
        void delete_notFound_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Costing", costingId))
                    .when(costingService).delete(eq(costingId), any());

            mockMvc.perform(
                            delete("/api/v1/costings/{id}", costingId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Returns 403 when non-owner deletes")
        void delete_nonOwner_returns403() throws Exception {
            doThrow(new ForbiddenException(
                    "You do not have permission"))
                    .when(costingService).delete(eq(costingId), any());

            mockMvc.perform(
                            delete("/api/v1/costings/{id}", costingId))
                    .andExpect(status().isForbidden());
        }
    }
}
