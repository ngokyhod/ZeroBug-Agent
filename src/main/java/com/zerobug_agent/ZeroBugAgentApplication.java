package com.zerobug_agent;

import com.zerobug_agent.config.ZeroBugAgentProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ZeroBugAgentProperties.class)
public class ZeroBugAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZeroBugAgentApplication.class, args);
    }
}
