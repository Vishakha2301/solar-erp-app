package com.solarerp.sizing.engine;

import com.solarerp.material.entity.Material;
import com.solarerp.material.entity.MaterialCategory;

import java.util.Optional;

/**
 * Abstraction over the Material catalog used by SolarSizingEngine.
 *
 * Keeping this as an interface lets the engine be unit-tested with a simple
 * in-memory stub instead of a database. The production implementation is
 * backed by MaterialRepository.
 */
public interface MaterialCatalog {

    /**
     * Returns the preferred active material for a category -- the cheapest
     * priced one, or any active one if none are priced. Empty if the catalog
     * has no active material in that category at all.
     */
    Optional<Material> preferredFor(MaterialCategory category);

    /**
     * Returns the preferred active material for a specific componentKey
     * (e.g. "dcCable"), falling back to category when no key match exists.
     */
    Optional<Material> preferredFor(MaterialCategory category,
                                    String componentKey);
}
