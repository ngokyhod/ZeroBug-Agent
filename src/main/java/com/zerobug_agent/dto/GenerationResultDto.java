package com.zerobug_agent.dto;

import com.zerobug_agent.entity.GenerationRecord;
public class GenerationResultDto {
    private final GenerationRecord record;
    private final String aiSource;
    private final String awsMessage;

    private GenerationResultDto(GenerationRecord record, String aiSource, String awsMessage) {
        this.record = record;
        this.aiSource = aiSource;
        this.awsMessage = awsMessage;
    }

    public GenerationRecord getRecord() {
        return record;
    }

    public String getAiSource() {
        return aiSource;
    }

    public String getAwsMessage() {
        return awsMessage;
    }

    public static GenerationResultDtoBuilder builder() {
        return new GenerationResultDtoBuilder();
    }

    public static class GenerationResultDtoBuilder {
        private GenerationRecord record;
        private String aiSource;
        private String awsMessage;

        public GenerationResultDtoBuilder record(GenerationRecord record) {
            this.record = record;
            return this;
        }

        public GenerationResultDtoBuilder aiSource(String aiSource) {
            this.aiSource = aiSource;
            return this;
        }

        public GenerationResultDtoBuilder awsMessage(String awsMessage) {
            this.awsMessage = awsMessage;
            return this;
        }

        public GenerationResultDto build() {
            return new GenerationResultDto(record, aiSource, awsMessage);
        }
    }
}
