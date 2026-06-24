package com.zerobug_agent.dto;

import com.zerobug_agent.entity.Project;
import com.zerobug_agent.entity.SourceType;
import java.time.LocalDateTime;
public class ProjectDto {
    private Long id;
    private String name;
    private SourceType sourceType;
    private String gitUrl;
    private LocalDateTime createdAt;

    public static ProjectDto from(Project project) {
        return ProjectDto.builder()
                .id(project.getId())
                .name(project.getName())
                .sourceType(project.getSourceType())
                .gitUrl(project.getGitUrl())
                .createdAt(project.getCreatedAt())
                .build();
    }

    public ProjectDto() {
    }

    public ProjectDto(Long id, String name, SourceType sourceType, String gitUrl, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.sourceType = sourceType;
        this.gitUrl = gitUrl;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public String getGitUrl() {
        return gitUrl;
    }

    public void setGitUrl(String gitUrl) {
        this.gitUrl = gitUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static ProjectDtoBuilder builder() {
        return new ProjectDtoBuilder();
    }

    public static class ProjectDtoBuilder {
        private Long id;
        private String name;
        private SourceType sourceType;
        private String gitUrl;
        private LocalDateTime createdAt;

        public ProjectDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ProjectDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ProjectDtoBuilder sourceType(SourceType sourceType) {
            this.sourceType = sourceType;
            return this;
        }

        public ProjectDtoBuilder gitUrl(String gitUrl) {
            this.gitUrl = gitUrl;
            return this;
        }

        public ProjectDtoBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ProjectDto build() {
            return new ProjectDto(id, name, sourceType, gitUrl, createdAt);
        }
    }
}
