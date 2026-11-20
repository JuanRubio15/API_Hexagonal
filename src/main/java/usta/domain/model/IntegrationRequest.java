package usta.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
public final class IntegrationRequest {
    private final Long id;
    private final String sourceSystem;
    private final String sourceDocumentNumber;
    private final String originalPayload;
    private final Instant receivedAt;
    private Status status;
    private Long documentId;

    private IntegrationRequest(
            Long id,
            String sourceSystem,
            String sourceDocumentNumber,
            String originalPayload,
            Instant receivedAt,
            Status status,
            Long documentId) {
        if (originalPayload == null || receivedAt == null || status == null) {
            throw new IllegalArgumentException("La solicitud requiere contenido, fecha y estado.");
        }
        if ((status == Status.CONVERTED) != (documentId != null)) {
            throw new IllegalArgumentException("Solo una solicitud convertida tiene documento asociado.");
        }

        this.id = id;
        this.sourceSystem = sourceSystem;
        this.sourceDocumentNumber = sourceDocumentNumber;
        this.originalPayload = originalPayload;
        this.receivedAt = receivedAt;
        this.status = status;
        this.documentId = documentId;
    }

    public enum Status { PENDING, CONVERTED, REJECTED, DELETED }

    @Builder
    public static IntegrationRequest receive(String sourceSystem, String sourceDocumentNumber,
                                             String originalPayload, Instant receivedAt) {
        return new IntegrationRequest(null, sourceSystem, sourceDocumentNumber, originalPayload, receivedAt,
                Status.PENDING, null);
    }

    /** Recupera una solicitud guardada, conservando su identidad y su estado. */
    public static IntegrationRequest restore(long id, String sourceSystem, String sourceDocumentNumber,
                                             String originalPayload, Instant receivedAt, Status status, Long documentId) {
        return new IntegrationRequest(id, sourceSystem, sourceDocumentNumber, originalPayload, receivedAt,
                status, documentId);
    }

    public void markConverted(long documentId) {
        requirePending();
        this.documentId = documentId;
        status = Status.CONVERTED;
    }

    public void markDocumentDeleted() {
        if (status != Status.CONVERTED) throw new IllegalStateException("La solicitud no tiene una factura convertida.");
        documentId = null;
        status = Status.DELETED;
    }

    public void markRejected() {
        requirePending();
        status = Status.REJECTED;
    }

    private void requirePending() {
        if (status != Status.PENDING) throw new IllegalStateException("La solicitud ya está cerrada.");
    }
}
