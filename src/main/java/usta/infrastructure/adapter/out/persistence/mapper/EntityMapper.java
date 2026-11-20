package usta.infrastructure.adapter.out.persistence.mapper;

import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
import usta.domain.model.*;
import usta.infrastructure.adapter.out.persistence.entity.*;
import java.util.List;

public final class EntityMapper {
    private EntityMapper() {}

    public static void copy(Customer value, CustomerEntity row) {
        row.identificationNumber = value.getIdentificationNumber();
        row.identificationType = value.getIdentificationType();
        row.verificationDigit = value.getVerificationDigit();
        row.firstName = value.getFirstName();
        row.middleName = value.getMiddleName();
        row.firstSurname = value.getFirstSurname();
        row.secondSurname = value.getSecondSurname();
        row.displayName = value.getDisplayName();
        row.address = value.getAddress();
        row.phone = value.getPhone();
        row.email = value.getEmail();
        row.cityCode = value.getCityCode();
    }

    public static Customer toDomain(CustomerEntity row) {
        return Customer.builder().id(row.id)
                .identificationNumber(row.identificationNumber)
                .identificationType(row.identificationType)
                .verificationDigit(row.verificationDigit)
                .firstName(row.firstName)
                .middleName(row.middleName)
                .firstSurname(row.firstSurname)
                .secondSurname(row.secondSurname)
                .displayName(row.displayName)
                .address(row.address)
                .phone(row.phone)
                .email(row.email)
                .cityCode(row.cityCode).build();
    }

    public static void copy(Equivalence value, EquivalenceEntity row) {
        row.category = value.getCategory();
        row.sourceField = value.getSourceField();
        row.targetField = value.getTargetField();
        row.sourceValue = value.getSourceValue();
        row.targetValue = value.getTargetValue();
    }

    public static Equivalence toDomain(EquivalenceEntity row) {
        return Equivalence.builder().id(row.id)
                .category(row.category)
                .sourceField(row.sourceField)
                .targetField(row.targetField)
                .sourceValue(row.sourceValue)
                .targetValue(row.targetValue).build();
    }

    public static void copy(Item value, ItemEntity row) {
        row.itemReference = value.getItemReference();
        row.serviceCode = value.getServiceCode();
        row.quantity = value.getQuantity();
        row.price = value.getPrice();
        row.unitAmount = value.getUnitAmount();
        row.lineAmount = value.getLineAmount();
        row.vatRate = value.getVatRate();
        row.vatAmount = value.getVatAmount();
        row.consumptionTaxAmount = value.getConsumptionTaxAmount();
    }

    public static Item toDomain(ItemEntity row) {
        return Item.builder().id(row.id)
                .itemReference(row.itemReference)
                .serviceCode(row.serviceCode)
                .quantity(row.quantity)
                .price(row.price)
                .unitAmount(row.unitAmount)
                .lineAmount(row.lineAmount)
                .vatRate(row.vatRate)
                .vatAmount(row.vatAmount)
                .consumptionTaxAmount(row.consumptionTaxAmount).build();
    }

    public static void copy(Payment value, PaymentEntity row) {
        row.paymentMethodCode = value.getPaymentMethodCode();
        row.ledgerAccount = value.getLedgerAccount();
        row.amount = value.getAmount();
        row.dueDate = value.getDueDate();
        row.installmentNumber = value.getInstallmentNumber();
        row.payerIdentification = value.getPayerIdentification();
    }

    public static Payment toDomain(PaymentEntity row) {
        return Payment.builder().id(row.id)
                .paymentMethodCode(row.paymentMethodCode)
                .ledgerAccount(row.ledgerAccount)
                .amount(row.amount)
                .dueDate(row.dueDate)
                .installmentNumber(row.installmentNumber)
                .payerIdentification(row.payerIdentification).build();
    }

    public static void copy(Adjustment value, AdjustmentEntity row) {
        row.adjustmentType = value.getAdjustmentType();
        row.conceptCode = value.getConceptCode();
        row.description = value.getDescription();
        row.amount = value.getAmount();
    }

    public static Adjustment toDomain(AdjustmentEntity row) {
        return Adjustment.builder().id(row.id)
                .adjustmentType(row.adjustmentType)
                .conceptCode(row.conceptCode)
                .description(row.description)
                .amount(row.amount).build();
    }

    public static void copy(HealthBillingData value, HealthBillingDataEntity row) {
        row.paymentModality = value.getPaymentModality();
        row.benefitPlanCoverage = value.getBenefitPlanCoverage();
        row.contractNumber = value.getContractNumber();
        row.policyNumber = value.getPolicyNumber();
        row.copayment = value.getCopayment();
        row.moderatingFee = value.getModeratingFee();
        row.sharedPayments = value.getSharedPayments();
        row.advances = value.getAdvances();
    }

    public static HealthBillingData toDomain(HealthBillingDataEntity row) {
        return HealthBillingData.builder().id(row.id)
                .paymentModality(row.paymentModality)
                .benefitPlanCoverage(row.benefitPlanCoverage)
                .contractNumber(row.contractNumber)
                .policyNumber(row.policyNumber)
                .copayment(row.copayment)
                .moderatingFee(row.moderatingFee)
                .sharedPayments(row.sharedPayments)
                .advances(row.advances).build();
    }

    public static void copy(BillingDocument value, BillingDocumentEntity row) {
        row.documentType = value.getDocumentType();
        row.documentNumber = value.getDocumentNumber();
        row.externalNumber = value.getExternalNumber();
        row.affectedDocument = value.getAffectedDocument();
        row.issueDate = value.getIssueDate();
        row.dueDate = value.getDueDate();
        row.paymentTermDays = value.getPaymentTermDays();
        row.paymentMethodCode = value.getPaymentMethodCode();
        row.sellerIdentification = value.getSellerIdentification();
        row.warehouseCode = value.getWarehouseCode();
        row.description = value.getDescription();
        row.notes = value.getNotes();
        row.costCenter = value.getCostCenter();
        row.costSubcenter = value.getCostSubcenter();
        row.billingPeriod = value.getBillingPeriod();
        row.periodStart = value.getPeriodStart();
        row.periodEnd = value.getPeriodEnd();
        row.agentIdentification = value.getAgentIdentification();
        row.snapshotIdentificationNumber = value.getCustomer().getIdentificationNumber();
        row.snapshotIdentificationType = value.getCustomer().getIdentificationType();
        row.snapshotVerificationDigit = value.getCustomer().getVerificationDigit();
        row.snapshotFirstName = value.getCustomer().getFirstName();
        row.snapshotMiddleName = value.getCustomer().getMiddleName();
        row.snapshotFirstSurname = value.getCustomer().getFirstSurname();
        row.snapshotSecondSurname = value.getCustomer().getSecondSurname();
        row.snapshotDisplayName = value.getCustomer().getDisplayName();
        row.snapshotAddress = value.getCustomer().getAddress();
        row.snapshotPhone = value.getCustomer().getPhone();
        row.snapshotEmail = value.getCustomer().getEmail();
        row.snapshotCityCode = value.getCustomer().getCityCode();
    }

    public static BillingDocument toDomain(BillingDocumentEntity row, List<Item> items, List<Payment> payments, List<Adjustment> adjustments, List<HealthBillingData> health) {
        return BillingDocument.builder().id(row.id)
                .documentType(row.documentType)
                .documentNumber(row.documentNumber)
                .externalNumber(row.externalNumber)
                .affectedDocument(row.affectedDocument)
                .issueDate(row.issueDate)
                .dueDate(row.dueDate)
                .paymentTermDays(row.paymentTermDays)
                .paymentMethodCode(row.paymentMethodCode)
                .sellerIdentification(row.sellerIdentification)
                .warehouseCode(row.warehouseCode)
                .description(row.description)
                .notes(row.notes)
                .costCenter(row.costCenter)
                .costSubcenter(row.costSubcenter)
                .billingPeriod(row.billingPeriod)
                .periodStart(row.periodStart)
                .periodEnd(row.periodEnd)
                .agentIdentification(row.agentIdentification)
                .customer(Customer.builder().id(row.customer.id)
                        .identificationNumber(row.snapshotIdentificationNumber)
                        .identificationType(row.snapshotIdentificationType)
                        .verificationDigit(row.snapshotVerificationDigit)
                        .firstName(row.snapshotFirstName)
                        .middleName(row.snapshotMiddleName)
                        .firstSurname(row.snapshotFirstSurname)
                        .secondSurname(row.snapshotSecondSurname)
                        .displayName(row.snapshotDisplayName)
                        .address(row.snapshotAddress)
                        .phone(row.snapshotPhone)
                        .email(row.snapshotEmail)
                        .cityCode(row.snapshotCityCode).build())
                .items(items).payments(payments).adjustments(adjustments).healthBillingData(health).build();
    }

    public static void copy(User value, UserEntity row) {
        row.username = value.getUsername();
        row.passwordHash = value.getPasswordHash();
        row.role = value.getRole().name();
        row.active = value.isActive();
    }

    public static User toDomain(UserEntity row) {
        return User.builder().id(row.id)
                .username(row.username)
                .passwordHash(row.passwordHash)
                .role(Role.valueOf(row.role))
                .active(row.active).build();
    }
}
