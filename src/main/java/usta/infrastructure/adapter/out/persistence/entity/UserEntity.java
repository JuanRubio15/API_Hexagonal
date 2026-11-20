package usta.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class UserEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "username", unique = true)
    public String username;
    @Column(name = "password_hash")
    public String passwordHash;
    @Column(name = "role")
    public String role;
    @Column(name = "active")
    public boolean active;
}
