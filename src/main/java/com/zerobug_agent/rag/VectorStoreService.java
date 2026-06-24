package com.zerobug_agent.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class VectorStoreService {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreService.class);

    private final CodeChunkingService chunkingService;
    private final EmbeddingService embeddingService;
    private final DocumentChunkRepository repository;

    // Inject các dependency thông qua Constructor
    public VectorStoreService(CodeChunkingService chunkingService,
                              EmbeddingService embeddingService,
                              DocumentChunkRepository repository) {
        this.chunkingService = chunkingService;
        this.embeddingService = embeddingService;
        this.repository = repository;
    }

    /**
     * Hàm chính: Xử lý một file mã nguồn và lưu xuống Vector Database
     */
    @Transactional
    public void processAndStoreFile(Long projectId, String filePath, String sourceCode) {
        log.info("Bắt đầu phân tích và nhúng file: {}", filePath);

        // 1. Cắt file thành các mảnh nhỏ (Chunks)
        List<DocumentChunk> chunks = chunkingService.chunkFile(filePath, sourceCode);
        log.info("Đã bóc tách thành {} khối mã nguồn (chunks).", chunks.size());

        // 2. Lấy Vector cho từng chunk song song để tăng tốc độ, truyền projectId vào
        List<DocumentChunkEntity> entitiesToSave = chunks.parallelStream()
                .map(chunk -> createEntityWithEmbedding(projectId, chunk))
                .filter(java.util.Objects::nonNull)
                .toList();

        // 3. Batch Insert toàn bộ xuống PostgreSQL
        if (!entitiesToSave.isEmpty()) {
            repository.saveAll(entitiesToSave);
            log.info("Đã lưu thành công {} chunks kèm Vector xuống PostgreSQL!", entitiesToSave.size());
        }
    }

    private DocumentChunkEntity createEntityWithEmbedding(Long projectId, DocumentChunk chunk) {
        try {
            float[] vector = embeddingService.getEmbedding(chunk.getContent());
            // Sử dụng một Mapper hoặc Builder để chuyển đổi gọn hơn
            return mapChunkToEntity(projectId, chunk, vector);
        } catch (Exception e) {
            log.error("Lỗi khi tạo Vector cho chunk [{} - {}]: {}", chunk.getFilePath(), chunk.getMethodName(), e.getMessage());
            return null; // Bỏ qua chunk lỗi
        }
    }

    // Helper method để chuyển đổi DTO -> Entity (bạn có thể dùng MapStruct cho việc này)
    private DocumentChunkEntity mapChunkToEntity(Long projectId, DocumentChunk chunk, float[] vector) {
        DocumentChunkEntity entity = new DocumentChunkEntity();
        entity.setProjectId(projectId);
        entity.setFilePath(chunk.getFilePath());
        entity.setPackageName(chunk.getPackageName());
        entity.setClassName(chunk.getClassName());
        entity.setMethodName(chunk.getMethodName());
        entity.setMethodSignature(chunk.getMethodSignature());
        entity.setContent(chunk.getContent());
        entity.setAnnotations(chunk.getAnnotations());
        entity.setDependencies(chunk.getDependencies());
        entity.setConstructors(chunk.getConstructors());
        entity.setStartLine(chunk.getStartLine());
        entity.setEndLine(chunk.getEndLine());
        entity.setEmbedding(vector);
        return entity;
    }
}