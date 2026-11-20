package usta.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "billing_documents")
public class BillingDocumentEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "document_type")
    public String documentType;
    @Column(name = "document_number")
    public String documentNumber;
    @Column(name = "external_number")
    public String externalNumber;
    @Column(name = "affected_document")
    public String affectedDocument;
    @Column(name = "issue_date")
    public LocalDate issueDate;
    @Column(name = "due_date")
    public LocalDate dueDate;
    @Column(name = "payment_term_days")
    public Integer paymentTermDays;
    @Column(name = "payment_method_code")
    public String paymentMethodCode;
    @Column(name = "seller_identification")
    public String sellerIdentification;
    @Column(name = "warehouse_code")
    public String warehouseCode;
    @Column(name = "description")
    public String description;
    @Column(name = "notes")
    public String notes;
    @Column(name = "cost_center")
    public String costCenter;
    @Column(name = "cost_subcenter")
    public String costSubcenter;
    @Column(name = "billing_period")
    public String billingPeriod;
    @Column(name = "period_start")
    public LocalDate periodStart;
    @Column(name = "period_end")
    public LocalDate periodEnd;
    @Column(name = "agent_identification")
    public String agentIdentification;
    @Column(name = "snapshot_identification_number")
    public String snapshotIdentificationNumber;
    @Column(name = "snapshot_identification_type")
    public String snapshotIdentificationType;
    @Column(name = "snapshot_verification_digit")
    public String snapshotVerificationDigit;
    @Column(name = "snapshot_first_name")
    public String snapshotFirstName;
    @Column(name = "snapshot_middle_name")
    public String snapshotMiddleName;
    @Column(name = "snapshot_first_surname")
    public String snapshotFirstSurname;
    @Column(name = "snapshot_second_surname")
    public String snapshotSecondSurname;
    @Column(name = "snapshot_display_name")
    public String snapshotDisplayName;
    @Column(name = "snapshot_address")
    public String snapshotAddress;
    @Column(name = "snapshot_phone")
    public String snapshotPhone;
    @Column(name = "snapshot_email")
    public String snapshotEmail;
    @Column(name = "snapshot_city_code")
    public String snapshotCityCode;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "customer_id")
    public CustomerEntity customer;
}
