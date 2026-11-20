package usta.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**Clase que representa los ajustes en una factura: recargos y descuentos*/
@Getter
public final class Adjustment {
    private final Long id;
    private final String adjustmentType;
    private final String conceptCode;
    private final String description;
    private final BigDecimal amount;

    @Builder
    private Adjustment(
            Long id,
            String adjustmentType,
            String conceptCode,
            String description,
            BigDecimal amount) {

        //Se agregaron validaciones propias de esta clase

        if (!"C".equals(adjustmentType) && !"D".equals(adjustmentType)) {
            throw new IllegalArgumentException("El tipo de ajuste debe ser C o D.");
        }
        if (conceptCode == null || conceptCode.isBlank()) {
            throw new IllegalArgumentException("Falta el concepto del ajuste.");
        }
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("El importe del ajuste debe ser cero o positivo.");
        }

        this.id = id;
        this.adjustmentType = adjustmentType;
        this.conceptCode = conceptCode;
        this.description = description;
        this.amount = amount;
    }

    //Diferencia de comportamiento entre un recargo y un descuento
    public BigDecimal signedAmount() {
        if ("D".equals(adjustmentType)) {
            return amount.negate();
        }
        return amount;
    }

}
