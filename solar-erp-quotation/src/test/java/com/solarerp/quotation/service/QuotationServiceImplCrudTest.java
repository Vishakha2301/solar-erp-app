package com.solarerp.quotation.service;

import com.solarerp.costing.entity.SavedCostingEntity;
import com.solarerp.costing.repository.CostingRepository;
import com.solarerp.customer.entity.Customer;
import com.solarerp.customer.entity.CustomerType;
import com.solarerp.customer.entity.CustomerSite;
import com.solarerp.customer.repository.CustomerRepository;
import com.solarerp.exception.BadRequestException;
import com.solarerp.exception.ResourceNotFoundException;
import com.solarerp.material.repository.MaterialRepository;
import com.solarerp.material.service.MaterialService;
import com.solarerp.quotation.dto.*;
import com.solarerp.quotation.entity.*;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuotationServiceImpl CRUD Tests")
class QuotationServiceImplCrudTest {

    @Mock private QuotationRepository quotationRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private CostingRepository costingRepository;
    @Mock private MaterialRepository materialRepository;
    @Mock private MaterialService materialService;

    @InjectMocks
    private QuotationServiceImpl quotationService;

    private UUID quotationId;
    private UUID customerId;
    private UUID costingId;
    private UUID userId;
    private Customer customer;
    private CustomerSite site;
    private SavedCostingEntity costingEntity;
    private Quotation quotation;
    private QuotationRequest request;

    @BeforeEach
    void setUp() {
        quotationId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        costingId = UUID.randomUUID();
        userId = UUID.randomUUID();

        site = new CustomerSite();
        site.setId(UUID.randomUUID());
        site.setSiteLabel("Main Roof");

        customer = new Customer();
        customer.setId(customerId);
        customer.setCustomerType(CustomerType.INDIVIDUAL);
        customer.setName("John Doe");
        customer.setPhone("9876543210");
        customer.setActive(true);
        customer.setCreatedBy(userId);
        customer.setSites(List.of(site));

        costingEntity = new SavedCostingEntity();
        costingEntity.setContext("{}");
        costingEntity.setSnapshot("{}");

        quotation = new Quotation();
        quotation.setId(quotationId);
        quotation.setQuotationNumber("QT-2026-001");
        quotation.setStatus(QuotationStatus.DRAFT);
        quotation.setCustomer(customer);
        quotation.setCreatedBy(userId);
        quotation.setValidityDays(30);
        quotation.setDiscount(BigDecimal.ZERO);
        quotation.setCostings(new ArrayList<>());
        quotation.setInstalments(new ArrayList<>());
        quotation.setPackages(new ArrayList<>());

        request = new QuotationRequest(
                customerId,
                null,
                "ONGRID 5KW",
                30,
                BigDecimal.ZERO,
                null,
                null,
                null,
                null,
                false,
                null,
                List.of(new QuotationCostingRequest(
                        costingId, "Main Roof", BigDecimal.ZERO)),
                List.of(new QuotationInstalmentRequest(
                        1, "Advance",
                        BigDecimal.valueOf(10))),
                null
        );
    }

    @Nested
    @DisplayName("getAll()")
    class GetAllTests {

        @Test
        @DisplayName("Returns all quotations ordered by createdAt desc")
        void getAll_returnsAllQuotations() {
            when(quotationRepository.findAllByOrderByCreatedAtDesc())
                    .thenReturn(List.of(quotation));

            List<QuotationResponse> result = quotationService.getAll();

            assertThat(result).hasSize(1);
            verify(quotationRepository, times(1))
                    .findAllByOrderByCreatedAtDesc();
        }

        @Test
        @DisplayName("Returns empty list when no quotations")
        void getAll_noQuotations_returnsEmptyList() {
            when(quotationRepository.findAllByOrderByCreatedAtDesc())
                    .thenReturn(List.of());

            List<QuotationResponse> result = quotationService.getAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("Returns quotation when found")
        void getById_existingId_returnsQuotation() {
            when(quotationRepository.findById(quotationId))
                    .thenReturn(Optional.of(quotation));

            QuotationResponse result =
                    quotationService.getById(quotationId);

            assertThat(result).isNotNull();
            assertThat(result.quotationNumber())
                    .isEqualTo("QT-2026-001");
        }

        @Test
        @DisplayName("Throws ResourceNotFoundException when not found")
        void getById_nonExistentId_throwsNotFound() {
            when(quotationRepository.findById(quotationId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    quotationService.getById(quotationId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Quotation");
        }
    }

    @Nested
    @DisplayName("getByStatus()")
    class GetByStatusTests {

        @Test
        @DisplayName("Returns quotations filtered by status")
        void getByStatus_draft_returnsDraftQuotations() {
            when(quotationRepository
                    .findByStatusOrderByCreatedAtDesc(
                            QuotationStatus.DRAFT))
                    .thenReturn(List.of(quotation));

            List<QuotationResponse> result =
                    quotationService.getByStatus(QuotationStatus.DRAFT);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).status())
                    .isEqualTo(QuotationStatus.DRAFT);
        }

        @Test
        @DisplayName("Returns empty list when no quotations with status")
        void getByStatus_approved_returnsEmptyList() {
            when(quotationRepository
                    .findByStatusOrderByCreatedAtDesc(
                            QuotationStatus.APPROVED))
                    .thenReturn(List.of());

            List<QuotationResponse> result = quotationService
                    .getByStatus(QuotationStatus.APPROVED);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getByCustomer()")
    class GetByCustomerTests {

        @Test
        @DisplayName("Returns quotations for customer")
        void getByCustomer_returnsCustomerQuotations() {
            when(quotationRepository
                    .findByCustomerIdOrderByCreatedAtDesc(customerId))
                    .thenReturn(List.of(quotation));

            List<QuotationResponse> result =
                    quotationService.getByCustomer(customerId);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Returns empty list when customer has no quotations")
        void getByCustomer_noQuotations_returnsEmptyList() {
            when(quotationRepository
                    .findByCustomerIdOrderByCreatedAtDesc(customerId))
                    .thenReturn(List.of());

            List<QuotationResponse> result =
                    quotationService.getByCustomer(customerId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("Creates quotation successfully")
        void create_validRequest_createsQuotation() {
            when(customerRepository.findById(customerId))
                    .thenReturn(Optional.of(customer));
            when(costingRepository.findById(costingId))
                    .thenReturn(Optional.of(costingEntity));
            when(quotationRepository.count()).thenReturn(0L);
            when(quotationRepository.existsByQuotationNumber(anyString()))
                    .thenReturn(false);
            when(quotationRepository.save(any()))
                    .thenReturn(quotation);

            QuotationResponse result =
                    quotationService.create(request, userId);

            assertThat(result).isNotNull();
            verify(quotationRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("Throws ResourceNotFoundException when customer not found")
        void create_customerNotFound_throwsNotFound() {
            when(customerRepository.findById(customerId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    quotationService.create(request, userId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Customer");
        }

        @Test
        @DisplayName("Throws ResourceNotFoundException when costing not found")
        void create_costingNotFound_throwsNotFound() {
            when(customerRepository.findById(customerId))
                    .thenReturn(Optional.of(customer));
            when(costingRepository.findById(costingId))
                    .thenReturn(Optional.empty());
            when(quotationRepository.count()).thenReturn(0L);
            when(quotationRepository.existsByQuotationNumber(anyString()))
                    .thenReturn(false);

            assertThatThrownBy(() ->
                    quotationService.create(request, userId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Costing");
        }

        @Test
        @DisplayName("Generates unique quotation number")
        void create_generatesUniqueQuotationNumber() {
            when(customerRepository.findById(customerId))
                    .thenReturn(Optional.of(customer));
            when(costingRepository.findById(costingId))
                    .thenReturn(Optional.of(costingEntity));
            when(quotationRepository.count()).thenReturn(0L);
            when(quotationRepository.existsByQuotationNumber(anyString()))
                    .thenReturn(false);
            when(quotationRepository.save(any()))
                    .thenReturn(quotation);

            quotationService.create(request, userId);

            verify(quotationRepository, atLeastOnce())
                    .existsByQuotationNumber(anyString());
        }

        @Test
        @DisplayName("Increments quotation number when collision exists")
        void create_quotationNumberCollision_incrementsNumber() {
            when(customerRepository.findById(customerId))
                    .thenReturn(Optional.of(customer));
            when(costingRepository.findById(costingId))
                    .thenReturn(Optional.of(costingEntity));
            when(quotationRepository.count()).thenReturn(0L);
            when(quotationRepository.existsByQuotationNumber(anyString()))
                    .thenReturn(true)  // first attempt collides
                    .thenReturn(false); // second attempt succeeds
            when(quotationRepository.save(any()))
                    .thenReturn(quotation);

            quotationService.create(request, userId);

            verify(quotationRepository, times(2))
                    .existsByQuotationNumber(anyString());
        }

        @Test
        @DisplayName("Creates quotation with customer site")
        void create_withCustomerSite_setsCustomerSite() {
            QuotationRequest requestWithSite = new QuotationRequest(
                    customerId,
                    site.getId(),
                    "ONGRID 5KW",
                    30,
                    BigDecimal.ZERO,
                    null, null, null, null,
                    false, null,
                    List.of(new QuotationCostingRequest(
                            costingId, "Main Roof", BigDecimal.ZERO)),
                    List.of(new QuotationInstalmentRequest(
                            1, "Advance", BigDecimal.valueOf(10))),
                    null
            );

            when(customerRepository.findById(customerId))
                    .thenReturn(Optional.of(customer));
            when(costingRepository.findById(costingId))
                    .thenReturn(Optional.of(costingEntity));
            when(quotationRepository.count()).thenReturn(0L);
            when(quotationRepository.existsByQuotationNumber(anyString()))
                    .thenReturn(false);
            when(quotationRepository.save(any()))
                    .thenReturn(quotation);

            QuotationResponse result =
                    quotationService.create(requestWithSite, userId);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("update()")
    class UpdateTests {

        @Test
        @DisplayName("Updates DRAFT quotation successfully")
        void update_draftQuotation_updatesSuccessfully() {
            when(quotationRepository.findById(quotationId))
                    .thenReturn(Optional.of(quotation));
            when(customerRepository.findById(customerId))
                    .thenReturn(Optional.of(customer));
            when(costingRepository.findById(costingId))
                    .thenReturn(Optional.of(costingEntity));
            when(quotationRepository.save(any()))
                    .thenReturn(quotation);

            QuotationResponse result =
                    quotationService.update(quotationId, request);

            assertThat(result).isNotNull();
            verify(quotationRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("Updates REJECTED quotation successfully")
        void update_rejectedQuotation_updatesSuccessfully() {
            quotation.setStatus(QuotationStatus.REJECTED);
            when(quotationRepository.findById(quotationId))
                    .thenReturn(Optional.of(quotation));
            when(customerRepository.findById(customerId))
                    .thenReturn(Optional.of(customer));
            when(costingRepository.findById(costingId))
                    .thenReturn(Optional.of(costingEntity));
            when(quotationRepository.save(any()))
                    .thenReturn(quotation);

            QuotationResponse result =
                    quotationService.update(quotationId, request);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Throws BadRequestException when updating SUBMITTED")
        void update_submittedQuotation_throwsBadRequest() {
            quotation.setStatus(QuotationStatus.SUBMITTED);
            when(quotationRepository.findById(quotationId))
                    .thenReturn(Optional.of(quotation));

            assertThatThrownBy(() ->
                    quotationService.update(quotationId, request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("DRAFT or REJECTED");
        }

        @Test
        @DisplayName("Throws BadRequestException when updating APPROVED")
        void update_approvedQuotation_throwsBadRequest() {
            quotation.setStatus(QuotationStatus.APPROVED);
            when(quotationRepository.findById(quotationId))
                    .thenReturn(Optional.of(quotation));

            assertThatThrownBy(() ->
                    quotationService.update(quotationId, request))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("Throws ResourceNotFoundException when not found")
        void update_nonExistentId_throwsNotFound() {
            when(quotationRepository.findById(quotationId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    quotationService.update(quotationId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
