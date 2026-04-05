package com.solarerp.costing.repository;

import com.solarerp.costing.entity.SavedCostingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CostingRepository extends JpaRepository<SavedCostingEntity, UUID> {

    // All costings visible to everyone in the org, newest first
    List<SavedCostingEntity> findAllByOrderByCreatedAtDesc();

    // All costings created by a specific user
    List<SavedCostingEntity> findByCreatedByOrderByCreatedAtDesc(UUID createdBy);
}