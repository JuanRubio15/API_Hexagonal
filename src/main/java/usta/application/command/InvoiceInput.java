package usta.application.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import usta.domain.model.HealthBillingData;

/** Entrada IPS ya tipada. El lector normaliza pago/pagos y verifica el formato externo. */
public record InvoiceInput(Header header, Patient patient, String payerId, List<Line> lines,
                           List<PaymentInput> payments, List<AdjustmentInput> discounts,
                           List<AdjustmentInput> surcharges, HealthBillingData health) {
    public InvoiceInput {
        Objects.requireNonNull(header);
        Objects.requireNonNull(patient);
        lines = List.copyOf(lines);
        payments = List.copyOf(payments);
        discounts = List.copyOf(discounts);
        surcharges = List.copyOf(surcharges);
    }
    public record Header(String number, String type, LocalDate date, LocalDate dueDate,
                         LocalDate start, LocalDate end, String site, String notes, String affected) {}
    public record Patient(String type, String number, String digit, String name, String address,
                          String phone, String email, String city) {}
    public record Line(String service, BigDecimal quantity, BigDecimal unitAmount, BigDecimal vatRate) {}
    public record PaymentInput(String method, LocalDate date, BigDecimal amount) {}
    public record AdjustmentInput(String type, String description, BigDecimal amount) {}
}
