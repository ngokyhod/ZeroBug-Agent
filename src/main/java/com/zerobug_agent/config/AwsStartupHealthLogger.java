package com.zerobug_agent.config;

import com.zerobug_agent.service.AwsHealthService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AwsStartupHealthLogger {

    private final AwsHealthService awsHealthService;

    public AwsStartupHealthLogger(AwsHealthService awsHealthService) {
        this.awsHealthService = awsHealthService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        awsHealthService.logStartupStatus();
    }
}
