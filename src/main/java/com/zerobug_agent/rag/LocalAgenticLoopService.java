package com.zerobug_agent.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;

@Service
public class LocalAgenticLoopService {

    private static final Logger log = LoggerFactory.getLogger(LocalAgenticLoopService.class);
    
    // Gọi trực tiếp Service RAG đang nằm cùng project
    private final ZeroBugAgentService ragService;

    public LocalAgenticLoopService(ZeroBugAgentService ragService) {
        this.ragService = ragService;
    }

    public String executeSelfCorrectionLoop(String userQuery) {
        String currentQuery = userQuery;
        String finalCode = "";
        int maxAttempts = 3;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            log.info("🔄 [LOCAL RUN] Vòng lặp Agentic {}/{}: Đang gọi Bedrock viết code...", attempt, maxAttempts);

            // BƯỚC 1: Thực thi RAG cục bộ (Lấy Vector -> Gọi Titan -> Gọi Claude 3)
            finalCode = ragService.generateUnitTest(currentQuery);

            // BƯỚC 2: Lưu code ra file thực tế trên ổ cứng máy bạn
            saveCodeToFile(finalCode, "src/test/java/com/zerobug_agent/GeneratedTest.java");

            // BƯỚC 3: Kích hoạt Terminal Windows chạy ngầm lệnh Maven
            log.info("⚙️ Đang biên dịch và chạy thử Unit Test bằng Maven Local...");
            String testLog = runMavenTest();

            // BƯỚC 4: Phân tích Log và Tự phản xạ
            if (testLog.contains("BUILD SUCCESS")) {
                log.info("🎉 THÀNH CÔNG! Mã nguồn đã chuẩn ở lần thử thứ {}", attempt);
                return "ZeroBug Agent hoàn tất! Mã nguồn đã pass 100%:\n\n" + finalCode;
            } else {
                log.warn("❌ AI viết code bị lỗi (Lần {}). Bắt AI đọc log và sửa lại!", attempt);
                
                if (attempt == maxAttempts) {
                    return "AI đã thử 3 lần nhưng hệ thống không thể tự fix lỗi. Đây là log lỗi:\n" + testLog;
                }

                // Cập nhật lại Prompt, kẹp log lỗi vào mắng AI
                currentQuery = "Đoạn code Unit Test vừa rồi bị lỗi khi chạy. Log lỗi từ Maven đây:\n" + testLog + 
                               "\nHãy phân tích và viết lại file Unit Test chính xác hơn, không bị thiếu thư viện.";
            }
        }
        return finalCode;
    }

    private void saveCodeToFile(String code, String path) {
        try {
            File file = new File(path);
            file.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(code);
            }
        } catch (Exception e) {
            log.error("Lỗi khi lưu file test trên Local: {}", e.getMessage());
        }
    }

    private String runMavenTest() {
        StringBuilder output = new StringBuilder();
        try {
            // Chạy lệnh mvn test qua cmd của Windows
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "mvn test -Dtest=GeneratedTest");
            builder.redirectErrorStream(true); 
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
        } catch (Exception e) {
            return "LỖI HỆ THỐNG MAVEN LOCAL: " + e.getMessage();
        }
        return output.toString();
    }
}