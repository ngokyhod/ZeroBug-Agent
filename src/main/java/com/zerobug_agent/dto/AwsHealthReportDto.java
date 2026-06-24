package com.zerobug_agent.dto;

import java.util.List;

public class AwsHealthReportDto {
    private final boolean allOk;
    private final List<AwsServiceStatusDto> services;

    private AwsHealthReportDto(boolean allOk, List<AwsServiceStatusDto> services) {
        this.allOk = allOk;
        this.services = services;
    }

    public boolean isAllOk() {
        return allOk;
    }

    public List<AwsServiceStatusDto> getServices() {
        return services;
    }

    public static AwsHealthReportDtoBuilder builder() {
        return new AwsHealthReportDtoBuilder();
    }

    public static class AwsHealthReportDtoBuilder {
        private boolean allOk;
        private List<AwsServiceStatusDto> services;

        public AwsHealthReportDtoBuilder allOk(boolean allOk) {
            this.allOk = allOk;
            return this;
        }

        public AwsHealthReportDtoBuilder services(List<AwsServiceStatusDto> services) {
            this.services = services;
            return this;
        }

        public AwsHealthReportDto build() {
            return new AwsHealthReportDto(allOk, services);
        }
    }
}
