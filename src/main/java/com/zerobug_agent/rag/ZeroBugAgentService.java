package com.zerobug_agent.rag;
import org.json.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.util.List;

@Service
public class ZeroBugAgentService {

    private static final Logger log = LoggerFactory.getLogger(ZeroBugAgentService.class);

    private final EmbeddingService embeddingService;
    private final DocumentChunkRepository repository;
    private final BedrockRuntimeClient bedrockClient;

    public ZeroBugAgentService(EmbeddingService embeddingService, 
                               DocumentChunkRepository repository,
                               @Value("${zerobug.aws.region:us-east-1}") String region) {
        this.embeddingService = embeddingService;
        this.repository = repository;
        this.bedrockClient = BedrockRuntimeClient.builder()
                .region(Region.of(region))
                .build();
    }

    /**
     * Hàm chạy RAG ngay tại Local: Tạo Vector -> Tìm Database -> Gọi AI
     */
    public String generateUnitTest(String userQuery) {
        log.info("1. Bắt đầu xử lý RAG Local cho yêu cầu: {}", userQuery);

        // [RETRIEVAL]
        float[] queryVector = embeddingService.getEmbedding(userQuery);
        // Dòng mới: Truyền tạm ID dự án số 1 (1L) vào để test Local
List<DocumentChunkEntity> contextChunks = repository.findSimilarChunks(1L, queryVector, 3);

        if (contextChunks.isEmpty()) {
            return "Không tìm thấy mã nguồn nào phù hợp trong Database PostgreSQL để viết test.";
        }

        // [AUGMENTED]
        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("Đây là các đoạn mã nguồn (Context) được trích xuất từ hệ thống:\n\n");
        for (DocumentChunkEntity chunk : contextChunks) {
            contextBuilder.append("--- File: ").append(chunk.getFilePath()).append(" ---\n");
            contextBuilder.append(chunk.getContent()).append("\n\n");
        }

        String finalPrompt = String.format(
            "Bạn là một chuyên gia kiểm thử phần mềm (QA/Tester) cấp cao Java. \n" +
            "%s\n" +
            "Dựa NHẤT QUÁN vào các đoạn mã nguồn trên, hãy thực hiện yêu cầu sau: %s\n" +
            "Chỉ trả về mã nguồn Unit Test (dùng JUnit 5 và Mockito), không cần giải thích.", 
            contextBuilder.toString(), userQuery
        );

        // [GENERATION]
        log.info("3. Đang gửi Context lên Claude 3 Haiku...");
        return callClaudeHaiku(finalPrompt);
    }

    private String callClaudeHaiku(String prompt) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("anthropic_version", "bedrock-2023-05-31");
            payload.put("max_tokens", 3000);
            
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", prompt);
            payload.put("messages", List.of(message));

            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId("anthropic.claude-3-haiku-20240307-v1:0")
                    .contentType("application/json")
                    .accept("application/json")
                    .body(SdkBytes.fromUtf8String(payload.toString()))
                    .build();

            InvokeModelResponse response = bedrockClient.invokeModel(request);
            
            JSONObject responseJson = new JSONObject(response.body().asUtf8String());
            return responseJson.getJSONArray("content").getJSONObject(0).getString("text");

        } catch (Exception e) {
            log.error("Lỗi khi gọi Claude 3 Haiku: {}", e.getMessage());
            return "LỖI AI BEDROCK LOCAL: " + e.getMessage();
        }
    }
}