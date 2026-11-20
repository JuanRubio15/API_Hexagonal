package usta.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "items")
public class ItemEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "item_reference")
    public String itemReference;
    @Column(name = "service_code")
    public String serviceCode;
    @Column(name = "quantity")
    public BigDecimal quantity;
    @Column(name = "price")
    public BigDecimal price;
    @Column(name = "unit_amount")
    public BigDecimal unitAmount;
    @Column(name = "line_amount")
    public BigDecimal lineAmount;
    @Column(name = "vat_rate")
    public BigDecimal vatRate;
    @Column(name = "vat_amount")
    public BigDecimal vatAmount;
    @Column(name = "consumption_tax_amount")
    public BigDecimal consumptionTaxAmount;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "document_id")
    public BillingDocumentEntity document;
}
