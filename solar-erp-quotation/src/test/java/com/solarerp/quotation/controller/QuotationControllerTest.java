package com.solarerp.quotation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solarerp.customer.dto.CustomerResponse;
import com.solarerp.customer.entity.CustomerType;
import com.solarerp.exception.BadRequestException;
import com.solarerp.exception.GlobalExceptionHandler;
import com.solarerp.exception.ResourceNotFoundException;
import com.solarerp.quotation.dto.*;
import com.solarerp.quotation.entity.QuotationStatus;
import com.solarerp.quotation.service.QuotationDocumentService;
import com.solarerp.quotation.service.QuotationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuotationController Tests")
class QuotationControllerTest {

    @Mock
    private QuotationService quotationService;

    @Mock
    private QuotationDocumentService quotationDocumentService;

    @InjectMocks
    private QuotationController quotationController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UUID quotationId;
    private UUID customerId;
    private UUID userId;
    private QuotationResponse quotationResponse;
    private QuotationRequest quotationRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(quotationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        quotationId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        userId = UUID.randomUUID();

        CustomerResponse customerResponse = new CustomerResponse(
                customerId,
                CustomerType.INDIVIDUAL,
                "John Doe",
                null,
                "9876543210",
                null, null, null, null, null, null,
                true,
                Instant.now(),
                userId,
                List.of()
        );

        quotationResponse = new QuotationResponse(
                quotationId,
                "QT-2026-001",
                customerResponse,
                null,
                QuotationStatus.DRAFT,
                "ONGRID 5KW",
                30,
                BigDecimal.ZERO,
                null, null, null, null,
                false, null, null, null,
                userId,
                null, null, null,
                Instant.now(),
                Instant.now(),
                List.of(),
                List.of(),
                List.of()
        );

        quotationRequest = new QuotationRequest(
                customerId,
                null,
                "ONGRID 5KW",
                30,
                BigDecimal.ZERO,
                null, null, null, null,
                false, null,
                List.of(new QuotationCostingRequest(
                        UUID.randomUUID(), "Main Roof",
                        BigDecimal.ZERO)),
                List.of(new QuotationInstalmentRequest(
                        1, "Advance", BigDecimal.valueOf(10))),
                null
        );
    }

    @Nested
    @DisplayName("GET /api/v1/quotations")
    class GetAllTests {

        @Test
        @DisplayName("Returns 200 with list of quotations")
        void getAll_returns200WithQuotations() throws Exception {
            when(quotationService.getAll())
                    .thenReturn(List.of(quotationResponse));

            mockMvc.perform(get("/api/v1/quotations"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].quotationNumber")
                            .value("QT-2026-001"))
                    .andExpect(jsonPath("$[0].status")
                            .value("DRAFT"));
        }

        @Test
        @DisplayName("Returns 200 with empty list")
        void getAll_noQuotations_returns200WithEmptyList()
                throws Exception {
            when(quotationService.getAll()).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/quotations"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/quotations/{id}")
    class GetByIdTests {

        @Test
        @DisplayName("Returns 200 with quotation when found")
        void getById_existingId_returns200() throws Exception {
            when(quotationService.getById(quotationId))
                    .thenReturn(quotationResponse);

            mockMvc.perform(
                            get("/api/v1/quotations/{id}", quotationId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id")
                            .value(quotationId.toString()))
                    .andExpect(jsonPath("$.quotationNumber")
                            .value("QT-2026-001"));
        }

        @Test
        @DisplayName("Returns 404 when quotation not found")
        void getById_notFound_returns404() throws Exception {
            when(quotationService.getById(quotationId))
                    .thenThrow(new ResourceNotFoundException(
                            "Quotation", quotationId));

            mockMvc.perform(
                            get("/api/v1/quotations/{id}", quotationId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/quotations/status/{status}")
    class GetByStatusTests {

        @Test
        @DisplayName("Returns 200 with quotations filtered by status")
        void getByStatus_draft_returns200() throws Exception {
            when(quotationService.getByStatus(QuotationStatus.DRAFT))
                    .thenReturn(List.of(quotationResponse));

            mockMvc.perform(get(
                            "/api/v1/quotations/status/{status}",
                            "DRAFT"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].status")
                            .value("DRAFT"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/quotations/customer/{customerId}")
    class GetByCustomerTests {

        @Test
        @DisplayName("Returns 200 with customer quotations")
        void getByCustomer_returns200() throws Exception {
            when(quotationService.getByCustomer(customerId))
                    .thenReturn(List.of(quotationResponse));

            mockMvc.perform(get(
                            "/api/v1/quotations/customer/{customerId}",
                            customerId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].customer.id")
                            .value(customerId.toString()));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/quotations")
    class CreateTests {

        @Test
        @DisplayName("Returns 201 when quotation created")
        void create_validRequest_returns201() throws Exception {
            when(quotationService.create(any(), any()))
                    .thenReturn(quotationResponse);

            mockMvc.perform(post("/api/v1/quotations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper
                                    .writeValueAsString(quotationRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.quotationNumber")
                            .value("QT-2026-001"));
        }

        @Test
        @DisplayName("Returns 404 when customer not found")
        void create_customerNotFound_returns404() throws Exception {
            when(quotationService.create(any(), any()))
                    .thenThrow(new ResourceNotFoundException(
                            "Customer", customerId));

            mockMvc.perform(post("/api/v1/quotations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper
                                    .writeValueAsString(quotationRequest)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/quotations/{id}")
    class UpdateTests {

        @Test
        @DisplayName("Returns 200 when quotation updated")
        void update_validRequest_returns200() throws Exception {
            when(quotationService.update(eq(quotationId), any()))
                    .thenReturn(quotationResponse);

            mockMvc.perform(
                            put("/api/v1/quotations/{id}", quotationId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper
                                            .writeValueAsString(
                                                    quotationRequest)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Returns 400 when updating non-DRAFT quotation")
        void update_nonDraft_returns400() throws Exception {
            when(quotationService.update(eq(quotationId), any()))
                    .thenThrow(new BadRequestException(
                            "Only DRAFT or REJECTED quotations can be edited"));

            mockMvc.perform(
                            put("/api/v1/quotations/{id}", quotationId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper
                                            .writeValueAsString(
                                                    quotationRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Only DRAFT or REJECTED quotations can be edited"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/quotations/{id}/submit")
    class SubmitTests {

        @Test
        @DisplayName("Returns 200 when quotation submitted")
        void submit_draftQuotation_returns200() throws Exception {
            QuotationResponse submittedResponse = new QuotationResponse(
                    quotationId, "QT-2026-001",
                    quotationResponse.customer(),
                    null, QuotationStatus.SUBMITTED,
                    "ONGRID 5KW", 30, BigDecimal.ZERO,
                    null, null, null, null,
                    false, null, null, null,
                    userId, Instant.now(), null, null,
                    Instant.now(), Instant.now(),
                    List.of(), List.of(), List.of()
            );

            when(quotationService.submit(eq(quotationId), any()))
                    .thenReturn(submittedResponse);

            mockMvc.perform(post(
                            "/api/v1/quotations/{id}/submit",
                            quotationId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status")
                            .value("SUBMITTED"));
        }

        @Test
        @DisplayName("Returns 400 when already submitted")
        void submit_alreadySubmitted_returns400() throws Exception {
            when(quotationService.submit(eq(quotationId), any()))
                    .thenThrow(new BadRequestException(
                            "Only DRAFT or REJECTED quotations can be submitted"));

            mockMvc.perform(post(
                            "/api/v1/quotations/{id}/submit",
                            quotationId))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/quotations/{id}/approve")
    class ApproveTests {

        @Test
        @DisplayName("Returns 200 when quotation approved")
        void approve_submittedQuotation_returns200() throws Exception {
            QuotationResponse approvedResponse = new QuotationResponse(
                    quotationId, "QT-2026-001",
                    quotationResponse.customer(),
                    null, QuotationStatus.APPROVED,
                    "ONGRID 5KW", 30, BigDecimal.ZERO,
                    null, null, null, null,
                    false, null, null, "Looks good",
                    userId, null, userId, Instant.now(),
                    Instant.now(), Instant.now(),
                    List.of(), List.of(), List.of()
            );

            when(quotationService.approve(
                    eq(quotationId), any(), any()))
                    .thenReturn(approvedResponse);

            mockMvc.perform(post(
                            "/api/v1/quotations/{id}/approve",
                            quotationId)
                            .param("approvalNotes", "Looks good"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status")
                            .value("APPROVED"));
        }

        @Test
        @DisplayName("Returns 400 when approving non-submitted quotation")
        void approve_nonSubmitted_returns400() throws Exception {
            when(quotationService.approve(
                    eq(quotationId), any(), any()))
                    .thenThrow(new BadRequestException(
                            "Only SUBMITTED or REVISED quotations can be approved"));

            mockMvc.perform(post(
                            "/api/v1/quotations/{id}/approve",
                            quotationId))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/quotations/{id}/reject")
    class RejectTests {

        @Test
        @DisplayName("Returns 200 when quotation rejected")
        void reject_submittedQuotation_returns200() throws Exception {
            QuotationResponse rejectedResponse = new QuotationResponse(
                    quotationId, "QT-2026-001",
                    quotationResponse.customer(),
                    null, QuotationStatus.REJECTED,
                    "ONGRID 5KW", 30, BigDecimal.ZERO,
                    null, null, null, null,
                    false, null, "Price too high", null,
                    userId, null, userId, Instant.now(),
                    Instant.now(), Instant.now(),
                    List.of(), List.of(), List.of()
            );

            when(quotationService.reject(
                    eq(quotationId), any(), any()))
                    .thenReturn(rejectedResponse);

            mockMvc.perform(post(
                            "/api/v1/quotations/{id}/reject",
                            quotationId)
                            .param("rejectionReason", "Price too high"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status")
                            .value("REJECTED"))
                    .andExpect(jsonPath("$.rejectionReason")
                            .value("Price too high"));
        }

        @Test
        @DisplayName("Returns 400 when rejecting non-submitted quotation")
        void reject_nonSubmitted_returns400() throws Exception {
            when(quotationService.reject(
                    eq(quotationId), any(), any()))
                    .thenThrow(new BadRequestException(
                            "Only SUBMITTED or REVISED quotations can be rejected"));

            mockMvc.perform(post(
                            "/api/v1/quotations/{id}/reject",
                            quotationId)
                            .param("rejectionReason", "Price too high"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/quotations/{id}")
    class DeleteTests {

        @Test
        @DisplayName("Returns 204 when quotation deleted")
        void delete_draftQuotation_returns204() throws Exception {
            doNothing().when(quotationService).delete(quotationId);

            mockMvc.perform(
                            delete("/api/v1/quotations/{id}",
                                    quotationId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Returns 400 when deleting non-DRAFT quotation")
        void delete_nonDraft_returns400() throws Exception {
            doThrow(new BadRequestException(
                    "Only DRAFT quotations can be deleted"))
                    .when(quotationService).delete(quotationId);

            mockMvc.perform(
                            delete("/api/v1/quotations/{id}",
                                    quotationId))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Returns 404 when quotation not found")
        void delete_notFound_returns404() throws Exception {
            doThrow(new ResourceNotFoundException(
                    "Quotation", quotationId))
                    .when(quotationService).delete(quotationId);

            mockMvc.perform(
                            delete("/api/v1/quotations/{id}",
                                    quotationId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/quotations/{id}/document")
    class DocumentTests {

        @Test
        @DisplayName("Returns 200 with docx bytes")
        void downloadDocument_returns200WithBytes()
                throws Exception {
            byte[] docBytes = "fake-docx-content".getBytes();
            when(quotationDocumentService.generateDocx(quotationId))
                    .thenReturn(docBytes);

            mockMvc.perform(get(
                            "/api/v1/quotations/{id}/document",
                            quotationId))
                    .andExpect(status().isOk())
                    .andExpect(header().string(
                            "Content-Disposition",
                            org.hamcrest.Matchers.containsString(
                                    "attachment")))
                    .andExpect(content().bytes(docBytes));
        }

        @Test
        @DisplayName("Returns 404 when quotation not found")
        void downloadDocument_notFound_returns404() throws Exception {
            when(quotationDocumentService.generateDocx(quotationId))
                    .thenThrow(new ResourceNotFoundException(
                            "Quotation", quotationId));

            mockMvc.perform(get(
                            "/api/v1/quotations/{id}/document",
                            quotationId))
                    .andExpect(status().isNotFound());
        }
    }
}
