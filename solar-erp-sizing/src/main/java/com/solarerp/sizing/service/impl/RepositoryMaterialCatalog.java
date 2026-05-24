package com.solarerp.sizing.service.impl;

import com.solarerp.material.entity.Material;
import com.solarerp.material.entity.MaterialCategory;
import com.solarerp.material.repository.MaterialRepository;
import com.solarerp.sizing.engine.MaterialCatalog;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Production MaterialCatalog backed by MaterialRepository.
 *
 * "Preferred" material = the cheapest active priced one in a category. If none
 * are priced, falls back to the first active material so the BOM still lists
 * the component (as an unpriced line the costing form can complete).
 */
@Component
public class RepositoryMaterialCatalog implements MaterialCatalog {

    private final MaterialRepository materialRepository;

    public RepositoryMaterialCatalog(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    @Override
    public Optional<Material> preferredFor(MaterialCategory category) {
        return pickPreferred(materialRepository
                .findByCategoryAndActiveTrueOrderByBrandNameAsc(category));
    }

    @Override
    public Optional<Material> preferredFor(MaterialCategory category,
                                           String componentKey) {
        if (componentKey != null && !componentKey.isBlank()) {
            List<Material> byKey = materialRepository
                    .findByComponentKeyAndActiveTrueOrderByBrandNameAsc(
                            componentKey);
            Optional<Material> keyMatch = pickPreferred(byKey);
            if (keyMatch.isPresent()) {
                return keyMatch;
            }
        }
        return preferredFor(category);
    }

    /**
     * From a list of active materials, returns the cheapest priced one, or the
     * first material if none have a price, or empty if the list is empty.
     */
    private Optional<Material> pickPreferred(List<Material> materials) {
        if (materials == null || materials.isEmpty()) {
            return Optional.empty();
        }
        Optional<Material> cheapestPriced = materials.stream()
                .filter(m -> m.getUnitPrice() != null
                        && m.getUnitPrice().signum() > 0)
                .min(Comparator.comparing(Material::getUnitPrice));

        return cheapestPriced.isPresent()
                ? cheapestPriced
                : Optional.of(materials.get(0));
    }
}
