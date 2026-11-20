package usta.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "integration_requests")
public class IntegrationRequestEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "source_system")
    public String sourceSystem;
    @Column(name = "source_document_number")
    public String sourceDocumentNumber;
    @Column(name = "original_payload", columnDefinition = "LONGTEXT")
    public String originalPayload;
    @Column(name = "received_at")
    public Instant receivedAt;
    @Column(name = "status")
    public String status;
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "document_id")
    public BillingDocumentEntity document;
}
