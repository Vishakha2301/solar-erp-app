package com.solarerp.quotation.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "quotation_instalments")
@Getter
@Setter
@NoArgsConstructor
public class QuotationInstalment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id", nullable = false)
    private Quotation quotation;

    @Column(name = "instalment_no", nullable = false)
    private int instalmentNo;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentage;
}