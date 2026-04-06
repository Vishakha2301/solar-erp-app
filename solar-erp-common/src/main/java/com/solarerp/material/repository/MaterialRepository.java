package com.solarerp.material.repository;

import com.solarerp.material.entity.Material;
import com.solarerp.material.entity.MaterialCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MaterialRepository extends JpaRepository<Material, UUID> {

    List<Material> findAllByActiveTrueOrderByBrandNameAsc();

    List<Material> findByCategoryAndActiveTrueOrderByBrandNameAsc(MaterialCategory category);

    List<Material> findByComponentKeyAndActiveTrueOrderByBrandNameAsc(String componentKey);

    List<Material> findByBrandNameContainingIgnoreCaseAndActiveTrue(String brandName);
}
