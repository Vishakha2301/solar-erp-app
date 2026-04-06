package com.solarerp.quotation.repository;

import com.solarerp.quotation.entity.Quotation;
import com.solarerp.quotation.entity.QuotationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, UUID> {

    List<Quotation> findAllByOrderByCreatedAtDesc();

    List<Quotation> findByCreatedByOrderByCreatedAtDesc(UUID createdBy);

    List<Quotation> findByStatusOrderByCreatedAtDesc(QuotationStatus status);

    List<Quotation> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    boolean existsByQuotationNumber(String quotationNumber);
}