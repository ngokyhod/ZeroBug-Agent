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
        int maxAttempts = 3;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            log.info("🔄 Vòng lặp Agentic {}/{}: Gọi AI viết code...", attempt, maxAttempts);

            // BƯỚC 1: Gọi Lambda trên mây đẻ code
            finalCode = orchestratorService.requestUnitTestFromCloud(currentQuery);

            // BƯỚC 2: Lưu code xuống ổ cứng của máy chủ EC2
            saveCodeToFile(finalCode, "src/test/java/com/zerobug_agent/GeneratedTest.java");

            // BƯỚC 3: Kích hoạt Terminal ngầm chạy lệnh Maven
            log.info("⚙️ Đang biên dịch và chạy thử bằng Maven...");
            String testLog = runMavenTest();

            // BƯỚC 4: Phân tích Log và Tự phản xạ
            if (testLog.contains("BUILD SUCCESS")) {
                log.info("🎉 THÀNH CÔNG TỰ ĐỘNG! Code đã chuẩn ở lần thử {}", attempt);
                return "ZeroBug Agent hoàn tất! Mã nguồn đã pass 100%:\n\n" + finalCode;
            } else {
                log.warn("❌ Cảnh báo: AI viết code bị lỗi (Lần {}). Đang bắt AI sửa lại!", attempt);
                
                if (attempt == maxAttempts) {
                    return "Đã thử 3 lần nhưng hệ thống không thể tự fix lỗi. Đây là log lỗi:\n" + testLog;
                }

                // Cập nhật lại câu hỏi: Mắng AI và đưa log lỗi cho nó đọc
                currentQuery = "Đoạn code Unit Test vừa rồi bị lỗi Compile/Test. Log lỗi đây:\n" + testLog + 
                               "\nHãy phân tích và viết lại chính xác hơn.";
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
            log.error("Lỗi khi lưu file test: {}", e.getMessage());
        }
    }

    private String runMavenTest() {
        StringBuilder output = new StringBuilder();
        try {
            // Lệnh cmd.exe dùng cho Windows Local. Khi deploy EC2 Linux thì đổi thành "sh", "-c"
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
            return "LỖI HỆ THỐNG MAVEN: " + e.getMessage();
        }
        return output.toString();
    }
}