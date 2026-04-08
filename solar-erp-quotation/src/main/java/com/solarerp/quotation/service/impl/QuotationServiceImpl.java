package com.solarerp.quotation.service.impl;

import com.solarerp.costing.repository.CostingRepository;
import com.solarerp.customer.dto.CustomerResponse;
import com.solarerp.customer.dto.CustomerSiteResponse;
import com.solarerp.customer.entity.Customer;
import com.solarerp.customer.entity.CustomerSite;
import com.solarerp.customer.repository.CustomerRepository;
import com.solarerp.material.dto.MaterialResponse;
import com.solarerp.material.entity.Material;
import com.solarerp.material.repository.MaterialRepository;
import com.solarerp.quotation.dto.*;
import com.solarerp.quotation.entity.*;
import com.solarerp.quotation.repository.QuotationRepository;
import com.solarerp.quotation.service.QuotationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class QuotationServiceImpl implements QuotationService {

    private final QuotationRepository quotationRepository;
    private final CustomerRepository customerRepository;
    private final CostingRepository costingRepository;
    private final MaterialRepository materialRepository;

    public QuotationServiceImpl(
            QuotationRepository quotationRepository,
            CustomerRepository customerRepository,
            CostingRepository costingRepository,
            MaterialRepository materialRepository) {
        this.quotationRepository = quotationRepository;
        this.customerRepository = customerRepository;
        this.costingRepository = costingRepository;
        this.materialRepository = materialRepository;
    }

    @Override
    public List<QuotationResponse> getAll() {
        return quotationRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public QuotationResponse getById(UUID id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public List<QuotationResponse> getByStatus(QuotationStatus status) {
        return quotationRepository.findByStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<QuotationResponse> getByCustomer(UUID customerId) {
        return quotationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public QuotationResponse create(QuotationRequest request, UUID userId) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Customer not found"));

        Quotation quotation = new Quotation();
        quotation.setQuotationNumber(generateQuotationNumber());
        quotation.setCustomer(customer);
        quotation.setCreatedBy(userId);

        if (request.customerSiteId() != null) {
            CustomerSite site = customer.getSites().stream()
                    .filter(s -> s.getId().equals(request.customerSiteId()))
                    .findFirst()
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Customer site not found"));
            quotation.setCustomerSite(site);
        }

        mapRequestToEntity(request, quotation);
        addCostings(request, quotation);
        addInstalments(request, quotation);
        addPackages(request, quotation);

        return toResponse(quotationRepository.save(quotation));
    }

    @Override
    public QuotationResponse update(UUID id, QuotationRequest request) {
        Quotation quotation = findOrThrow(id);

        if (quotation.getStatus() != QuotationStatus.DRAFT &&
                quotation.getStatus() != QuotationStatus.REJECTED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only DRAFT or REJECTED quotations can be edited");
        }

        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Customer not found"));

        quotation.setCustomer(customer);
        mapRequestToEntity(request, quotation);

        quotation.getCostings().clear();
        quotation.getInstalments().clear();
        quotation.getPackages().clear();

        addCostings(request, quotation);
        addInstalments(request, quotation);
        addPackages(request, quotation);

        return toResponse(quotationRepository.save(quotation));
    }

    @Override
    public QuotationResponse submit(UUID id, UUID userId) {
        Quotation quotation = findOrThrow(id);

        if (quotation.getStatus() != QuotationStatus.DRAFT &&
                quotation.getStatus() != QuotationStatus.REJECTED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Only DRAFT or REJECTED quotations can be submitted");
        }

        if (!quotation.getCreatedBy().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only the creator can submit this quotation");
        }

        // If previously rejected, mark as REVISED instead of SUBMITTED
        if (quotation.getStatus() == QuotationStatus.REJECTED) {
            quotation.setStatus(QuotationStatus.REVISED);
        } else {
            quotation.setStatus(QuotationStatus.SUBMITTED);
        }
        quotation.setSubmittedAt(Instant.now());
        quotation.setRejectionReason(null);

        return toResponse(quotationRepository.save(quotation));
    }

    @Override
    public QuotationResponse approve(UUID id, String approvalNotes, UUID userId) {
        Quotation quotation = findOrThrow(id);

        if (quotation.getStatus() != QuotationStatus.SUBMITTED &&
                quotation.getStatus() != QuotationStatus.REVISED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only SUBMITTED or REVISED quotations can be approved");
        }

        quotation.setStatus(QuotationStatus.APPROVED);
        quotation.setApprovalNotes(approvalNotes);
        quotation.setApprovedRejectedBy(userId);
        quotation.setApprovedRejectedAt(Instant.now());

        return toResponse(quotationRepository.save(quotation));
    }

    @Override
    public QuotationResponse reject(UUID id, String rejectionReason, UUID userId) {
        Quotation quotation = findOrThrow(id);

        if (quotation.getStatus() != QuotationStatus.SUBMITTED &&
                quotation.getStatus() != QuotationStatus.REVISED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only SUBMITTED or REVISED quotations can be rejected");
        }

        if (rejectionReason == null || rejectionReason.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Rejection reason is required");
        }

        quotation.setStatus(QuotationStatus.REJECTED);
        quotation.setRejectionReason(rejectionReason);
        quotation.setApprovedRejectedBy(userId);
        quotation.setApprovedRejectedAt(Instant.now());

        return toResponse(quotationRepository.save(quotation));
    }

    @Override
    public void delete(UUID id) {
        Quotation quotation = findOrThrow(id);

        if (quotation.getStatus() != QuotationStatus.DRAFT) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Only DRAFT quotations can be deleted");
        }

        quotationRepository.delete(quotation);
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private Quotation findOrThrow(UUID id) {
        return quotationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Quotation not found: " + id));
    }

    private String generateQuotationNumber() {
        int year = Year.now().getValue();
        long count = quotationRepository.count() + 1;
        String number = String.format("QT-%d-%03d", year, count);
        while (quotationRepository.existsByQuotationNumber(number)) {
            count++;
            number = String.format("QT-%d-%03d", year, count);
        }
        return number;
    }

    private void mapRequestToEntity(QuotationRequest request, Quotation quotation) {
        quotation.setSystemType(request.systemType());
        quotation.setValidityDays(request.validityDays() > 0 ? request.validityDays() : 30);
        quotation.setDiscount(request.discount());
        quotation.setScopeOfWork(request.scopeOfWork());
        quotation.setPaymentTerms(request.paymentTerms());
        quotation.setTermsAndConditions(request.termsAndConditions());
        quotation.setNotes(request.notes());
        quotation.setFinancingAvailable(request.financingAvailable());
        quotation.setFinancingRate(request.financingRate());
    }

    private void addCostings(QuotationRequest request, Quotation quotation) {
        if (request.costings() == null) return;
        request.costings().forEach(costingReq -> {
            QuotationCosting qc = new QuotationCosting();
            qc.setQuotation(quotation);
            qc.setCosting(costingRepository.findById(costingReq.costingId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Costing not found: " + costingReq.costingId())));
            qc.setRoofLabel(costingReq.roofLabel());
            qc.setSubsidyAmount(costingReq.subsidyAmount() != null
                    ? costingReq.subsidyAmount()
                    : BigDecimal.ZERO);
            quotation.getCostings().add(qc);
        });
    }

    private void addInstalments(QuotationRequest request, Quotation quotation) {
        if (request.instalments() == null) return;
        request.instalments().forEach(instReq -> {
            QuotationInstalment inst = new QuotationInstalment();
            inst.setQuotation(quotation);
            inst.setInstalmentNo(instReq.instalmentNo());
            inst.setDescription(instReq.description());
            inst.setPercentage(instReq.percentage());
            quotation.getInstalments().add(inst);
        });
    }

    private void addPackages(QuotationRequest request, Quotation quotation) {
        if (request.packages() == null) return;
        request.packages().forEach(pkgReq -> {
            QuotationPackage pkg = new QuotationPackage();
            pkg.setQuotation(quotation);
            pkg.setPackageName(pkgReq.packageName());
            pkg.setRecommended(pkgReq.isRecommended());
            pkgReq.materials().forEach(matReq -> {
                Material material = materialRepository.findById(matReq.materialId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Material not found: " + matReq.materialId()));
                QuotationPackageMaterial qpm = new QuotationPackageMaterial();
                qpm.setQuotationPackage(pkg);
                qpm.setMaterial(material);
                qpm.setComponentKey(matReq.componentKey());
                qpm.setRecommended(matReq.isRecommended());
                pkg.getMaterials().add(qpm);
            });
            quotation.getPackages().add(pkg);
        });
    }

    private QuotationResponse toResponse(Quotation quotation) {
        return new QuotationResponse(
                quotation.getId(),
                quotation.getQuotationNumber(),
                toCustomerResponse(quotation.getCustomer()),
                quotation.getCustomerSite() != null
                        ? toCustomerSiteResponse(quotation.getCustomerSite()) : null,
                quotation.getStatus(),
                quotation.getSystemType(),
                quotation.getValidityDays(),
                quotation.getDiscount(),
                quotation.getScopeOfWork(),
                quotation.getPaymentTerms(),
                quotation.getTermsAndConditions(),
                quotation.getNotes(),
                quotation.isFinancingAvailable(),
                quotation.getFinancingRate(),
                quotation.getRejectionReason(),
                quotation.getApprovalNotes(),
                quotation.getCreatedBy(),
                quotation.getSubmittedAt(),
                quotation.getApprovedRejectedBy(),
                quotation.getApprovedRejectedAt(),
                quotation.getCreatedAt(),
                quotation.getUpdatedAt(),
                quotation.getCostings().stream()
                        .map(qc -> new QuotationCostingResponse(
                                qc.getId(),
                                qc.getCosting().getId(),
                                qc.getRoofLabel(),
                                qc.getSubsidyAmount()))
                        .toList(),
                quotation.getInstalments().stream()
                        .map(inst -> new QuotationInstalmentResponse(
                                inst.getId(),
                                inst.getInstalmentNo(),
                                inst.getDescription(),
                                inst.getPercentage()))
                        .toList(),
                quotation.getPackages().stream()
                        .map(pkg -> new QuotationPackageResponse(
                                pkg.getId(),
                                pkg.getPackageName(),
                                pkg.isRecommended(),
                                pkg.getMaterials().stream()
                                        .map(qpm -> new QuotationPackageMaterialResponse(
                                                qpm.getId(),
                                                toMaterialResponse(qpm.getMaterial()),
                                                qpm.getComponentKey(),
                                                qpm.isRecommended()))
                                        .toList()))
                        .toList()
        );
    }

    private CustomerResponse toCustomerResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getCustomerType(),
                customer.getName(),
                customer.getCompanyName(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getAddress(),
                customer.getCity(),
                customer.getState(),
                customer.getPincode(),
                customer.getGstNumber(),
                customer.isActive(),
                customer.getCreatedAt(),
                customer.getCreatedBy(),
                customer.getSites().stream()
                        .map(this::toCustomerSiteResponse)
                        .toList()
        );
    }

    private CustomerSiteResponse toCustomerSiteResponse(CustomerSite site) {
        return new CustomerSiteResponse(
                site.getId(),
                site.getSiteLabel(),
                site.getAddress(),
                site.getCity(),
                site.getState(),
                site.getPincode(),
                site.isDefault(),
                site.getCreatedAt()
        );
    }

    private MaterialResponse toMaterialResponse(Material material) {
        return new MaterialResponse(
                material.getId(),
                new com.solarerp.material.dto.MaterialCategoryResponse(
                        material.getCategory().name(),
                        material.getCategory().name()
                ),
                material.getComponentKey(),
                material.getBrandName(),
                material.getModelName(),
                material.getSpecification(),
                material.getUnit(),
                material.getWarranty(),
                material.getHsnCode(),
                material.isActive(),
                material.getCreatedAt(),
                material.getCreatedBy()
        );
    }
}