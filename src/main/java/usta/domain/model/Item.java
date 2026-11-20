package usta.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**Clase que representa un item en una factura, ya sea un servicio o un artículo*/

@Getter
public final class Item {
    private final Long id;
    private final String itemReference;
    private final String serviceCode;
    private final BigDecimal quantity;
    private final BigDecimal price;
    private final BigDecimal unitAmount;
    private final BigDecimal lineAmount;
    private final BigDecimal vatRate;
    private final BigDecimal vatAmount;
    private final BigDecimal consumptionTaxAmount;

    @Builder
    private Item(
            Long id,
            String itemReference,
            String serviceCode,
            BigDecimal quantity,
            BigDecimal price,
            BigDecimal unitAmount,
            BigDecimal lineAmount, //Subtotal por item (cantidad x precio unit)
            BigDecimal vatRate,
            BigDecimal vatAmount,
            BigDecimal consumptionTaxAmount) {
        boolean hasReference = itemReference != null && !itemReference.isBlank();
        boolean hasService = serviceCode != null && !serviceCode.isBlank();
        if (hasReference == hasService) {
            throw new IllegalArgumentException("Indique exactamente una referencia o servicio.");
        }
        if (quantity == null || quantity.compareTo(new BigDecimal("0.0001")) < 0) {
            throw new IllegalArgumentException("Cantidad inferior al mínimo permitido.");
        }
        if (unitAmount == null || unitAmount.signum() < 0) {
            throw new IllegalArgumentException("El valor unitario debe ser cero o positivo.");
        }
        if (price != null && price.signum() < 0) {
            throw new IllegalArgumentException("Precio negativo.");
        }
        if (vatRate == null || vatRate.signum() < 0 || vatRate.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("El porcentaje de IVA debe estar entre 0 y 100.");
        }
        BigDecimal expectedLineAmount = quantity.multiply(unitAmount);
        if (lineAmount == null) {
            lineAmount = roundSourceAmount(quantity.doubleValue() * unitAmount.doubleValue());
        }
        //Verifica que sea coherente la información de valores
        if (lineAmount.signum() < 0 || expectedLineAmount.subtract(lineAmount).abs().compareTo(new BigDecimal("0.01")) > 0) {
            throw new IllegalArgumentException("El total de línea es inválido o difiere de cantidad por valor unitario.");
        }
        //Calcula IVA en casi  de que no lo hayan ingresado
        if (vatAmount == null) {
            vatAmount = roundSourceAmount(lineAmount.doubleValue() * vatRate.doubleValue() / 100);
        }
        if (consumptionTaxAmount == null) consumptionTaxAmount = BigDecimal.ZERO;
        if (vatAmount.signum() < 0 || consumptionTaxAmount.signum() < 0) {
            throw new IllegalArgumentException("Los impuestos deben ser cero o positivos.");
        }

        this.id = id;
        this.itemReference = itemReference;
        this.serviceCode = serviceCode;
        this.quantity = quantity;
        this.price = price;
        this.unitAmount = unitAmount;
        this.lineAmount = lineAmount;
        this.vatRate = vatRate;
        this.vatAmount = vatAmount;
        this.consumptionTaxAmount = consumptionTaxAmount;
    }

    // Reproduce round(float, 2) del conversor IPS solo para los importes derivados.
    private static BigDecimal roundSourceAmount(double amount) {
        if (!Double.isFinite(amount)) throw new IllegalArgumentException("Importe calculado fuera de rango.");
        return new BigDecimal(amount).setScale(2, RoundingMode.HALF_EVEN);
    }

    //Calcula valor final con impuestos
    public BigDecimal totalWithTaxes() {
        return lineAmount.add(vatAmount).add(consumptionTaxAmount);
    }
}
