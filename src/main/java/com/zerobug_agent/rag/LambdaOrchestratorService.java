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

    // Tên 2 con Lambda thực tế của bạn trên AWS
    private final String ragContextLambdaName = "RagContextLambda"; 
    private final String bedrockInvokeLambdaName = "BedrockInvokeLambda";

    public LambdaOrchestratorService(@Value("${zerobug.aws.region:us-east-1}") String region) {
        this.lambdaClient = LambdaClient.builder().region(Region.of(region)).build();
        this.objectMapper = new ObjectMapper();
    }

    public String requestUnitTestFromCloud(String userQuery) {
        try {
            // ====================================================
            // GIAI ĐOẠN 1: GỌI LAMBDA 13 (CẮT CODE & RAG)
            // ====================================================
            log.info("📡 Đang gửi câu hỏi lên Lambda 13 (Context Builder)...");
            Map<String, String> payload13 = new HashMap<>();
            payload13.put("query", userQuery);

            InvokeRequest req13 = InvokeRequest.builder()
                    .functionName(ragContextLambdaName)
                    .payload(SdkBytes.fromUtf8String(objectMapper.writeValueAsString(payload13)))
                    .build();

            InvokeResponse res13 = lambdaClient.invoke(req13);
            Map<String, String> result13 = objectMapper.readValue(res13.payload().asUtf8String(), Map.class);

            if (!"SUCCESS".equals(result13.get("status"))) {
                return "Lỗi từ Lambda 13: " + result13.get("errorMessage");
            }
            
            String finalPrompt = result13.get("final_prompt");
            log.info("✅ Lambda 13 đã nén xong Context. Chuẩn bị gọi AI...");

            // ====================================================
            // GIAI ĐOẠN 2: GỌI LAMBDA 15 (BEDROCK MISTRAL)
            // ====================================================
            log.info("🚀 Đang gửi Prompt lên Lambda 15 (Bedrock Invoke)...");
            Map<String, Object> payload15 = new HashMap<>();
            payload15.put("final_prompt", finalPrompt);

            InvokeRequest req15 = InvokeRequest.builder()
                    .functionName(bedrockInvokeLambdaName)
                    .payload(SdkBytes.fromUtf8String(objectMapper.writeValueAsString(payload15)))
                    .build();

            InvokeResponse res15 = lambdaClient.invoke(req15);
            // Kết quả của Lambda 15 trả về là chuỗi thuần (code Unit Test), không phải JSON map
            String generatedCode = res15.payload().asUtf8String();
            
            // Xóa dấu ngoặc kép bọc ngoài chuỗi (nếu có) do Lambda trả về
            if (generatedCode.startsWith("\"") && generatedCode.endsWith("\"")) {
                generatedCode = generatedCode.substring(1, generatedCode.length() - 1);
            }
            // Fix lỗi xuống dòng bị biến thành \n trong chuỗi
            generatedCode = generatedCode.replace("\\n", "\n").replace("\\\"", "\"");

            log.info("✅ Lambda 15 xử lý thành công! Đã mang code về máy.");
            return generatedCode;

        } catch (Exception e) {
            log.error("Lỗi đứt cáp lên mây AWS: {}", e.getMessage());
            return "Mất kết nối với hệ thống Serverless.";
        }
    }
}