package com.zerobug_agent.controller;

import com.zerobug_agent.dto.AwsHealthReportDto;
import com.zerobug_agent.service.AwsHealthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/aws")
public class AwsHealthController {

    private final AwsHealthService awsHealthService;

    public AwsHealthController(AwsHealthService awsHealthService) {
        this.awsHealthService = awsHealthService;
    }

    @GetMapping("/status")
    public ResponseEntity<AwsHealthReportDto> status() {
        return ResponseEntity.ok(awsHealthService.checkAll());
    }
}
