package com.zerobug_agent.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SourceType sourceType;

    private String gitUrl;

    @Column(nullable = false, unique = true)
    private String storagePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    
    // Constructors
    public Project() {}

    private Project(ProjectBuilder builder) {
        this.user = builder.user;
        this.name = builder.name;
        this.sourceType = builder.sourceType;
        this.gitUrl = builder.gitUrl;
        this.storagePath = builder.storagePath;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public SourceType getSourceType() { return sourceType; }
    public void setSourceType(SourceType sourceType) { this.sourceType = sourceType; }
    public String getGitUrl() { return gitUrl; }
    public void setGitUrl(String gitUrl) { this.gitUrl = gitUrl; }
    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Builder
    public static ProjectBuilder builder() {
        return new ProjectBuilder();
    }

    public static class ProjectBuilder {
        private User user;
        private String name;
        private SourceType sourceType;
        private String gitUrl;
        private String storagePath;

        public ProjectBuilder user(User user) { this.user = user; return this; }
        public ProjectBuilder name(String name) { this.name = name; return this; }
        public ProjectBuilder sourceType(SourceType sourceType) { this.sourceType = sourceType; return this; }
        public ProjectBuilder gitUrl(String gitUrl) { this.gitUrl = gitUrl; return this; }
        public ProjectBuilder storagePath(String storagePath) { this.storagePath = storagePath; return this; }

        public Project build() {
            return new Project(this);
        }
    }
}