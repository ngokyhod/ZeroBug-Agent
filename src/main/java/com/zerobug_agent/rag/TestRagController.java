package com.zerobug_agent.rag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test-rag")
public class TestRagController {

    private final VectorStoreService vectorStoreService;

    public TestRagController(VectorStoreService vectorStoreService) {
        this.vectorStoreService = vectorStoreService;
    }

    @PostMapping("/embed-file")
    public ResponseEntity<String> testEmbedFile(@RequestBody String sampleCode) {
        try {
            // Giả lập tên file
            Long dummyProjectId = 1L;
            String dummyFilePath = "src/main/java/com/example/UserService.java";
            
            // Gọi luồng xử lý chính: Cắt code -> Lấy Vector -> Lưu Database
            vectorStoreService.processAndStoreFile(dummyProjectId, dummyFilePath, sampleCode);
            
            return ResponseEntity.ok("Thành công! Đã cắt code, nhúng Vector và lưu xuống PostgreSQL.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi rồi: " + e.getMessage());
        }
    }
}