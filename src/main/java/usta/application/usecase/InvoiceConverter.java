package usta.application.usecase;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;
import usta.application.command.*;
import usta.domain.model.*;

/** Convierte una entrada IPS tipada usando las equivalencias vigentes en este intento. */
final class InvoiceConverter {
    private static final Pattern SUFFIX = Pattern.compile("([0-9]{1,10})$");
    private final List<Equivalence> mappings;
    private final List<String> errors = new ArrayList<>();

    InvoiceConverter(List<Equivalence> mappings) { this.mappings = List.copyOf(mappings); }

    private String translate(String category, String field, String key, String target) {
        for (Equivalence entry : mappings) {
            if (entry.getCategory().equals(category) && entry.getSourceField().equals(field)
                    && entry.getTargetField().equals(target) && key != null && key.equals(entry.getSourceValue())) {
                return entry.getTargetValue();
            }
        }
        errors.add("Falta equivalencia para " + field + " = " + key + " → " + target);
        return null;
    }

    BillingDocument convert(InvoiceInput input) {
        var header = input.header();
        var suffix = SUFFIX.matcher(header.number());
        String number = suffix.find() ? suffix.group(1) : null;
        String type = translate("documentos", "factura.tipo_documento", header.type(), "tipo");
        String site = header.site() == null ? null
                : translate("documentos", "factura.sede_atencion", header.site(), "bodega");
        Customer customer = customer(input.patient());
        var items = new ArrayList<Item>();
        for (var line : input.lines()) {
            String code = translate("items", "atenciones.codigo_servicio", line.service(), "servicio");
            if (code != null) items.add(Item.builder().serviceCode(code).quantity(line.quantity())
                    .price(line.unitAmount()).unitAmount(line.unitAmount())
                    .vatRate(line.vatRate() == null ? BigDecimal.ZERO : line.vatRate()).build());
        }
        var payments = new ArrayList<Payment>();
        for (var payment : input.payments()) {
            String code = translate("forma_pago", "pagos.metodo", payment.method(), "fpago");
            String account = translate("forma_pago", "pagos.metodo", payment.method(), "ctafpago");
            if (code != null && account != null) payments.add(Payment.builder().paymentMethodCode(code)
                    .ledgerAccount(account).amount(payment.amount()).dueDate(payment.date())
                    .payerIdentification(input.payerId()).build());
        }
        var adjustments = new ArrayList<Adjustment>();
        addAdjustments(adjustments, input.discounts(), "descuentos.tipo", "D");
        addAdjustments(adjustments, input.surcharges(), "recargos.tipo", "C");
        if (!errors.isEmpty()) throw new InputErrors(errors);
        var period = header.start() == null ? header.date() : header.start();
        return BillingDocument.builder().documentType(type).documentNumber(number)
                .externalNumber(number == null ? header.number() : null).affectedDocument(header.affected())
                .issueDate(header.date()).dueDate(header.dueDate()).periodStart(header.start()).periodEnd(header.end())
                .paymentTermDays(header.dueDate() == null ? 0 : Math.toIntExact(ChronoUnit.DAYS.between(header.date(), header.dueDate())))
                .billingPeriod(period.format(DateTimeFormatter.ofPattern("uuuuMM"))).warehouseCode(site)
                .notes(header.notes()).customer(customer).items(items).payments(payments).adjustments(adjustments)
                .healthBillingData(input.health() == null ? List.of() : List.of(input.health())).build();
    }

    private Customer customer(InvoiceInput.Patient p) {
        String type = translate("clientes", "paciente.tipo_identificacion", p.type().toUpperCase(Locale.ROOT), "claseid");
        if (type == null) return null;
        String[] parts = p.name().strip().split("\\s+");
        String name = String.join(" ", parts);
        var customer = Customer.builder().identificationType(type).identificationNumber(p.number())
                .verificationDigit(p.digit()).address(p.address()).phone(p.phone()).cityCode(p.city())
                .email(p.email()).displayName(name);
        if (type.equals("N")) return customer.firstName(name).build();
        customer.firstName(parts[0]);
        if (parts.length == 2) customer.firstSurname(parts[1]);
        if (parts.length >= 3) {
            customer.firstSurname(parts[parts.length - 2]).secondSurname(parts[parts.length - 1]);
        }
        if (parts.length >= 4) customer.middleName(String.join(" ", Arrays.copyOfRange(parts, 1, parts.length - 2)));
        return customer.build();
    }

    private void addAdjustments(List<Adjustment> result, List<InvoiceInput.AdjustmentInput> input,
                                String field, String expectedType) {
        for (var adjustment : input) {
            String type = translate("ajustes", field, adjustment.type(), "it");
            String concept = translate("ajustes", field, adjustment.type(), "cpto");
            if (type == null || concept == null) continue;
            if (!expectedType.equals(type)) {
                errors.add("El tipo " + adjustment.type() + " no corresponde a " + field);
                continue;
            }
            result.add(Adjustment.builder().adjustmentType(type).conceptCode(concept)
                    .description(adjustment.description()).amount(adjustment.amount()).build());
        }
    }
}
