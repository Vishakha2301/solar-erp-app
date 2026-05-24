package com.solarerp.material.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "materials")
@Getter
@Setter
@NoArgsConstructor
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaterialCategory category;

    @Column(name = "component_key", length = 50)
    private String componentKey;

    @Column(name = "brand_name", nullable = false, length = 255)
    private String brandName;

    @Column(name = "model_name", nullable = false, length = 255)
    private String modelName;

    @Column(columnDefinition = "TEXT")
    private String specification;

    @Column(length = 20)
    private String unit;

    @Column(length = 255)
    private String warranty;

    @Column(name = "hsn_code", length = 20)
    private String hsnCode;

    /**
     * Current catalog price per {@link #unit} (e.g. per Wp, per Nos, per Mtr).
     * Nullable: a material may exist in the catalog before a price is set.
     */
    @Column(name = "unit_price", precision = 12, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Applicable GST rate as a percentage (e.g. 5.00, 18.00).
     */
    @Column(name = "gst_rate", precision = 5, scale = 2)
    private BigDecimal gstRate;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false, updatable = false)
    private UUID createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
