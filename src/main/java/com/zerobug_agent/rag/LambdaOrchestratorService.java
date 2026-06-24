package com.zerobug_agent.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.util.HashMap;
import java.util.Map;

@Service
public class LambdaOrchestratorService {

    private static final Logger log = LoggerFactory.getLogger(LambdaOrchestratorService.class);
    private final LambdaClient lambdaClient;
    private final ObjectMapper objectMapper;

    // Tên của hàm Lambda khi bạn deploy lên AWS
    @Value("${aws.lambda.rag-context:RagContextLambdaFunction}")
    private String ragLambdaName;

    public LambdaOrchestratorService(@Value("${zerobug.aws.region:us-east-1}") String region) {
        this.lambdaClient = LambdaClient.builder()
                .region(Region.of(region))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Dùng AWS SDK để "bấm nút" gọi Lambda 13 trên mây
     */
    public String requestUnitTestFromCloud(String userQuery) {
        log.info("📡 Đang gửi yêu cầu RAG lên AWS Lambda: {}", userQuery);

        try {
            // Chuẩn bị gói hàng gửi lên Lambda 13
            Map<String, String> payload = new HashMap<>();
            payload.put("query", userQuery);
            String jsonPayload = objectMapper.writeValueAsString(payload);

            InvokeRequest request = InvokeRequest.builder()
                    .functionName(ragLambdaName) // Gọi đích danh tên con Lambda 13
                    .payload(SdkBytes.fromUtf8String(jsonPayload))
                    .build();

            // Chờ Lambda trên Cloud chạy xong và nhận kết quả (Code Unit Test)
            InvokeResponse response = lambdaClient.invoke(request);
            String responseJson = response.payload().asUtf8String();
            
            // Bóc tách kết quả từ Lambda
            Map<String, String> result = objectMapper.readValue(responseJson, Map.class);
            
            if ("SUCCESS".equals(result.get("status"))) {
                log.info("✅ Lambda xử lý thành công! Đã mang code về EC2.");
                // Lambda 13 -> gọi tiếp Lambda 15 -> kết quả cuối cùng trả về đây.
                // Tùy theo luồng setup trên AWS, giả định result chứa key "generated_code"
                return result.get("generated_code"); 
            } else {
                return "Lỗi từ mây: " + result.get("errorMessage");
            }

        } catch (Exception e) {
            log.error("Lỗi khi kết nối AWS Lambda: {}", e.getMessage());
            return "Mất kết nối với hệ thống AI Serverless.";
        }
    }
}