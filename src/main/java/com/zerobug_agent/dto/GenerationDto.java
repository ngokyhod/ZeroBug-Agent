package com.zerobug_agent.dto;

import com.zerobug_agent.entity.GenerationRecord;
import java.time.LocalDateTime;
public class GenerationDto {
    private Long id;
    private String requirements;
    private LocalDateTime createdAt;
    private Long projectId;
    private String projectName;

    public static GenerationDto from(GenerationRecord record) {
        return GenerationDto.builder()
                .id(record.getId())
                .requirements(record.getRequirements())
                .createdAt(record.getCreatedAt())
                .projectId(record.getProject() != null ? record.getProject().getId() : null)
                .projectName(record.getProject() != null ? record.getProject().getName() : null)
                .build();
    }

    public GenerationDto() {
    }

    public GenerationDto(Long id, String requirements, LocalDateTime createdAt, Long projectId, String projectName) {
        this.id = id;
        this.requirements = requirements;
        this.createdAt = createdAt;
        this.projectId = projectId;
        this.projectName = projectName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public static GenerationDtoBuilder builder() {
        return new GenerationDtoBuilder();
    }

    public static class GenerationDtoBuilder {
        private Long id;
        private String requirements;
        private LocalDateTime createdAt;
        private Long projectId;
        private String projectName;

        public GenerationDtoBuilder id(Long id) { this.id = id; return this; }
        public GenerationDtoBuilder requirements(String requirements) { this.requirements = requirements; return this; }
        public GenerationDtoBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public GenerationDtoBuilder projectId(Long projectId) { this.projectId = projectId; return this; }
        public GenerationDtoBuilder projectName(String projectName) { this.projectName = projectName; return this; }

        public GenerationDto build() {
            return new GenerationDto(id, requirements, createdAt, projectId, projectName);
        }
    }
}
