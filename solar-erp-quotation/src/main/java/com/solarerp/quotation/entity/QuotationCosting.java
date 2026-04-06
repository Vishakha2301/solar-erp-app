package com.solarerp.quotation.entity;

import com.solarerp.costing.entity.SavedCostingEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "quotation_costings")
@Getter
@Setter
@NoArgsConstructor
public class QuotationCosting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id", nullable = false)
    private Quotation quotation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "costing_id", nullable = false)
    private SavedCostingEntity costing;

    @Column(name = "roof_label", length = 255)
    private String roofLabel;
}