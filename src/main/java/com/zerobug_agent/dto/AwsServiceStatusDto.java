package com.zerobug_agent.dto;
public class AwsServiceStatusDto {
    private final String service;
    private final String status;
    private final String message;

    private AwsServiceStatusDto(String service, String status, String message) {
        this.service = service;
        this.status = status;
        this.message = message;
    }

    public String getService() {
        return service;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public static AwsServiceStatusDtoBuilder builder() {
        return new AwsServiceStatusDtoBuilder();
    }

    public static class AwsServiceStatusDtoBuilder {
        private String service;
        private String status;
        private String message;

        public AwsServiceStatusDtoBuilder service(String service) {
            this.service = service;
            return this;
        }

        public AwsServiceStatusDtoBuilder status(String status) {
            this.status = status;
            return this;
        }

        public AwsServiceStatusDtoBuilder message(String message) {
            this.message = message;
            return this;
        }

        public AwsServiceStatusDto build() {
            return new AwsServiceStatusDto(service, status, message);
        }
    }
}
