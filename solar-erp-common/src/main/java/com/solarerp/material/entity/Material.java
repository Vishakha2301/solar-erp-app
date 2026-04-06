package com.solarerp.material.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "material_category")
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
