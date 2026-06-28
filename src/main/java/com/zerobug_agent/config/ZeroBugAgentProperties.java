package com.zerobug_agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConfigurationProperties(prefix = "zerobug")
@Primary
public class ZeroBugAgentProperties {

    private Admin admin = new Admin();
    private Storage storage = new Storage();
    private App app = new App();
    private Mail mail = new Mail();
    private Aws aws = new Aws();

    // --- GETTERS & SETTERS CHO CLASS CHÍNH ---
    public Admin getAdmin() { return admin; }
    public void setAdmin(Admin admin) { this.admin = admin; }
    public Storage getStorage() { return storage; }
    public void setStorage(Storage storage) { this.storage = storage; }
    public App getApp() { return app; }
    public void setApp(App app) { this.app = app; }
    public Mail getMail() { return mail; }
    public void setMail(Mail mail) { this.mail = mail; }
    public Aws getAws() { return aws; }
    public void setAws(Aws aws) { this.aws = aws; }

    // --- CÁC CLASS CON ---

    public static class Admin {
        private String email;
        private String password;
        private String fullName;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
    }

    public static class Storage {
        private String type = "local";
        private String path;
        private String s3Bucket;
        private String s3Region;

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        public String getS3Bucket() { return s3Bucket; }
        public void setS3Bucket(String s3Bucket) { this.s3Bucket = s3Bucket; }
        public String getS3Region() { return s3Region; }
        public void setS3Region(String s3Region) { this.s3Region = s3Region; }
    }

    public static class App {
        private String baseUrl;

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    }

    public static class Mail {
        private boolean devMode;

        public boolean isDevMode() { return devMode; }
        public void setDevMode(boolean devMode) { this.devMode = devMode; }
    }

    public static class Aws {
        private boolean enabled;
        private String region;
        private String bedrockRegion;
        private String knowledgeBaseId;
        private String dataSourceId;
        private String modelId;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public String getBedrockRegion() { return bedrockRegion; }
        public void setBedrockRegion(String bedrockRegion) { this.bedrockRegion = bedrockRegion; }
        public String getKnowledgeBaseId() { return knowledgeBaseId; }
        public void setKnowledgeBaseId(String knowledgeBaseId) { this.knowledgeBaseId = knowledgeBaseId; }
        public String getDataSourceId() { return dataSourceId; }
        public void setDataSourceId(String dataSourceId) { this.dataSourceId = dataSourceId; }
        public String getModelId() { return modelId; }
        public void setModelId(String modelId) { this.modelId = modelId; }
    }
}