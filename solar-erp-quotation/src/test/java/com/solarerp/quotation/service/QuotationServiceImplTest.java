package com.solarerp.quotation.service;

import com.solarerp.customer.entity.Customer;
import com.solarerp.customer.repository.CustomerRepository;
import com.solarerp.costing.repository.CostingRepository;
import com.solarerp.exception.BadRequestException;
import com.solarerp.exception.ForbiddenException;
import com.solarerp.exception.ResourceNotFoundException;
import com.solarerp.material.repository.MaterialRepository;
import com.solarerp.material.service.MaterialService;
import com.solarerp.quotation.dto.QuotationRequest;
import com.solarerp.quotation.dto.QuotationResponse;
import com.solarerp.quotation.entity.Quotation;
import com.solarerp.quotation.entity.QuotationStatus;
import com.solarerp.quotation.repository.QuotationRepository;
import com.solarerp.quotation.service.impl.QuotationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuotationServiceImpl Tests")
class QuotationServiceImplTest {

    @Mock
    private QuotationRepository quotationRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private CostingRepository costingRepository;
    @Mock
    private MaterialRepository materialRepository;
    @Mock
    private MaterialService materialService;

    @InjectMocks
    private QuotationServiceImpl quotationService;

    private UUID quotationId;
    private UUID userId;
    private Quotation draftQuotation;
    private Quotation submittedQuotation;
    private Quotation rejectedQuotation;
    private Customer customer;

    @BeforeEach
    void setUp() {
        quotationId = UUID.randomUUID();
        userId = UUID.randomUUID();

        customer = new Customer();
        customer.setName("Test Customer");
        customer.setPhone("9876543210");

        draftQuotation = new Quotation();
        draftQuotation.setId(quotationId);
        draftQuotation.setQuotationNumber("QT-2026-001");
        draftQuotation.setStatus(QuotationStatus.DRAFT);
        draftQuotation.setCreatedBy(userId);
        draftQuotation.setCustomer(customer);
        draftQuotation.setValidityDays(30);
        draftQuotation.setDiscount(BigDecimal.ZERO);
        draftQuotation.setCostings(new ArrayList<>());
        draftQuotation.setInstalments(new ArrayList<>());
        draftQuotation.setPackages(new ArrayList<>());

        submittedQuotation = new Quotation();
        submittedQuotation.setId(quotationId);
        submittedQuotation.setQuotationNumber("QT-2026-001");
        submittedQuotation.setStatus(QuotationStatus.SUBMITTED);
        submittedQuotation.setCreatedBy(userId);
        submittedQuotation.setCustomer(customer);
        submittedQuotation.setValidityDays(30);
        submittedQuotation.setDiscount(BigDecimal.ZERO);
        submittedQuotation.setCostings(new ArrayList<>());
        submittedQuotation.setInstalments(new ArrayList<>());
        submittedQuotation.setPackages(new ArrayList<>());

        rejectedQuotation = new Quotation();
        rejectedQuotation.setId(quotationId);
        rejectedQuotation.setQuotationNumber("QT-2026-001");
        rejectedQuotation.setStatus(QuotationStatus.REJECTED);
        rejectedQuotation.setCreatedBy(userId);
        rejectedQuotation.setCustomer(customer);
        rejectedQuotation.setValidityDays(30);
        rejectedQuotation.setDiscount(BigDecimal.ZERO);
        rejectedQuotation.setCostings(new ArrayList<>());
        rejectedQuotation.setInstalments(new ArrayList<>());
        rejectedQuotation.setPackages(new ArrayList<>());
    }

    @Nested
    @DisplayName("submit()")
    class SubmitTests {

        @Test
        @DisplayName("DRAFT quotation can be submitted")
        void submit_draftQuotation_setsStatusToSubmitted() {
            when(quotationRepository.findById(quotationId))
                    .thenReturn(Optional.of(draftQuotation));
            when(quotationRepository.save(any()))
                    .thenReturn(draftQuotation);

            quotationService.submit(quotationId, userId);

            assertThat(draftQuotation.getStatus())
                    .isEqualTo(QuotationStatus.SUBMITTED);
            assertThat(draftQuotation.getSubmittedAt()).isNotNull();
        }

        @Test
        @DisplayName("REJECTED quotation resubmit sets status to REVISED")
        void submit_rejectedQuotation_setsStatusToRevised() {
            when(quotationRepository.findById(quotationId))
                    .thenReturn(Optional.of(rejectedQuotation));
            when(quotationRepository.save(any()))
                    .thenReturn(rejectedQuotation);

            quotationService.submit(quotationId, userId);

            assertThat(rejectedQuotation.getStatus())
                    .isEqualTo(QuotationStatus.REVISED);
            assertThat(rejectedQuotation.getRejectionReason()).isNull();
        }

        @Test
        @DisplayName("SUBMITTED quotation cannot be submitted again")
        void submit_submittedQuotation_throwsBadRequest() {
            when(quotationRepository.findById(quotationId))
                    .thenReturn(Optional.of(submittedQuotation));

            assertThatThrownBy(() ->
                    quotationService.submit(quotationId, userId))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("DRAFT or REJECTED");
        }

        @Test
        @DisplayName("Non-creator cannot submit quotation")
        void submit_differentUser_throwsForbidden() {
            UUID differentUserId = UUID.randomUUID();
            when(quotationRepository.findById(quotationId))
                    .thenReturn(Optional.of(draftQuotation));

            assertThatThrownBy(() ->
                    quotationService.submit(quotationId, differentUserId))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("Non-existent quotation throws ResourceNotFoundException")
        void submit_nonExistentQuotation_throwsNotFound() {
            when(quotationRepository.findById(quotationId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    quotationService.submit(quotationId, userId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Quotation");
        }
    }

    @Nested
    @DisplayName("approve()")
    class ApproveTests {

        @Test
        @DisplayName("SUBMITTED quotation can be approved")
        void approve_submittedQuotation_setsStatusToApproved() {
            when(quotationRepository.findById(quotationId))
                    .thenReturn(Optional.of(submittedQuotation));
            when(quotationRepository.save(any()))
                    .thenReturn(submittedQuotation);

            quotationService.approve(quotationId, "Looks good", userId);

            assertThat(submittedQuotation.getStatus())
                    .isEqualTo(QuotationStatus.APPROVED);
            assertThat(submittedQuotation.getApprovalNotes())
                    .isEqualTo("Looks good");
            assertThat(submittedQuotation.getApprovedRejectedBy())
                    .isEqualTo(userId);
            assertThat(submittedQuotation.getApprovedRejectedAt())
                    .isNotNull();
        }

        @Test
        @DisplayName("REVISED quotation can be approved")
        void approve_revisedQuotation_setsStatusToApproved() {
            Quotation revisedQuotation = new Quotation();
            revisedQuotation.setId(quotationId);
            revisedQuotation.setStatus(QuotationStatus.REVISED);
            revisedQuotation.setCustomer(customer);
            revisedQuotation.setValidityDays(30);
            revisedQuotation.setDiscount(BigDecimal.ZERO);
            revisedQuotation.setCostings(new ArrayList<>());
            revisedQuotation.setInstalments(new ArrayList<>());
            revisedQuotation.setPackages(new ArrayList<>());

            when(quotationRepository.findById(quotationId))
                    .thenReturn(Optional.of(revisedQuotation));
            when(quotationRepository.save(any()))
                    .thenReturn(revisedQuotation);

            quotationService.approve(quotationId, null, userId);

            assertThat(revisedQuotation.getStatus())
                    .isEqualTo(QuotationStatus.APPROVED);
        }

        @Test
        @DisplayName("DRAFT quotation cannot be approved")
        void approve_draftQuotation_throwsBadRequest() {
            when(quotationRepository.findById(quotationId))
                    .thenReturn(Optional.of(draftQuotation));

            assertThatThrownBy(() ->
                    quotationService.approve(quotationId, null, userId))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("SUBMITTED or REVISED");
        }
    }

    @Nested
    @DisplayName("reject()")
    class RejectTests {

        @Test
        @DisplayName("SUBMITTED quotation can be rejected with reason")
        void reject_submittedQuotation_setsStatusToRejected() {
            when(quotationRepository.findById(quotationId))
                    .thenReturn(Optional.of(submittedQuotation));
            when(quotationRepository.save(any()))
                    .thenReturn(submittedQuotation);

            quotationService.reject(quotationId, "Price too high", userId);

            assertThat(submittedQuotation.getStatus())
                    .isEqualTo(QuotationStatus.REJECTED);
            assertThat(submittedQuotation.getRejectionReason())
                    .isEqualTo("Price too high");
        }

        @Test
        @DisplayName("Rejection without reason throws BadRequestException")
        void reject_emptyReason_throwsBadRequest() {
            when(quotationRepository.findById(quotationId))
                    .thenReturn(Optional.of(submittedQuotation));

            assertThatThrownBy(() ->
                    quotationService.reject(quotationId, "", userId))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Rejection reason is required");
        }

        @Test
        @DisplayName("Rejection with null reason throws BadRequestException")
        void reject_nullReason_throwsBadRequest() {
            when(quotationRepository.findById(quotationId))
                    .thenReturn(Optional.of(submittedQuotation));

            assertThatThrownBy(() ->
                    quotationService.reject(quotationId, null, userId))
                    .isInstanceOf(BadRequestException.class);
        }
    }

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("DRAFT quotation can be deleted")
        void delete_draftQuotation_deletesSuccessfully() {
            when(quotationRepository.findById(quotationId))
                    .thenReturn(Optional.of(draftQuotation));

            quotationService.delete(quotationId);

            verify(quotationRepository, times(1))
                    .delete(draftQuotation);
        }

        @Test
        @DisplayName("Non-DRAFT quotation cannot be deleted")
        void delete_submittedQuotation_throwsBadRequest() {
            when(quotationRepository.findById(quotationId))
                    .thenReturn(Optional.of(submittedQuotation));

            assertThatThrownBy(() ->
                    quotationService.delete(quotationId))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Only DRAFT");
        }
    }
}
