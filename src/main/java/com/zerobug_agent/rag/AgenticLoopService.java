package com.zerobug_agent.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;

@Service
public class AgenticLoopService {

    private static final Logger log = LoggerFactory.getLogger(AgenticLoopService.class);
    private final LambdaOrchestratorService orchestratorService;

    // Inject "Bộ đàm" gọi mây vào đây
    public AgenticLoopService(LambdaOrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    public String executeSelfCorrectionLoop(String initialQuery) {
        String currentQuery = initialQuery;
        String finalCode = "";
        String pureJavaCode = "";
        int maxAttempts = 3;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            log.info("🔄 Vòng lặp Agentic {}/{}: Gọi AI viết code...", attempt, maxAttempts);

            // BƯỚC 1: Gọi Lambda trên mây đẻ code
            finalCode = orchestratorService.requestUnitTestFromCloud(currentQuery);
            if (finalCode == null || !finalCode.contains("class ") && !finalCode.contains("@Test")) {
    log.error("AWS trả về lỗi, không phải mã nguồn! Nội dung: " + finalCode);
    return "Quy trình thất bại do lỗi từ Cloud: " + finalCode; // Ngắt luôn, không lưu file nữa
}
            // Lọc bỏ lời chào hỏi, chỉ lấy code Java thuần
            pureJavaCode = extractPureJavaCode(finalCode);

            // BƯỚC 2: Lưu code XỊN (pureJavaCode) xuống ổ cứng của máy chủ EC2
            // FIX: Truyền đúng pureJavaCode vào thay vì finalCode
            saveCodeToFile(pureJavaCode, "src/test/java/com/zerobug_agent/GeneratedTest.java");

            // BƯỚC 3: Kích hoạt Terminal ngầm chạy lệnh Maven
            log.info("⚙️ Đang biên dịch và chạy thử bằng Maven...");
            String testLog = runMavenTest();

            // BƯỚC 4: Phân tích Log và Tự phản xạ
            if (testLog.contains("BUILD SUCCESS")) {
                log.info("🎉 THÀNH CÔNG TỰ ĐỘNG! Code đã chuẩn ở lần thử {}", attempt);
                // Trả về pureJavaCode để Postman hiển thị code sạch đẹp
                return "ZeroBug Agent hoàn tất! Mã nguồn đã pass 100%:\n\n" + pureJavaCode;
            } else {
                log.warn("❌ Cảnh báo: AI viết code bị lỗi (Lần {}). Đang bắt AI sửa lại!", attempt);
                
                if (attempt == maxAttempts) {
                    return "Đã thử 3 lần nhưng hệ thống không thể tự fix lỗi. Đây là log lỗi:\n" + testLog;
                }

                // Cập nhật lại câu hỏi: Mắng AI và đưa log lỗi cho nó đọc
                // FIX: Ép AI không được giải thích lằng nhằng để tránh lỗi format
                currentQuery = "Đoạn code Unit Test vừa rồi bị lỗi Compile/Test. Log lỗi đây:\n" + testLog + 
                               "\nHãy phân tích và viết lại đoạn code chính xác hơn. KHÔNG ĐƯỢC GIẢI THÍCH, CHỈ TRẢ VỀ ĐOẠN CODE TRONG THẺ ```java";
            }
        }
        return pureJavaCode;
    }

    private void saveCodeToFile(String code, String path) {
        try {
            File file = new File(path);
            file.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(code);
            }
        } catch (Exception e) {
            log.error("Lỗi khi lưu file test: {}", e.getMessage());
        }
    }

    private String runMavenTest() {
        StringBuilder output = new StringBuilder();
        try {
            // FIX: Tự động nhận diện Hệ điều hành và gán đúng thư mục làm việc (Working Directory)
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder builder;
            
            if (os.contains("win")) {
                // Thêm .\\ để bắt buộc Windows chạy file trong thư mục hiện tại
                builder = new ProcessBuilder("cmd.exe", "/c", ".\\mvnw.cmd test -Dtest=GeneratedTest");
            } else {
                builder = new ProcessBuilder("sh", "-c", "./mvnw test -Dtest=GeneratedTest");
            }

            // FIX QUAN TRỌNG: Chỉ định chính xác thư mục gốc của Project để Maven tìm thấy file pom.xml
            builder.directory(new File(System.getProperty("user.dir")));
            builder.redirectErrorStream(true); 
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
        } catch (Exception e) {
            return "LỖI HỆ THỐNG MAVEN: " + e.getMessage();
        }
        return output.toString();
    }

    private String extractPureJavaCode(String aiResponse) {
        // Tìm block code markdown
        if (aiResponse.contains("```java")) {
            int start = aiResponse.indexOf("```java") + 7;
            int end = aiResponse.indexOf("```", start);
            if (end > start) {
                return aiResponse.substring(start, end).trim();
            }
        } else if (aiResponse.contains("```")) {
            int start = aiResponse.indexOf("```") + 3;
            int end = aiResponse.indexOf("```", start);
            if (end > start) {
                return aiResponse.substring(start, end).trim();
            }
        }
        return aiResponse.trim();
    }
}