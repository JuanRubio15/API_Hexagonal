package usta.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "adjustments")
public class AdjustmentEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "adjustment_type")
    public String adjustmentType;
    @Column(name = "concept_code")
    public String conceptCode;
    @Column(name = "description")
    public String description;
    @Column(name = "amount")
    public BigDecimal amount;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "document_id")
    public BillingDocumentEntity document;
}
