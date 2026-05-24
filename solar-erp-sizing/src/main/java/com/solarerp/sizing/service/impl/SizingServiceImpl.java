package com.solarerp.sizing.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solarerp.costing.dto.CostingContextDto;
import com.solarerp.costing.dto.CostingSnapshotDto;
import com.solarerp.costing.dto.SavedCostingRequest;
import com.solarerp.costing.dto.SavedCostingResponse;
import com.solarerp.costing.service.CostingService;
import com.solarerp.customer.repository.CustomerRepository;
import com.solarerp.exception.BadRequestException;
import com.solarerp.exception.ForbiddenException;
import com.solarerp.exception.ResourceNotFoundException;
import com.solarerp.sizing.dto.BomLineItem;
import com.solarerp.sizing.dto.SizingEstimateResponse;
import com.solarerp.sizing.dto.SizingRequest;
import com.solarerp.sizing.dto.SizingResult;
import com.solarerp.sizing.engine.MaterialCatalog;
import com.solarerp.sizing.engine.SizingParameters;
import com.solarerp.sizing.engine.SolarSizingEngine;
import com.solarerp.sizing.entity.SizingEstimate;
import com.solarerp.sizing.entity.StateIrradiance;
import com.solarerp.sizing.repository.SizingEstimateRepository;
import com.solarerp.sizing.repository.StateIrradianceRepository;
import com.solarerp.sizing.service.SizingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SizingServiceImpl implements SizingService {

    private final SizingEstimateRepository estimateRepository;
    private final StateIrradianceRepository irradianceRepository;
    private final CustomerRepository customerRepository;
    private final SolarSizingEngine engine;
    private final MaterialCatalog materialCatalog;
    private final CostingService costingService;
    private final ObjectMapper objectMapper;

    public SizingServiceImpl(SizingEstimateRepository estimateRepository,
                             StateIrradianceRepository irradianceRepository,
                             CustomerRepository customerRepository,
                             SolarSizingEngine engine,
                             MaterialCatalog materialCatalog,
                             CostingService costingService,
                             ObjectMapper objectMapper) {
        this.estimateRepository = estimateRepository;
        this.irradianceRepository = irradianceRepository;
        this.customerRepository = customerRepository;
        this.engine = engine;
        this.materialCatalog = materialCatalog;
        this.costingService = costingService;
        this.objectMapper = objectMapper;
    }

    @Override
    public SizingEstimateResponse createEstimate(SizingRequest request,
                                                 UUID userId) {
        validateInputs(request);

        if (!customerRepository.existsById(request.customerId())) {
            throw new ResourceNotFoundException(
                    "Customer", request.customerId());
        }

        BigDecimal psh = irradianceRepository.findById(request.state())
                .map(StateIrradiance::getPeakSunHours)
                .orElse(SizingParameters.DEFAULT_PEAK_SUN_HOURS);

        SizingResult result = engine.calculate(request, psh, materialCatalog);

        SizingEstimate entity = toEntity(request, result, userId);
        return toResponse(estimateRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public SizingEstimateResponse getById(UUID id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SizingEstimateResponse> getAll() {
        return estimateRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SizingEstimateResponse> getByCustomer(UUID customerId) {
        return estimateRepository
                .findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void delete(UUID id, UUID userId) {
        SizingEstimate entity = findOrThrow(id);
        checkOwnership(entity, userId);
        estimateRepository.delete(entity);
    }

    @Override
    public UUID convertToCosting(UUID estimateId, UUID userId) {
        SizingEstimate estimate = findOrThrow(estimateId);

        if (estimate.getCosting() != null) {
            throw new BadRequestException(
                    "This estimate already has a linked costing");
        }

        CostingContextDto context = new CostingContextDto(
                estimate.getRecommendedCapacityKw().doubleValue(),
                "Rooftop",
                "THREE".equalsIgnoreCase(estimate.getPhaseType())
                        ? "3PH" : "1PH",
                "RCC",
                "Sizing " + shortId(estimate.getId()),
                false
        );

        // Carry the indicative ex-GST material cost as systemSubTotal.
        // All remaining components (installation, transport, civil, net metering,
        // etc.), additional percentage costs, and GST are handled in the costing
        // module — staff complete those fields in the costing form.
        double materialCost = estimate.getIndicativeMaterialCost().doubleValue();
        CostingSnapshotDto snapshot = new CostingSnapshotDto(
                materialCost,  // systemSubTotal (ex-GST materials from sizing)
                0.0,           // subsidyProcessingFee
                0.0,           // contingency
                0.0,           // cp1
                0.0,           // cp2
                0.0,           // amc
                materialCost,  // grandTotal (staff will add all remaining costs)
                materialCost,  // projectCostAfterGst (placeholder; costing form recalculates)
                estimate.getRecommendedCapacityKw().signum() > 0
                        ? materialCost / (estimate.getRecommendedCapacityKw()
                                .doubleValue() * 1000)
                        : 0.0  // perWpAfterGst
        );

        SavedCostingResponse costing = costingService.create(
                new SavedCostingRequest(context, snapshot), userId);

        // CostingService returns a DTO, not the managed entity, so attach the
        // relation by id with a modifying query.
        estimateRepository.linkCosting(estimate.getId(), costing.id());

        return costing.id();
    }

    // ---- helpers --------------------------------------------------------

    /**
     * Exactly one consumption input must be present: either a non-empty list
     * of readings, or a known monthly average.
     */
    private void validateInputs(SizingRequest request) {
        boolean hasReadings = request.monthlyConsumptionReadingsKwh() != null
                && !request.monthlyConsumptionReadingsKwh().isEmpty();
        boolean hasAverage = request.knownMonthlyAverageKwh() != null;

        if (hasReadings == hasAverage) {
            throw new BadRequestException(
                    "Provide exactly one of monthlyConsumptionReadingsKwh "
                            + "(non-empty) or knownMonthlyAverageKwh");
        }
    }

    private SizingEstimate findOrThrow(UUID id) {
        return estimateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sizing estimate", id));
    }

    private void checkOwnership(SizingEstimate entity, UUID userId) {
        if (!entity.getCreatedBy().equals(userId)) {
            throw new ForbiddenException(
                    "You do not have permission to modify this estimate");
        }
    }

    private SizingEstimate toEntity(SizingRequest request,
                                    SizingResult result,
                                    UUID userId) {
        SizingEstimate e = new SizingEstimate();
        e.setCustomerId(request.customerId());
        e.setCustomerSiteId(request.customerSiteId());
        e.setMonthlyAverageKwh(result.monthlyAverageKwh());
        e.setGrowthBufferPercent(result.growthBufferPercentApplied());
        e.setState(request.state());
        e.setAvailableRoofAreaSqm(request.availableRoofAreaSqm());
        e.setPanelWattageWp(request.panelWattageWp());
        e.setConnectionType(request.connectionType());
        e.setPhaseType(request.phaseType());

        e.setTargetMonthlyConsumptionKwh(
                result.targetMonthlyConsumptionKwh());
        e.setRecommendedCapacityKw(result.recommendedCapacityKw());
        e.setRoofConstrained(result.roofConstrained());
        e.setPanelCount(result.panelCount());
        e.setInverterCapacityKw(result.inverterCapacityKw());
        e.setAnnualGenerationKwh(result.annualGenerationKwh());
        e.setIndicativeMaterialCost(result.indicativeMaterialCost());
        e.setBom(writeBom(result.billOfMaterials()));
        e.setCreatedBy(userId);
        return e;
    }

    private SizingEstimateResponse toResponse(SizingEstimate e) {
        List<BomLineItem> bom = readBom(e.getBom());
        boolean fullyPriced = !bom.isEmpty()
                && bom.stream().allMatch(BomLineItem::priced);
        boolean exceedsTypical = e.getRecommendedCapacityKw().compareTo(
                SizingParameters.TYPICAL_HOUSEHOLD_KW.multiply(
                        SizingParameters.TYPICAL_HOUSEHOLD_FLAG_MULTIPLE)) > 0;

        return new SizingEstimateResponse(
                e.getId(),
                e.getCustomerId(),
                e.getCustomerSiteId(),
                e.getMonthlyAverageKwh(),
                e.getGrowthBufferPercent(),
                e.getTargetMonthlyConsumptionKwh(),
                e.getState(),
                e.getAvailableRoofAreaSqm(),
                e.getPanelWattageWp(),
                e.getConnectionType(),
                e.getPhaseType(),
                e.getRecommendedCapacityKw(),
                e.isRoofConstrained(),
                e.getPanelCount(),
                e.getInverterCapacityKw(),
                e.getAnnualGenerationKwh(),
                e.getIndicativeMaterialCost(),
                bom,
                fullyPriced,
                exceedsTypical,
                e.getCosting() != null ? e.getCosting().getId() : null,
                e.getCreatedAt(),
                e.getCreatedBy()
        );
    }

    private String writeBom(List<BomLineItem> bom) {
        try {
            return objectMapper.writeValueAsString(bom);
        } catch (Exception ex) {
            throw new BadRequestException("Failed to serialise BOM");
        }
    }

    private List<BomLineItem> readBom(String json) {
        try {
            return objectMapper.readValue(
                    json, new TypeReference<List<BomLineItem>>() {});
        } catch (Exception ex) {
            throw new BadRequestException(
                    "Corrupt stored BOM: " + ex.getMessage());
        }
    }

    private String shortId(UUID id) {
        return id.toString().substring(0, 8);
    }
}
