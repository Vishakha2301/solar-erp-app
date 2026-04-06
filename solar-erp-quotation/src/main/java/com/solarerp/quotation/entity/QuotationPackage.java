package com.solarerp.quotation.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "quotation_packages")
@Getter
@Setter
@NoArgsConstructor
public class QuotationPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id", nullable = false)
    private Quotation quotation;

    @Column(name = "package_name", nullable = false, length = 100)
    private String packageName;

    @Column(name = "is_recommended", nullable = false)
    private boolean isRecommended = false;

    @OneToMany(mappedBy = "quotationPackage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuotationPackageMaterial> materials = new ArrayList<>();
}