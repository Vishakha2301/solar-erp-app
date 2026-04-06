package com.solarerp.quotation.entity;

import com.solarerp.material.entity.Material;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "quotation_package_materials")
@Getter
@Setter
@NoArgsConstructor
public class QuotationPackageMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private QuotationPackage quotationPackage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(name = "component_key", length = 50)
    private String componentKey;

    @Column(name = "is_recommended", nullable = false)
    private boolean isRecommended = false;
}