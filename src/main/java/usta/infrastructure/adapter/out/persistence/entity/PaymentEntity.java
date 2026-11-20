package usta.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;

@Entity
@Table(name = "payments")
public class PaymentEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "payment_method_code")
    public String paymentMethodCode;
    @Column(name = "ledger_account")
    public String ledgerAccount;
    @Column(name = "amount")
    public BigDecimal amount;
    @Column(name = "due_date")
    public LocalDate dueDate;
    @Column(name = "installment_number")
    public String installmentNumber;
    @Column(name = "payer_identification")
    public String payerIdentification;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "document_id")
    public BillingDocumentEntity document;
}
