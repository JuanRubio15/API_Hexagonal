package usta.domain.service;

import lombok.Getter;
import usta.domain.model.BillingDocument;
import usta.domain.model.Payment;

import java.math.BigDecimal;

/**Servicio encargado de realizar la conciliación económica (cuadre)de una factura*/

public final class BillingReconciliationService {
    private static final BigDecimal TOLERANCE = new BigDecimal("0.01");

    //Revisa si hay una diferencia aignificativa (0.01)
    // entre el valor neto de la factura y el registrado en el pago
    public Result reconcile(BillingDocument document) {
        BigDecimal net = document.netAmount();
        BigDecimal paid = document.getPayments().stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal difference = net.subtract(paid);
        boolean warning = !document.getPayments().isEmpty() && difference.abs().compareTo(TOLERANCE) > 0;
        return new Result(net, paid, difference, warning);
    }

    @Getter
    public static final class Result {
        private final BigDecimal net;
        private final BigDecimal paid;
        private final BigDecimal difference;
        private final boolean warning;

        private Result(BigDecimal net, BigDecimal paid, BigDecimal difference, boolean warning) {
            this.net = net;
            this.paid = paid;
            this.difference = difference;
            this.warning = warning;
        }
    }
}
