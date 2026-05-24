package com.solarerp.sizing.repository;

import com.solarerp.sizing.entity.SizingEstimate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SizingEstimateRepository
        extends JpaRepository<SizingEstimate, UUID> {

    List<SizingEstimate> findAllByOrderByCreatedAtDesc();

    List<SizingEstimate> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    /**
     * Sets the costing FK on an estimate. Used after a SavedCosting is created
     * from the estimate -- CostingService returns a DTO rather than the
     * managed entity, so the relation is set by id.
     */
    @Modifying
    @Query("UPDATE SizingEstimate e SET e.costing.id = :costingId "
            + "WHERE e.id = :estimateId")
    void linkCosting(@Param("estimateId") UUID estimateId,
                     @Param("costingId") UUID costingId);
}
