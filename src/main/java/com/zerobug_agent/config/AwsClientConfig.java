package com.zerobug_agent.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@ConditionalOnProperty(name = "zerobug.storage.type", havingValue = "s3")
public class AwsClientConfig {

    @Bean
    public S3Client s3Client(ZeroBugAgentProperties properties) {
        String region = properties.getStorage().getS3Region();
        if (region == null || region.isBlank()) {
            region = properties.getAws().getRegion();
        }
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
