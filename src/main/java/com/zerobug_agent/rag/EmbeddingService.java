package com.zerobug_agent.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);
    
    private final BedrockRuntimeClient bedrockClient;
    private final ObjectMapper objectMapper;

    // Tự động lấy region (us-east-1) từ file application.yml, nếu không có thì mặc định là us-east-1
    public EmbeddingService(@Value("${zerobug.aws.bedrock-region:us-east-1}") String region) {
        // Khởi tạo Client sẽ tự động lấy thông tin Access Key / Secret Key từ AWS CLI trên máy bạn
        this.bedrockClient = BedrockRuntimeClient.builder()
                .region(Region.of(region))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Gọi Amazon Bedrock (Titan v2) để biến văn bản thành mảng float[] 1024 chiều.
     */
    public float[] getEmbedding(String text) {
        try {
            // 1. Chuẩn bị Payload gửi lên Titan v2
            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("inputText", text);
            payloadMap.put("dimensions", 1024); // BẮT BUỘC KHỚP VỚI CỘT vector(1024) TRONG DATABASE
            payloadMap.put("normalize", true);  // CỰC KỲ QUAN TRỌNG: Tối ưu cho thuật toán Cosine Similarity

            String payload = objectMapper.writeValueAsString(payloadMap);

            // 2. Đóng gói Request
            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId("amazon.titan-embed-text-v2:0")
                    .contentType("application/json")
                    .accept("application/json")
                    .body(SdkBytes.fromUtf8String(payload))
                    .build();

            // 3. Gọi API và nhận kết quả
            InvokeModelResponse response = bedrockClient.invokeModel(request);

            // 4. Bóc tách file JSON trả về để lấy mảng Vector
            String responseBody = response.body().asUtf8String();
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode embeddingNode = rootNode.get("embedding");

            // Chuyển đổi JSON Array thành mảng float[] của Java
            float[] vector = new float[embeddingNode.size()];
            for (int i = 0; i < embeddingNode.size(); i++) {
                vector[i] = (float) embeddingNode.get(i).asDouble();
            }

            return vector;

        } catch (Exception e) {
            log.error("Lỗi khi gọi Amazon Titan Embedding API: {}", e.getMessage());
            // Trả về mảng rỗng hoặc ném lỗi tùy chiến lược, ở đây ta ném lỗi để VectorStoreService bắt được
            throw new RuntimeException("Không thể tạo vector cho khối mã nguồn này", e);
        }
    }
}