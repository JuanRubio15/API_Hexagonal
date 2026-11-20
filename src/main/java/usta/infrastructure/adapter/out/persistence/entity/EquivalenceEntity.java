package usta.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "equivalences")
public class EquivalenceEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "category")
    public String category;
    @Column(name = "source_field")
    public String sourceField;
    @Column(name = "target_field")
    public String targetField;
    @Column(name = "source_value")
    public String sourceValue;
    @Column(name = "target_value")
    public String targetValue;
}
