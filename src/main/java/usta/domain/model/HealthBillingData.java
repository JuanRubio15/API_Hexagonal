package usta.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**Esta clase representa los datos propios del sector salud*/

@Getter
public final class HealthBillingData {
    private final Long id;
    private final String paymentModality;
    private final String benefitPlanCoverage;
    private final String contractNumber;
    private final String policyNumber;
    private final BigDecimal copayment;
    private final BigDecimal moderatingFee;
    private final BigDecimal sharedPayments;
    private final BigDecimal advances;

    @Builder
    private HealthBillingData(
            Long id,
            String paymentModality,
            String benefitPlanCoverage,
            String contractNumber,
            String policyNumber,
            BigDecimal copayment,
            BigDecimal moderatingFee,
            BigDecimal sharedPayments,
            BigDecimal advances) {
        copayment = amountOrZero(copayment);
        moderatingFee = amountOrZero(moderatingFee);
        sharedPayments = amountOrZero(sharedPayments);
        advances = amountOrZero(advances);

        this.id = id;
        this.paymentModality = paymentModality;
        this.benefitPlanCoverage = benefitPlanCoverage;
        this.contractNumber = contractNumber;
        this.policyNumber = policyNumber;
        this.copayment = copayment;
        this.moderatingFee = moderatingFee;
        this.sharedPayments = sharedPayments;
        this.advances = advances;
    }

    private static BigDecimal amountOrZero(BigDecimal amount) {
        if (amount == null) return BigDecimal.ZERO;
        if (amount.signum() < 0) throw new IllegalArgumentException("Los importes de salud deben ser no negativos.");
        return amount;
    }
}
