package com.solarerp.sizing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solarerp.costing.dto.SavedCostingRequest;
import com.solarerp.costing.dto.SavedCostingResponse;
import com.solarerp.costing.entity.SavedCostingEntity;
import com.solarerp.costing.service.CostingService;
import com.solarerp.customer.repository.CustomerRepository;
import com.solarerp.exception.BadRequestException;
import com.solarerp.exception.ForbiddenException;
import com.solarerp.exception.ResourceNotFoundException;
import com.solarerp.material.entity.Material;
import com.solarerp.material.entity.MaterialCategory;
import com.solarerp.material.repository.MaterialRepository;
import com.solarerp.sizing.dto.SizingEstimateResponse;
import com.solarerp.sizing.dto.SizingRequest;
import com.solarerp.sizing.dto.SizingResult;
import com.solarerp.sizing.engine.MaterialCatalog;
import com.solarerp.sizing.engine.SolarSizingEngine;
import com.solarerp.sizing.entity.SizingEstimate;
import com.solarerp.sizing.entity.StateIrradiance;
import com.solarerp.sizing.repository.SizingEstimateRepository;
import com.solarerp.sizing.repository.StateIrradianceRepository;
import com.solarerp.sizing.service.impl.RepositoryMaterialCatalog;
import com.solarerp.sizing.service.impl.SizingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SizingServiceImpl Tests")
class SizingServiceImplTest {

    @Mock private SizingEstimateRepository estimateRepository;
    @Mock private StateIrradianceRepository irradianceRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private SolarSizingEngine engine;
    @Mock private MaterialCatalog materialCatalog;
    @Mock private CostingService costingService;
    @Mock private MaterialRepository materialRepository;

    private SizingServiceImpl sizingService;
    private RepositoryMaterialCatalog repositoryCatalog;

    private UUID userId;
    private UUID customerId;
    private UUID estimateId;

    @BeforeEach
    void setUp() {
        sizingService = new SizingServiceImpl(
                estimateRepository, irradianceRepository, customerRepository,
                engine, materialCatalog, costingService, new ObjectMapper());
        repositoryCatalog = new RepositoryMaterialCatalog(materialRepository);

        userId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        estimateId = UUID.randomUUID();
    }

    // ---- input validation -----------------------------------------------

    @Nested
    @DisplayName("createEstimate() — input validation")
    class InputValidationTests {

        @Test
        @DisplayName("Rejects when both readings and average are supplied")
        void createEstimate_bothInputs_throwsBadRequest() {
            SizingRequest request = new SizingRequest(
                    customerId, null,
                    List.of(BigDecimal.valueOf(300)), BigDecimal.valueOf(300),
                    null, "Maharashtra",
                    new BigDecimal("100"), new BigDecimal("550"),
                    "RESIDENTIAL", "SINGLE");

            assertThatThrownBy(() -> sizingService.createEstimate(request, userId))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("Rejects when neither readings nor average is supplied")
        void createEstimate_noInput_throwsBadRequest() {
            SizingRequest request = new SizingRequest(
                    customerId, null,
                    null, null,
                    null, "Maharashtra",
                    new BigDecimal("100"), new BigDecimal("550"),
                    "RESIDENTIAL", "SINGLE");

            assertThatThrownBy(() -> sizingService.createEstimate(request, userId))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("Rejects when customer does not exist")
        void createEstimate_unknownCustomer_throwsNotFound() {
            when(customerRepository.existsById(customerId)).thenReturn(false);

            assertThatThrownBy(
                    () -> sizingService.createEstimate(averageRequest(), userId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Customer");
        }
    }

    // ---- createEstimate — happy paths ------------------------------------

    @Nested
    @DisplayName("createEstimate() — happy path")
    class CreateEstimateTests {

        @Test
        @DisplayName("Uses state PSH from DB when state is found")
        void createEstimate_stateFound_usesDbPsh() {
            StateIrradiance irr = new StateIrradiance();
            irr.setState("Maharashtra");
            irr.setPeakSunHours(new BigDecimal("5.10"));

            when(customerRepository.existsById(customerId)).thenReturn(true);
            when(irradianceRepository.findById("Maharashtra"))
                    .thenReturn(Optional.of(irr));
            when(engine.calculate(eq(averageRequest()),
                    eq(new BigDecimal("5.10")), eq(materialCatalog)))
                    .thenReturn(sizingResult());
            when(estimateRepository.save(any())).thenReturn(savedEstimate());

            SizingEstimateResponse response =
                    sizingService.createEstimate(averageRequest(), userId);

            assertThat(response).isNotNull();
            assertThat(response.recommendedCapacityKw())
                    .isEqualByComparingTo(new BigDecimal("5.00"));
        }

        @Test
        @DisplayName("Falls back to default PSH when state not in table")
        void createEstimate_stateNotFound_usesDefaultPsh() {
            when(customerRepository.existsById(customerId)).thenReturn(true);
            when(irradianceRepository.findById("Maharashtra"))
                    .thenReturn(Optional.empty());
            when(engine.calculate(any(), any(), any())).thenReturn(sizingResult());
            when(estimateRepository.save(any())).thenReturn(savedEstimate());

            sizingService.createEstimate(averageRequest(), userId);

            verify(engine).calculate(any(), eq(new BigDecimal("4.80")), any());
        }

        @Test
        @DisplayName("Accepts request with consumption readings list")
        void createEstimate_withReadings_succeeds() {
            SizingRequest request = new SizingRequest(
                    customerId, null,
                    List.of(BigDecimal.valueOf(300), BigDecimal.valueOf(350)),
                    null, null, "Maharashtra",
                    new BigDecimal("100"), new BigDecimal("550"),
                    "RESIDENTIAL", "SINGLE");

            when(customerRepository.existsById(customerId)).thenReturn(true);
            when(irradianceRepository.findById("Maharashtra"))
                    .thenReturn(Optional.empty());
            when(engine.calculate(any(), any(), any())).thenReturn(sizingResult());
            when(estimateRepository.save(any())).thenReturn(savedEstimate());

            SizingEstimateResponse response =
                    sizingService.createEstimate(request, userId);

            assertThat(response).isNotNull();
        }
    }

    // ---- getById ---------------------------------------------------------

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("Throws ResourceNotFoundException when estimate missing")
        void getById_notFound_throws() {
            when(estimateRepository.findById(estimateId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sizingService.getById(estimateId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Sizing estimate");
        }

        @Test
        @DisplayName("Returns response for found estimate")
        void getById_found_returnsResponse() {
            when(estimateRepository.findById(estimateId))
                    .thenReturn(Optional.of(savedEstimate()));

            SizingEstimateResponse response = sizingService.getById(estimateId);

            assertThat(response.customerId()).isEqualTo(customerId);
        }
    }

    // ---- getAll / getByCustomer ------------------------------------------

    @Nested
    @DisplayName("getAll() and getByCustomer()")
    class ListTests {

        @Test
        @DisplayName("getAll() returns all estimates as responses")
        void getAll_returnsAll() {
            when(estimateRepository.findAllByOrderByCreatedAtDesc())
                    .thenReturn(List.of(savedEstimate()));

            assertThat(sizingService.getAll()).hasSize(1);
        }

        @Test
        @DisplayName("getByCustomer() returns estimates for that customer")
        void getByCustomer_returnsFiltered() {
            when(estimateRepository.findByCustomerIdOrderByCreatedAtDesc(customerId))
                    .thenReturn(List.of(savedEstimate()));

            assertThat(sizingService.getByCustomer(customerId)).hasSize(1);
        }
    }

    // ---- delete ----------------------------------------------------------

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("Throws ForbiddenException when non-owner attempts delete")
        void delete_nonOwner_throwsForbidden() {
            SizingEstimate estimate = savedEstimate();
            estimate.setCreatedBy(UUID.randomUUID());
            when(estimateRepository.findById(estimateId))
                    .thenReturn(Optional.of(estimate));

            assertThatThrownBy(() -> sizingService.delete(estimateId, userId))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("Deletes successfully when owner requests it")
        void delete_owner_succeeds() {
            SizingEstimate estimate = savedEstimate();
            when(estimateRepository.findById(estimateId))
                    .thenReturn(Optional.of(estimate));

            assertThatNoException().isThrownBy(
                    () -> sizingService.delete(estimateId, userId));
            verify(estimateRepository).delete(estimate);
        }
    }

    // ---- convertToCosting ------------------------------------------------

    @Nested
    @DisplayName("convertToCosting()")
    class ConvertToCostingTests {

        @Test
        @DisplayName("Throws BadRequestException when estimate already has costing")
        void convertToCosting_alreadyLinked_throwsBadRequest() {
            SizingEstimate estimate = savedEstimate();
            SavedCostingEntity existingCosting = new SavedCostingEntity();
            existingCosting.setId(UUID.randomUUID());
            estimate.setCosting(existingCosting);

            when(estimateRepository.findById(estimateId))
                    .thenReturn(Optional.of(estimate));

            assertThatThrownBy(
                    () -> sizingService.convertToCosting(estimateId, userId))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already has");
        }

        @Test
        @DisplayName("Creates costing and links it when estimate has no costing")
        void convertToCosting_notLinked_createsAndLinks() {
            UUID costingId = UUID.randomUUID();
            SavedCostingResponse costingResponse =
                    new SavedCostingResponse(costingId, Instant.now(),
                            userId, null, null);

            when(estimateRepository.findById(estimateId))
                    .thenReturn(Optional.of(savedEstimate()));
            when(costingService.create(any(SavedCostingRequest.class), eq(userId)))
                    .thenReturn(costingResponse);

            UUID result = sizingService.convertToCosting(estimateId, userId);

            assertThat(result).isEqualTo(costingId);
            verify(estimateRepository).linkCosting(estimateId, costingId);
        }
    }

    // ---- RepositoryMaterialCatalog ---------------------------------------

    @Nested
    @DisplayName("RepositoryMaterialCatalog")
    class CatalogTests {

        @Test
        @DisplayName("Returns empty when no materials in category")
        void preferredFor_emptyList_returnsEmpty() {
            when(materialRepository.findByCategoryAndActiveTrueOrderByBrandNameAsc(
                    MaterialCategory.PANEL))
                    .thenReturn(List.of());

            assertThat(repositoryCatalog.preferredFor(MaterialCategory.PANEL))
                    .isEmpty();
        }

        @Test
        @DisplayName("Returns cheapest priced material from category")
        void preferredFor_hasPricedMaterials_returnsCheapest() {
            Material expensive = makeMaterial(MaterialCategory.PANEL,
                    new BigDecimal("15000"));
            Material cheap = makeMaterial(MaterialCategory.PANEL,
                    new BigDecimal("10000"));

            when(materialRepository.findByCategoryAndActiveTrueOrderByBrandNameAsc(
                    MaterialCategory.PANEL))
                    .thenReturn(List.of(expensive, cheap));

            Optional<Material> result =
                    repositoryCatalog.preferredFor(MaterialCategory.PANEL);

            assertThat(result).isPresent();
            assertThat(result.get().getUnitPrice())
                    .isEqualByComparingTo(new BigDecimal("10000"));
        }

        @Test
        @DisplayName("Falls back to first material when none are priced")
        void preferredFor_noPricedMaterials_returnsFirst() {
            Material unpriced = makeMaterial(MaterialCategory.PANEL, null);

            when(materialRepository.findByCategoryAndActiveTrueOrderByBrandNameAsc(
                    MaterialCategory.PANEL))
                    .thenReturn(List.of(unpriced));

            assertThat(repositoryCatalog.preferredFor(MaterialCategory.PANEL))
                    .contains(unpriced);
        }

        @Test
        @DisplayName("preferredFor(category, componentKey) matches by key first")
        void preferredForKey_keyMatch_usesKeyResult() {
            Material byKey = makeMaterial(MaterialCategory.CABLE,
                    new BigDecimal("45"));

            when(materialRepository.findByComponentKeyAndActiveTrueOrderByBrandNameAsc(
                    "dcCable"))
                    .thenReturn(List.of(byKey));

            assertThat(repositoryCatalog.preferredFor(
                    MaterialCategory.CABLE, "dcCable"))
                    .contains(byKey);
        }

        @Test
        @DisplayName("Falls back to category when no key match")
        void preferredForKey_noKeyMatch_fallsBackToCategory() {
            Material byCategory = makeMaterial(MaterialCategory.CABLE,
                    new BigDecimal("45"));

            when(materialRepository.findByComponentKeyAndActiveTrueOrderByBrandNameAsc(
                    "dcCable"))
                    .thenReturn(List.of());
            when(materialRepository.findByCategoryAndActiveTrueOrderByBrandNameAsc(
                    MaterialCategory.CABLE))
                    .thenReturn(List.of(byCategory));

            assertThat(repositoryCatalog.preferredFor(
                    MaterialCategory.CABLE, "dcCable"))
                    .contains(byCategory);
        }

        @Test
        @DisplayName("Null componentKey skips key lookup and falls back to category")
        void preferredForKey_nullKey_fallsBackToCategory() {
            Material byCategory = makeMaterial(MaterialCategory.CABLE,
                    new BigDecimal("45"));

            when(materialRepository.findByCategoryAndActiveTrueOrderByBrandNameAsc(
                    MaterialCategory.CABLE))
                    .thenReturn(List.of(byCategory));

            assertThat(repositoryCatalog.preferredFor(
                    MaterialCategory.CABLE, null))
                    .contains(byCategory);
        }
    }

    // ---- helpers ---------------------------------------------------------

    private SizingRequest averageRequest() {
        return new SizingRequest(
                customerId, null,
                null, new BigDecimal("300"),
                null, "Maharashtra",
                new BigDecimal("100"), new BigDecimal("550"),
                "RESIDENTIAL", "SINGLE");
    }

    private SizingResult sizingResult() {
        return new SizingResult(
                new BigDecimal("300.00"),
                new BigDecimal("10"),
                new BigDecimal("330.00"),
                new BigDecimal("5.00"),
                new BigDecimal("24.75"),
                new BigDecimal("5.00"),
                false,
                10,
                new BigDecimal("4.35"),
                new BigDecimal("7300.00"),
                new BigDecimal("250000.00"),
                List.of(),
                false,
                false
        );
    }

    private SizingEstimate savedEstimate() {
        SizingEstimate e = new SizingEstimate();
        e.setId(estimateId);
        e.setCustomerId(customerId);
        e.setMonthlyAverageKwh(new BigDecimal("300.00"));
        e.setGrowthBufferPercent(new BigDecimal("10"));
        e.setTargetMonthlyConsumptionKwh(new BigDecimal("330.00"));
        e.setState("Maharashtra");
        e.setAvailableRoofAreaSqm(new BigDecimal("100"));
        e.setPanelWattageWp(new BigDecimal("550"));
        e.setConnectionType("RESIDENTIAL");
        e.setPhaseType("SINGLE");
        e.setRecommendedCapacityKw(new BigDecimal("5.00"));
        e.setRoofConstrained(false);
        e.setPanelCount(10);
        e.setInverterCapacityKw(new BigDecimal("4.35"));
        e.setAnnualGenerationKwh(new BigDecimal("7300.00"));
        e.setIndicativeMaterialCost(new BigDecimal("250000.00"));
        e.setBom("[]");
        e.setCreatedBy(userId);
        return e;
    }

    private Material makeMaterial(MaterialCategory category, BigDecimal price) {
        Material m = new Material();
        m.setId(UUID.randomUUID());
        m.setCategory(category);
        m.setBrandName("TestBrand");
        m.setModelName("TestModel");
        m.setUnitPrice(price);
        return m;
    }
}
