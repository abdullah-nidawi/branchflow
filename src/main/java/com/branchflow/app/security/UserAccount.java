package com.branchflow.app.security;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_account")
public class UserAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 100)
    private String username;
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private UserRole role;
    @Column(nullable = false)
    private boolean enabled;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected UserAccount() {}
    public UserAccount(String username, String passwordHash, UserRole role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.enabled = true;
        this.createdAt = Instant.now();
    }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public UserRole getRole() { return role; }
    public boolean isEnabled() { return enabled; }
}
