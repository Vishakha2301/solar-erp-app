package com.solarerp.sizing.engine;

import com.solarerp.material.entity.Material;
import com.solarerp.material.entity.MaterialCategory;
import com.solarerp.sizing.dto.BomLineItem;
import com.solarerp.sizing.dto.SizingRequest;
import com.solarerp.sizing.dto.SizingResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SolarSizingEngine Tests (rooftop on-grid)")
class SolarSizingEngineTest {

    private SolarSizingEngine engine;
    private StubCatalog catalog;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        engine = new SolarSizingEngine();
        catalog = new StubCatalog();
        customerId = UUID.randomUUID();
    }

    /** Simple in-memory MaterialCatalog for deterministic tests. */
    private static class StubCatalog implements MaterialCatalog {
        private final Map<MaterialCategory, Material> byCategory =
                new EnumMap<>(MaterialCategory.class);

        void put(MaterialCategory category, BigDecimal price, BigDecimal gst) {
            Material m = new Material();
            m.setId(UUID.randomUUID());
            m.setCategory(category);
            m.setBrandName("TestBrand");
            m.setModelName("TestModel");
            m.setUnitPrice(price);
            m.setGstRate(gst);
            byCategory.put(category, m);
        }

        @Override
        public Optional<Material> preferredFor(MaterialCategory category) {
            return Optional.ofNullable(byCategory.get(category));
        }

        @Override
        public Optional<Material> preferredFor(MaterialCategory category,
                                               String componentKey) {
            return preferredFor(category);
        }
    }

    private void seedFullCatalog() {
        catalog.put(MaterialCategory.PANEL,
                new BigDecimal("11"), new BigDecimal("12"));
        catalog.put(MaterialCategory.INVERTER,
                new BigDecimal("25000"), new BigDecimal("12"));
        catalog.put(MaterialCategory.CABLE,
                new BigDecimal("45"), new BigDecimal("18"));
        catalog.put(MaterialCategory.STRUCTURE,
                new BigDecimal("1800"), new BigDecimal("18"));
        catalog.put(MaterialCategory.ELECTRICAL,
                new BigDecimal("15000"), new BigDecimal("18"));
    }

    /** Request from a list of monthly readings. Large roof, 550 Wp panels. */
    private SizingRequest requestFromReadings(List<BigDecimal> readings,
                                              BigDecimal bufferPercent) {
        return new SizingRequest(
                customerId, null,
                readings, null, bufferPercent,
                "Maharashtra",
                new BigDecimal("100"),
                new BigDecimal("550"),
                "RESIDENTIAL", "SINGLE");
    }

    /** Request from a known average. Large roof, 550 Wp panels. */
    private SizingRequest requestFromAverage(BigDecimal average,
                                             BigDecimal bufferPercent) {
        return new SizingRequest(
                customerId, null,
                null, average, bufferPercent,
                "Maharashtra",
                new BigDecimal("100"),
                new BigDecimal("550"),
                "RESIDENTIAL", "SINGLE");
    }

    @Nested
    @DisplayName("Consumption averaging")
    class ConsumptionTests {

        @Test
        @DisplayName("Averages multiple monthly readings")
        void averagesMultipleReadings() {
            seedFullCatalog();
            SizingResult result = engine.calculate(
                    requestFromReadings(List.of(
                            new BigDecimal("300"),
                            new BigDecimal("200"),
                            new BigDecimal("400")), BigDecimal.ZERO),
                    new BigDecimal("5.00"), catalog);

            assertThat(result.monthlyAverageKwh())
                    .isEqualByComparingTo(new BigDecimal("300.00"));
        }

        @Test
        @DisplayName("Uses a single reading as its own average")
        void singleReadingIsAverage() {
            seedFullCatalog();
            SizingResult result = engine.calculate(
                    requestFromReadings(List.of(new BigDecimal("250")),
                            BigDecimal.ZERO),
                    new BigDecimal("5.00"), catalog);

            assertThat(result.monthlyAverageKwh())
                    .isEqualByComparingTo(new BigDecimal("250.00"));
        }

        @Test
        @DisplayName("Uses a known average directly when supplied")
        void usesKnownAverage() {
            seedFullCatalog();
            SizingResult result = engine.calculate(
                    requestFromAverage(new BigDecimal("350"), BigDecimal.ZERO),
                    new BigDecimal("5.00"), catalog);

            assertThat(result.monthlyAverageKwh())
                    .isEqualByComparingTo(new BigDecimal("350.00"));
        }
    }

    @Nested
    @DisplayName("Growth buffer")
    class GrowthBufferTests {

        @Test
        @DisplayName("Default 10% buffer is applied when none is supplied")
        void defaultTenPercentBuffer() {
            seedFullCatalog();
            SizingResult result = engine.calculate(
                    requestFromAverage(new BigDecimal("300"), null),
                    new BigDecimal("5.00"), catalog);

            assertThat(result.growthBufferPercentApplied())
                    .isEqualByComparingTo(new BigDecimal("10"));
            assertThat(result.targetMonthlyConsumptionKwh())
                    .isEqualByComparingTo(new BigDecimal("330.00"));
        }

        @Test
        @DisplayName("A custom buffer overrides the default")
        void customBuffer() {
            seedFullCatalog();
            SizingResult result = engine.calculate(
                    requestFromAverage(new BigDecimal("300"),
                            new BigDecimal("20")),
                    new BigDecimal("5.00"), catalog);

            assertThat(result.targetMonthlyConsumptionKwh())
                    .isEqualByComparingTo(new BigDecimal("360.00"));
        }

        @Test
        @DisplayName("Zero buffer leaves the average unchanged")
        void zeroBuffer() {
            seedFullCatalog();
            SizingResult result = engine.calculate(
                    requestFromAverage(new BigDecimal("300"), BigDecimal.ZERO),
                    new BigDecimal("5.00"), catalog);

            assertThat(result.targetMonthlyConsumptionKwh())
                    .isEqualByComparingTo(new BigDecimal("300.00"));
        }
    }

    @Nested
    @DisplayName("Capacity sizing")
    class CapacitySizingTests {

        @Test
        @DisplayName("Sizes to 100% of buffered target consumption")
        void sizesToTarget() {
            seedFullCatalog();
            // 330 kWh/month / 30 = 11 kWh/day; 11 / (5.0 * 0.80) = 2.75 kW
            SizingResult result = engine.calculate(
                    requestFromAverage(new BigDecimal("300"), null),
                    new BigDecimal("5.00"), catalog);

            assertThat(result.requiredCapacityKw())
                    .isEqualByComparingTo(new BigDecimal("2.75"));
            assertThat(result.recommendedCapacityKw())
                    .isEqualByComparingTo(new BigDecimal("2.75"));
        }

        @Test
        @DisplayName("Roof capacity depends on panel wattage")
        void roofCapacityDependsOnPanelWattage() {
            seedFullCatalog();
            // Roof 100 m2, area-per-panel 2.2 -> 45 panels; 45 * 550 = 24.75 kW
            SizingResult result = engine.calculate(
                    requestFromAverage(new BigDecimal("300"), BigDecimal.ZERO),
                    new BigDecimal("5.00"), catalog);

            assertThat(result.roofLimitedCapacityKw())
                    .isEqualByComparingTo(new BigDecimal("24.75"));
        }

        @Test
        @DisplayName("Small roof constrains the system below required")
        void smallRoofConstrains() {
            seedFullCatalog();
            // Roof 5 m2 -> 2 panels -> 2 * 550 = 1.10 kW; required 2.75 kW
            SizingRequest req = new SizingRequest(
                    customerId, null,
                    null, new BigDecimal("300"), null,
                    "Maharashtra",
                    new BigDecimal("5"),
                    new BigDecimal("550"),
                    "RESIDENTIAL", "SINGLE");

            SizingResult result = engine.calculate(
                    req, new BigDecimal("5.00"), catalog);

            assertThat(result.roofConstrained()).isTrue();
            assertThat(result.recommendedCapacityKw())
                    .isEqualByComparingTo(new BigDecimal("1.10"));
        }

        @Test
        @DisplayName("Higher-wattage panels yield more kW on the same roof")
        void higherWattageMoreKw() {
            seedFullCatalog();
            SizingRequest with550 = new SizingRequest(
                    customerId, null, null, new BigDecimal("300"),
                    BigDecimal.ZERO, "Maharashtra",
                    new BigDecimal("10"), new BigDecimal("550"),
                    "RESIDENTIAL", "SINGLE");
            SizingRequest with450 = new SizingRequest(
                    customerId, null, null, new BigDecimal("300"),
                    BigDecimal.ZERO, "Maharashtra",
                    new BigDecimal("10"), new BigDecimal("450"),
                    "RESIDENTIAL", "SINGLE");

            BigDecimal roof550 = engine.calculate(with550,
                    new BigDecimal("5.00"), catalog)
                    .roofLimitedCapacityKw();
            BigDecimal roof450 = engine.calculate(with450,
                    new BigDecimal("5.00"), catalog)
                    .roofLimitedCapacityKw();

            assertThat(roof550).isGreaterThan(roof450);
        }
    }

    @Nested
    @DisplayName("Bill of materials")
    class BomTests {

        @Test
        @DisplayName("Generates a line for every component category")
        void generatesAllCategories() {
            seedFullCatalog();
            SizingResult result = engine.calculate(
                    requestFromAverage(new BigDecimal("300"), null),
                    new BigDecimal("5.00"), catalog);

            assertThat(result.billOfMaterials())
                    .extracting(BomLineItem::category)
                    .contains("PANEL", "INVERTER", "CABLE",
                            "STRUCTURE", "ELECTRICAL");
        }

        @Test
        @DisplayName("fullyPriced is true when all materials are priced")
        void fullyPricedWhenAllPriced() {
            seedFullCatalog();
            SizingResult result = engine.calculate(
                    requestFromAverage(new BigDecimal("300"), null),
                    new BigDecimal("5.00"), catalog);

            assertThat(result.fullyPriced()).isTrue();
        }

        @Test
        @DisplayName("fullyPriced is false when a category has no material")
        void notFullyPricedWhenCategoryMissing() {
            catalog.put(MaterialCategory.PANEL,
                    new BigDecimal("11"), new BigDecimal("12"));
            catalog.put(MaterialCategory.CABLE,
                    new BigDecimal("45"), new BigDecimal("18"));
            catalog.put(MaterialCategory.STRUCTURE,
                    new BigDecimal("1800"), new BigDecimal("18"));
            catalog.put(MaterialCategory.ELECTRICAL,
                    new BigDecimal("15000"), new BigDecimal("18"));
            // no inverter

            SizingResult result = engine.calculate(
                    requestFromAverage(new BigDecimal("300"), null),
                    new BigDecimal("5.00"), catalog);

            assertThat(result.fullyPriced()).isFalse();
        }

        @Test
        @DisplayName("Indicative material cost is positive when fully priced")
        void indicativeCostPositive() {
            seedFullCatalog();
            SizingResult result = engine.calculate(
                    requestFromAverage(new BigDecimal("300"), null),
                    new BigDecimal("5.00"), catalog);

            assertThat(result.indicativeMaterialCost().signum())
                    .isPositive();
        }
    }

    @Nested
    @DisplayName("Generation and reasonableness")
    class GenerationTests {

        @Test
        @DisplayName("Annual generation scales with capacity and sun hours")
        void annualGenerationScales() {
            seedFullCatalog();
            // recommended 2.75 kW * 5.0 PSH * 0.80 * 365 = 4015 kWh
            SizingResult result = engine.calculate(
                    requestFromAverage(new BigDecimal("300"), null),
                    new BigDecimal("5.00"), catalog);

            assertThat(result.annualGenerationKwh())
                    .isEqualByComparingTo(new BigDecimal("4015.00"));
        }

        @Test
        @DisplayName("Typical household sizing is not flagged as excessive")
        void typicalHouseholdNotFlagged() {
            seedFullCatalog();
            SizingResult result = engine.calculate(
                    requestFromAverage(new BigDecimal("300"), null),
                    new BigDecimal("5.00"), catalog);

            assertThat(result.exceedsTypicalHousehold()).isFalse();
        }

        @Test
        @DisplayName("A very large system is flagged as exceeding typical")
        void largeSystemFlagged() {
            seedFullCatalog();
            SizingResult result = engine.calculate(
                    requestFromAverage(new BigDecimal("3000"), BigDecimal.ZERO),
                    new BigDecimal("5.00"), catalog);

            assertThat(result.exceedsTypicalHousehold()).isTrue();
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Falls back to default PSH when none is supplied")
        void fallsBackToDefaultPsh() {
            seedFullCatalog();
            SizingResult result = engine.calculate(
                    requestFromAverage(new BigDecimal("300"), null),
                    null, catalog);

            assertThat(result.recommendedCapacityKw().signum())
                    .isPositive();
        }

        @Test
        @DisplayName("Tiny consumption still yields at least 1 kW")
        void minimumOneKw() {
            seedFullCatalog();
            SizingResult result = engine.calculate(
                    requestFromAverage(new BigDecimal("5"), BigDecimal.ZERO),
                    new BigDecimal("5.00"), catalog);

            assertThat(result.recommendedCapacityKw())
                    .isGreaterThanOrEqualTo(new BigDecimal("1.00"));
        }
    }
}
