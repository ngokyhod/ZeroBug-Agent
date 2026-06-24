package com.zerobug_agent.service;

import com.zerobug_agent.config.ZeroBugAgentProperties;
import com.zerobug_agent.dto.GenerationResultDto;
import com.zerobug_agent.entity.GenerationRecord;
import com.zerobug_agent.entity.Project;
import com.zerobug_agent.entity.User;
import com.zerobug_agent.rag.DocumentChunkEntity;
import com.zerobug_agent.rag.DocumentChunkRepository;
import com.zerobug_agent.rag.EmbeddingService;
import com.zerobug_agent.repository.GenerationRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TestGenerationService {

    private final ZeroBugAgentProperties properties;
    private final GenerationRecordRepository generationRecordRepository;
    private final EmbeddingService embeddingService;
    private final DocumentChunkRepository documentChunkRepository;

    private static final Logger log = LoggerFactory.getLogger(TestGenerationService.class);

    public TestGenerationService(ZeroBugAgentProperties properties,
                                 GenerationRecordRepository generationRecordRepository,
                                 EmbeddingService embeddingService,
                                 DocumentChunkRepository documentChunkRepository) {
        this.properties = properties;
        this.generationRecordRepository = generationRecordRepository;
        this.embeddingService = embeddingService;
        this.documentChunkRepository = documentChunkRepository;
    }

    @Transactional
    public GenerationResultDto generate(User user, Project project, String requirements) throws Exception {
        // --- BƯỚC 1: TRUY XUẤT NGỮ CẢNH BẰNG RAG ---
        log.info("Bắt đầu truy xuất ngữ cảnh (RAG) cho yêu cầu: '{}'", requirements);

        // 1.1. Chuyển yêu cầu của người dùng thành vector
        float[] requirementVector = embeddingService.getEmbedding(requirements);

        // 1.2. Tìm kiếm các chunk code tương đồng nhất trong DB (lấy top 3)
        List<DocumentChunkEntity> similarChunks = documentChunkRepository.findSimilarChunks(
                project.getId(), requirementVector, 3);
        log.info("Tìm thấy {} chunk code liên quan.", similarChunks.size());

        // 1.3. Ghép các chunk code thành một chuỗi ngữ cảnh duy nhất
        String sourceContext = similarChunks.stream()
                .map(DocumentChunkEntity::getContent)
                .collect(Collectors.joining("\n\n---\n\n"));

        // --- BƯỚC 2: GỌI AI ĐỂ SINH TEST ---
        String aiSource;
        String awsMessage;
        String response;

        if (properties.getAws().isEnabled()) {
            response = callBedrock(requirements, sourceContext);
            aiSource = "bedrock";
            awsMessage = "Amazon Bedrock sinh test thành công (model: "
                    + properties.getAws().getModelId() + ")";
        } else {
            response = generateMockResponse(requirements, sourceContext);
            aiSource = "mock";
            awsMessage = "Bedrock chưa bật — đang dùng mock response (AWS_ENABLED=false)";
        }

        GenerationRecord record = GenerationRecord.builder()
                .user(user)
                .project(project)
                .requirements(requirements)
                .response(response)
                .build();

        record = generationRecordRepository.save(record);

        return GenerationResultDto.builder()
                .record(record)
                .aiSource(aiSource)
                .awsMessage(awsMessage)
                .build();
    }

    private String callBedrock(String requirements, String sourceContext) {
        String prompt = buildPrompt(requirements, sourceContext);

        String bedrockRegion = properties.getAws().getBedrockRegion();
        if (bedrockRegion == null || bedrockRegion.isBlank()) {
            bedrockRegion = properties.getAws().getRegion();
        }

        try (BedrockRuntimeClient client = BedrockRuntimeClient.builder()
                .region(Region.of(bedrockRegion))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build()) {

            String requestBody = """
                {
                  "anthropic_version": "bedrock-2023-05-31",
                  "max_tokens": 4096,
                  "messages": [
                    {
                      "role": "user",
                      "content": %s
                    }
                  ]
                }
                """.formatted(escapeJson(prompt));

            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(properties.getAws().getModelId())
                    .body(SdkBytes.fromUtf8String(requestBody))
                    .contentType("application/json")
                    .accept("application/json")
                    .build();

            InvokeModelResponse response = client.invokeModel(request);
            String responseJson = response.body().asUtf8String();

            int contentStart = responseJson.indexOf("\"text\":\"") + 8;
            int contentEnd = responseJson.indexOf("\"", contentStart);
            if (contentStart > 7 && contentEnd > contentStart) {
                return unescapeJson(responseJson.substring(contentStart, contentEnd));
            }
            return responseJson;
        } catch (Exception e) {
            log.error("Amazon Bedrock failed: {}", e.getMessage());
            throw new IllegalStateException("Amazon Bedrock thất bại: " + e.getMessage(), e);
        }
    }

    private String generateMockResponse(String requirements, String sourceContext) {
        String className = extractClassName(sourceContext);
        return """
                // ZeroBug Agent - Mock Response
                // Yêu cầu: %s

                import org.junit.jupiter.api.*;
                import org.junit.jupiter.api.extension.ExtendWith;
                import org.mockito.*;
                import org.mockito.junit.jupiter.MockitoExtension;
                import static org.junit.jupiter.api.Assertions.*;
                import static org.mockito.Mockito.*;

                @ExtendWith(MockitoExtension.class)
                class %sTest {

                    @InjectMocks
                    private %s target;

                    @BeforeEach
                    void setUp() {
                        // Khởi tạo dữ liệu test
                    }

                    @Test
                    @DisplayName("Happy path - đáp ứng yêu cầu cơ bản")
                    void shouldFulfillBasicRequirement() {
                        // Arrange
                        // Act
                        // Assert
                        assertTrue(true, "Thay thế bằng assertion thực tế theo yêu cầu");
                    }

                    @Test
                    @DisplayName("Edge case - xử lý dữ liệu không hợp lệ")
                    void shouldHandleInvalidInput() {
                        assertThrows(IllegalArgumentException.class, () -> {
                            // Gọi method với input không hợp lệ
                        });
                    }
                }
                """.formatted(requirements.trim(), className, className);
    }

    private String buildPrompt(String requirements, String sourceContext) {
        return """
                Bạn là chuyên gia Java QA. Dựa trên source code và yêu cầu bên dưới, hãy viết class JUnit 5 hoàn chỉnh.
                Chỉ trả về code Java, không giải thích thêm. Sử dụng JUnit 5 và Mockito nếu cần.

                === YÊU CẦU ===
                %s

                === SOURCE CODE ===
                %s
                """.formatted(requirements, sourceContext);
    }

    private String extractClassName(String sourceContext) {
        for (String line : sourceContext.split("\n")) {
            line = line.trim();
            if (line.startsWith("public class ") || line.startsWith("class ")) {
                String[] parts = line.replace("{", "").split("\\s+");
                for (int i = 0; i < parts.length - 1; i++) {
                    if ("class".equals(parts[i])) {
                        return parts[i + 1];
                    }
                }
            }
        }
        return "Generated";
    }

    private String escapeJson(String text) {
        return "\"" + text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t") + "\"";
    }

    private String unescapeJson(String text) {
        return text.replace("\\n", "\n").replace("\\\"", "\"").replace("\\t", "\t");
    }
}
