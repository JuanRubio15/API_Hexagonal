package usta.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "processing_events")
public class ProcessingEventEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "occurred_at")
    public Instant occurredAt;
    @Column(name = "actor")
    public String actor;
    @Column(name = "operation")
    public String operation;
    @Column(name = "outcome")
    public String outcome;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "request_id")
    public IntegrationRequestEntity request;
    @ElementCollection
    @CollectionTable(name = "event_messages", joinColumns = @JoinColumn(name = "event_id"))
    @OrderColumn(name = "position")
    @Column(name = "message", columnDefinition = "TEXT")
    public java.util.List<String> messages = new java.util.ArrayList<>();
}
