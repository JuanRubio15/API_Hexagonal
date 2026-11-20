package usta.domain.model;

import lombok.Builder;
import lombok.Getter;

/**Clase que representa las equivalencias entre el sistema externo y el sistema de facturación electrónica*/
@Getter
public final class Equivalence {
    private final Long id;
    private final String category;
    private final String sourceField;
    private final String targetField;
    private final String sourceValue;
    private final String targetValue;

    @Builder
    private Equivalence(
            Long id,
            String category,
            String sourceField,
            String targetField,
            String sourceValue,
            String targetValue) {
        if (category == null || category.isBlank() || sourceField == null || sourceField.isBlank() || targetField == null || targetField.isBlank()) {
            throw new IllegalArgumentException("Indique categoría y ambos campos.");
        }
        sourceValue = sourceValue == null || sourceValue.isBlank() ? null : sourceValue;
        targetValue = targetValue == null || targetValue.isBlank() ? null : targetValue;
        if ((sourceValue == null) != (targetValue == null)) {
            throw new IllegalArgumentException("Los valores de origen y destino se informan juntos.");
        }

        this.id = id;
        this.category = category;
        this.sourceField = sourceField;
        this.targetField = targetField;
        this.sourceValue = sourceValue;
        this.targetValue = targetValue;
    }
}
