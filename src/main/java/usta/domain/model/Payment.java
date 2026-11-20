package usta.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**Esta clase representa la forma de o formas de pago en un documento, (crédito, efectivo. tarjeta) y otros factores
 * como plazos, fechas de pago, etc*/

@Getter
public final class Payment {
    private final Long id;
    private final String paymentMethodCode;
    private final String ledgerAccount;
    private final BigDecimal amount;
    private final LocalDate dueDate;
    private final String installmentNumber;
    private final String payerIdentification;

    @Builder
    private Payment(
            Long id,
            String paymentMethodCode,
            String ledgerAccount,
            BigDecimal amount,
            LocalDate dueDate,
            String installmentNumber,
            String payerIdentification) {
        if (paymentMethodCode == null || paymentMethodCode.isBlank() || ledgerAccount == null || ledgerAccount.isBlank()) {
            throw new IllegalArgumentException("Se requieren forma de pago y cuenta.");
        }
        if (amount == null || amount.compareTo(new BigDecimal("0.01")) < 0) {
            throw new IllegalArgumentException("El pago mínimo es 0.01.");
        }

        this.id = id;
        this.paymentMethodCode = paymentMethodCode;
        this.ledgerAccount = ledgerAccount;
        this.amount = amount;
        this.dueDate = dueDate;
        this.installmentNumber = installmentNumber;
        this.payerIdentification = payerIdentification;
    }
}
