package com.zerobug_agent.rag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunkEntity, Long> {

    /**
     * Tìm kiếm Hybrid: Lấy ra top K khối mã nguồn có ý nghĩa tương đồng nhất
     * Toán tử <=> trong pgvector đại diện cho Cosine Distance.
     */
    @Query(value = "SELECT * FROM document_chunks WHERE project_id = :projectId ORDER BY embedding <=> cast(:vector as vector) LIMIT :k", nativeQuery = true)
    List<DocumentChunkEntity> findSimilarChunks(
            @Param("projectId") Long projectId,
            @Param("vector") float[] vector, @Param("k") int k);
}