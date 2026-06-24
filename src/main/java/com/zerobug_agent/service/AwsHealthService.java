package com.zerobug_agent.service;

import com.zerobug_agent.config.ZeroBugAgentProperties;
import com.zerobug_agent.dto.AwsHealthReportDto;
import com.zerobug_agent.dto.AwsServiceStatusDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrock.BedrockClient;
import software.amazon.awssdk.services.bedrock.model.GetFoundationModelRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.ArrayList;
import java.util.List;

@Service
public class AwsHealthService {

    private final JdbcTemplate jdbcTemplate;
    private final ZeroBugAgentProperties properties;

    private static final Logger log = LoggerFactory.getLogger(AwsHealthService.class);

    public AwsHealthService(JdbcTemplate jdbcTemplate, ZeroBugAgentProperties properties) {
        this.jdbcTemplate = jdbcTemplate;
        this.properties = properties;
    }

    @Autowired(required = false)
    private S3Client s3Client;

    public AwsHealthReportDto checkAll() {
        List<AwsServiceStatusDto> services = new ArrayList<>();
        services.add(checkRds());
        services.add(checkS3());
        services.add(checkBedrock());

        boolean allOk = services.stream()
                .allMatch(s -> "OK".equals(s.getStatus()) || "DISABLED".equals(s.getStatus()));

        return AwsHealthReportDto.builder()
                .allOk(allOk)
                .services(services)
                .build();
    }

    public void logStartupStatus() {
        AwsHealthReportDto report = checkAll();
        for (AwsServiceStatusDto service : report.getServices()) {
            switch (service.getStatus()) {
                case "OK":
                    log.info("[AWS {}] OK — {}", service.getService(), service.getMessage());
                    break;
                case "DISABLED":
                    log.info("[AWS {}] DISABLED — {}", service.getService(), service.getMessage());
                    break;
                default:
                    log.warn("[AWS {}] ERROR — {}", service.getService(), service.getMessage());
                    break;
            }
        }
    }

    private AwsServiceStatusDto checkRds() {
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            if (result != null && result == 1) {
                return ok("RDS PostgreSQL", "Kết nối database thành công");
            }
            return error("RDS PostgreSQL", "Truy vấn database trả về kết quả không hợp lệ");
        } catch (Exception e) {
            return error("RDS PostgreSQL", "Không thể kết nối RDS: " + rootMessage(e));
        }
    }

    private AwsServiceStatusDto checkS3() {
        if (!"s3".equalsIgnoreCase(properties.getStorage().getType())) {
            return disabled("Amazon S3", "Đang dùng lưu trữ local (không phải S3)");
        }
        if (s3Client == null) {
            return error("Amazon S3", "S3 chưa được cấu hình (thiếu S3Client bean)");
        }

        String bucket = properties.getStorage().getS3Bucket();
        if (bucket == null || bucket.isBlank()) {
            return error("Amazon S3", "Chưa cấu hình S3_BUCKET");
        }

        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            return ok("Amazon S3", "Bucket \"" + bucket + "\" truy cập thành công");
        } catch (S3Exception e) {
            return error("Amazon S3", "Không truy cập được bucket \"" + bucket + "\": " + e.awsErrorDetails().errorMessage());
        } catch (Exception e) {
            return error("Amazon S3", "Lỗi kiểm tra S3: " + rootMessage(e));
        }
    }

    private AwsServiceStatusDto checkBedrock() {
        if (!properties.getAws().isEnabled()) {
            return disabled("Amazon Bedrock", "AWS_ENABLED=false — AI dùng chế độ mock");
        }

        String modelId = properties.getAws().getModelId();
        String bedrockRegion = properties.getAws().getBedrockRegion();
        if (bedrockRegion == null || bedrockRegion.isBlank()) {
            bedrockRegion = properties.getAws().getRegion();
        }

        try (BedrockClient client = BedrockClient.builder()
                .region(Region.of(bedrockRegion))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build()) {

            client.getFoundationModel(GetFoundationModelRequest.builder()
                    .modelIdentifier(modelId)
                    .build());

            return ok("Amazon Bedrock", "Model \"" + modelId + "\" sẵn sàng (region " + bedrockRegion + ")");
        } catch (Exception e) {
            return error("Amazon Bedrock", "Không truy cập được model \"" + modelId + "\": " + rootMessage(e));
        }
    }

    private AwsServiceStatusDto ok(String service, String message) {
        return AwsServiceStatusDto.builder().service(service).status("OK").message(message).build();
    }

    private AwsServiceStatusDto error(String service, String message) {
        return AwsServiceStatusDto.builder().service(service).status("ERROR").message(message).build();
    }

    private AwsServiceStatusDto disabled(String service, String message) {
        return AwsServiceStatusDto.builder().service(service).status("DISABLED").message(message).build();
    }

    private String rootMessage(Throwable e) {
        Throwable cause = e;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause.getMessage() != null ? cause.getMessage() : e.getClass().getSimpleName();
    }
}
