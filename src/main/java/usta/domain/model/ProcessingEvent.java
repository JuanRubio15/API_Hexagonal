package usta.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
public final class ProcessingEvent {
    private final Long id;
    private final Long requestId;
    private final Instant occurredAt;
    private final Operation operation;
    private final Outcome outcome;
    private final String actor;
    private final List<String> messages;

    @Builder
    private ProcessingEvent(
            Long id,
            Long requestId,
            Instant occurredAt,
            Operation operation,
            Outcome outcome,
            String actor,
            List<String> messages) {
        if (requestId == null || occurredAt == null || operation == null || outcome == null) {
            throw new IllegalArgumentException("El evento requiere solicitud, fecha, operación y resultado.");
        }
        messages = messages == null ? List.of() : List.copyOf(messages);
        if (outcome != Outcome.SUCCESS && messages.stream().noneMatch(message -> !message.isBlank())) {
            throw new IllegalArgumentException("Indique el motivo de la advertencia o del error.");
        }

        this.id = id;
        this.requestId = requestId;
        this.occurredAt = occurredAt;
        this.operation = operation;
        this.outcome = outcome;
        this.actor = actor;
        this.messages = messages;
    }

    public enum Operation { RECEIVED, CONVERSION, INCLUDED_IN_QUERY, DOCUMENT_UPDATED, DOCUMENT_DELETED }
    public enum Outcome { SUCCESS, WARNING, ERROR }
}
