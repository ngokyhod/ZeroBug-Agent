package com.zerobug_agent.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class GenerationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String requirements;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String response;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Constructors
    public GenerationRecord() {}

    private GenerationRecord(GenerationRecordBuilder builder) {
        this.user = builder.user;
        this.project = builder.project;
        this.requirements = builder.requirements;
        this.response = builder.response;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Builder
    public static GenerationRecordBuilder builder() {
        return new GenerationRecordBuilder();
    }

    public static class GenerationRecordBuilder {
        private User user;
        private Project project;
        private String requirements;
        private String response;

        public GenerationRecordBuilder user(User user) { this.user = user; return this; }
        public GenerationRecordBuilder project(Project project) { this.project = project; return this; }
        public GenerationRecordBuilder requirements(String requirements) { this.requirements = requirements; return this; }
        public GenerationRecordBuilder response(String response) { this.response = response; return this; }

        public GenerationRecord build() {
            return new GenerationRecord(this);
        }
    }
}