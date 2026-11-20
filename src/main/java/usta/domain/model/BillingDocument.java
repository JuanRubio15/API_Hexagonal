package usta.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**Esta clase representa a un documento (factura)*/
@Getter
public final class BillingDocument {
    private final Long id;
    private final String documentType;
    private final String documentNumber;
    private final String externalNumber;
    private final String affectedDocument;
    private final LocalDate issueDate;
    private final LocalDate dueDate;
    private final Integer paymentTermDays;
    private final String paymentMethodCode;
    private final String sellerIdentification;
    private final String warehouseCode;
    private final String description;
    private final String notes;
    private final String costCenter;
    private final String costSubcenter;
    private final String billingPeriod;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final String agentIdentification;
    private final Customer customer;
    private List<Item> items;
    private final List<Payment> payments;
    private final List<Adjustment> adjustments;
    private final List<HealthBillingData> healthBillingData;

    @Builder
    private BillingDocument(
            Long id,
            String documentType,
            String documentNumber,
            String externalNumber,
            String affectedDocument,
            LocalDate issueDate,
            LocalDate dueDate,
            Integer paymentTermDays,
            String paymentMethodCode,
            String sellerIdentification,
            String warehouseCode,
            String description,
            String notes,
            String costCenter,
            String costSubcenter,
            String billingPeriod,
            LocalDate periodStart,
            LocalDate periodEnd,
            String agentIdentification,
            Customer customer,
            List<Item> items,
            List<Payment> payments,
            List<Adjustment> adjustments,
            List<HealthBillingData> healthBillingData) {

        //Contiene validaciones propias de un documento

        if (documentType == null || documentType.isBlank()) {
            throw new IllegalArgumentException("Falta el tipo de documento.");
        }
        if ((documentNumber == null || documentNumber.isBlank()) && (externalNumber == null || externalNumber.isBlank())) {
            throw new IllegalArgumentException("Se requiere número o número externo.");
        }
         /**Revisar*/
        if (List.of("DVZ", "NC1", "NC2").contains(documentType) && (affectedDocument == null || affectedDocument.isBlank())) {
            throw new IllegalArgumentException("La nota crédito debe indicar el documento afectado.");
        }
        if (customer == null || issueDate == null) {
            throw new IllegalArgumentException("Se requieren cliente y fecha de emisión.");
        }
        if (dueDate != null && dueDate.isBefore(issueDate)) {
            throw new IllegalArgumentException("El vencimiento es anterior a la emisión.");
        }
        if (periodStart != null && periodEnd != null && periodEnd.isBefore(periodStart)) {
            throw new IllegalArgumentException("El fin del período es anterior al inicio.");
        }
        if (paymentTermDays != null && paymentTermDays < 0) {
            throw new IllegalArgumentException("El plazo debe ser cero o positivo.");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("El documento requiere al menos un ítem.");
        }
        items = List.copyOf(items);
        payments = payments == null ? List.of() : List.copyOf(payments);
        adjustments = adjustments == null ? List.of() : List.copyOf(adjustments);
        healthBillingData = healthBillingData == null ? List.of() : List.copyOf(healthBillingData);

        this.id = id;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.externalNumber = externalNumber;
        this.affectedDocument = affectedDocument;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.paymentTermDays = paymentTermDays;
        this.paymentMethodCode = paymentMethodCode;
        this.sellerIdentification = sellerIdentification;
        this.warehouseCode = warehouseCode;
        this.description = description;
        this.notes = notes;
        this.costCenter = costCenter;
        this.costSubcenter = costSubcenter;
        this.billingPeriod = billingPeriod;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.agentIdentification = agentIdentification;
        this.customer = customer;
        this.items = items;
        this.payments = payments;
        this.adjustments = adjustments;
        this.healthBillingData = healthBillingData;
    }

    //Impide que se eliminen todos los items de una factura, debe quedar al menos 1
    public void removeItem(long itemId) {
        if (items.size() <= 1) {
            throw new IllegalStateException("El documento debe conservar al menos un ítem.");
        }

        var updatedItems = new ArrayList<>(items);
        boolean removed = updatedItems.removeIf(item -> item.getId() != null && item.getId() == itemId);

        if (!removed) {
            throw new IllegalArgumentException("El ítem no pertenece al documento.");
        }

        items = List.copyOf(updatedItems);
    }

    //Calcula el valor de todos los items con impuestos y despues de aplicar los ajustes
    public BigDecimal netAmount() {
        BigDecimal net = BigDecimal.ZERO;
        for (Item item : items) net = net.add(item.totalWithTaxes());
        for (Adjustment adjustment : adjustments) net = net.add(adjustment.signedAmount());
        return net;
    }
}
