package com.zerobug_agent.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private boolean used;

    // Constructors
    public PasswordResetToken() {}

    private PasswordResetToken(PasswordResetTokenBuilder builder) {
        this.token = builder.token;
        this.user = builder.user;
        this.expiresAt = builder.expiresAt;
        this.used = builder.used;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }

    // Builder
    public static PasswordResetTokenBuilder builder() {
        return new PasswordResetTokenBuilder();
    }

    public static class PasswordResetTokenBuilder {
        private String token;
        private User user;
        private LocalDateTime expiresAt;
        private boolean used;

        public PasswordResetTokenBuilder token(String token) { this.token = token; return this; }
        public PasswordResetTokenBuilder user(User user) { this.user = user; return this; }
        public PasswordResetTokenBuilder expiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; return this; }
        public PasswordResetTokenBuilder used(boolean used) { this.used = used; return this; }

        public PasswordResetToken build() {
            return new PasswordResetToken(this);
        }
    }
}