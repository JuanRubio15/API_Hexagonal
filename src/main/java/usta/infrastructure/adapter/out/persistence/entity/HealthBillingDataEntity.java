package usta.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "health_billing_data")
public class HealthBillingDataEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "payment_modality")
    public String paymentModality;
    @Column(name = "benefit_plan_coverage")
    public String benefitPlanCoverage;
    @Column(name = "contract_number")
    public String contractNumber;
    @Column(name = "policy_number")
    public String policyNumber;
    @Column(name = "copayment")
    public BigDecimal copayment;
    @Column(name = "moderating_fee")
    public BigDecimal moderatingFee;
    @Column(name = "shared_payments")
    public BigDecimal sharedPayments;
    @Column(name = "advances")
    public BigDecimal advances;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "document_id")
    public BillingDocumentEntity document;
}
