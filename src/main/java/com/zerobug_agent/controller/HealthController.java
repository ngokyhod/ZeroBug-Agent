package com.zerobug_agent.controller;

import com.zerobug_agent.config.ZeroBugAgentProperties;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
// ĐÃ XÓA @RequiredArgsConstructor Ở ĐÂY
public class HealthController {

    private final ZeroBugAgentProperties properties;
    private final Environment environment;

    // TỰ TAY THÊM ĐOẠN CONSTRUCTOR NÀY VÀO:
    public HealthController(ZeroBugAgentProperties properties, Environment environment) {
        this.properties = properties;
        this.environment = environment;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        String[] profiles = environment.getActiveProfiles();
        String profile = profiles.length > 0 ? profiles[0] : "default";
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "profile", profile,
                "awsEnabled", properties.getAws().isEnabled(),
                "storageType", properties.getStorage().getType() != null
                        ? properties.getStorage().getType()
                        : "local"
        ));
    }
}