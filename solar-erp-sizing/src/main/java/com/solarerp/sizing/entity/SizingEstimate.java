package com.solarerp.sizing.entity;

import com.solarerp.costing.entity.SavedCostingEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * The stored result of a rooftop on-grid sizing calculation for a customer.
 *
 * This is a SYSTEM DESIGN, not a quote. It deliberately holds no subsidy,
 * net cost or payback -- those depend on the real per-job cost, which is
 * built in the costing step. indicativeMaterialCost is materials only.
 *
 * The bill of materials is held as JSONB (bom) -- a frozen result whose
 * shape can evolve without a schema migration.
 *
 * If a costing is created from this estimate, costing links the two.
 */
@Entity
@Table(name = "sizing_estimates")
@Getter
@Setter
@NoArgsConstructor
public class SizingEstimate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "customer_site_id")
    private UUID customerSiteId;

    // ----- Inputs -----

    @Column(name = "monthly_average_kwh", nullable = false,
            precision = 12, scale = 2)
    private BigDecimal monthlyAverageKwh;

    @Column(name = "growth_buffer_percent", nullable = false,
            precision = 5, scale = 2)
    private BigDecimal growthBufferPercent;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(name = "available_roof_area_sqm", nullable = false,
            precision = 10, scale = 2)
    private BigDecimal availableRoofAreaSqm;

    @Column(name = "panel_wattage_wp", nullable = false,
            precision = 8, scale = 2)
    private BigDecimal panelWattageWp;

    @Column(name = "connection_type", nullable = false, length = 20)
    private String connectionType;

    @Column(name = "phase_type", nullable = false, length = 20)
    private String phaseType;

    // ----- Outputs (system design) -----

    @Column(name = "target_monthly_consumption_kwh", nullable = false,
            precision = 12, scale = 2)
    private BigDecimal targetMonthlyConsumptionKwh;

    @Column(name = "recommended_capacity_kw", nullable = false,
            precision = 8, scale = 2)
    private BigDecimal recommendedCapacityKw;

    @Column(name = "roof_constrained", nullable = false)
    private boolean roofConstrained;

    @Column(name = "panel_count", nullable = false)
    private Integer panelCount;

    @Column(name = "inverter_capacity_kw", nullable = false,
            precision = 8, scale = 2)
    private BigDecimal inverterCapacityKw;

    @Column(name = "annual_generation_kwh", nullable = false,
            precision = 12, scale = 2)
    private BigDecimal annualGenerationKwh;

    @Column(name = "indicative_material_cost", nullable = false,
            precision = 14, scale = 2)
    private BigDecimal indicativeMaterialCost;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String bom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "costing_id")
    private SavedCostingEntity costing;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private UUID createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
