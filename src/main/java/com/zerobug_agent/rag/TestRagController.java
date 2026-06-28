package com.zerobug_agent.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/test-rag")
public class TestRagController {

    private static final Logger log = LoggerFactory.getLogger(TestRagController.class);
    private final AgenticLoopService agenticLoopService;

    public TestRagController(AgenticLoopService agenticLoopService) {
        this.agenticLoopService = agenticLoopService;
    }

    @PostMapping("/generate")
    public ResponseEntity<String> generateTest(@RequestBody Map<String, String> payload) {
        try {
            // Lấy câu hỏi từ JSON body
            String userQuery = payload.get("query");
            
            log.info("Nhận yêu cầu: " + userQuery); 
            // Có thể mở rộng truyền thêm projectId hoặc fileName vào hàm này sau
            String result = agenticLoopService.executeSelfCorrectionLoop(userQuery);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Lỗi Controller: ", e);
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }
}