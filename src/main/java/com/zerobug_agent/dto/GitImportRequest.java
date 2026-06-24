package com.zerobug_agent.dto;

import jakarta.validation.constraints.NotBlank;

public class GitImportRequest {

    @NotBlank(message = "Git URL không được để trống")
    private String gitUrl;
    private String projectName;

    public String getGitUrl() {
        return gitUrl;
    }

    public void setGitUrl(String gitUrl) {
        this.gitUrl = gitUrl;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}