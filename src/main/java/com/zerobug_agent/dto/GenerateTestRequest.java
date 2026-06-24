package com.zerobug_agent.dto;

import jakarta.validation.constraints.NotBlank;

public class GenerateTestRequest {

    @NotBlank(message = "Yêu cầu không được để trống")
    private String requirements;

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }
}