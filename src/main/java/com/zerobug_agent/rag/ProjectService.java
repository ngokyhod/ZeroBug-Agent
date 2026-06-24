package com.zerobug_agent.rag;

import com.zerobug_agent.config.ZeroBugAgentProperties;
import com.zerobug_agent.entity.Project;
import com.zerobug_agent.entity.SourceType;
import com.zerobug_agent.entity.User;
import com.zerobug_agent.entity.UserRole;
import com.zerobug_agent.repository.ProjectRepository;
import com.zerobug_agent.storage.ProjectStorage;
import org.eclipse.jgit.api.Git;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ZeroBugAgentProperties properties;
    private final ProjectStorage projectStorage;
    private final VectorStoreService vectorStoreService;

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    public ProjectService(ProjectRepository projectRepository, ZeroBugAgentProperties properties,
                          ProjectStorage projectStorage,
                          VectorStoreService vectorStoreService) {
        this.projectRepository = projectRepository;
        this.properties = properties;
        this.projectStorage = projectStorage;
        this.vectorStoreService = vectorStoreService;
    }

    @Transactional
    public Project createFromGit(User user, String gitUrl, String projectName) throws Exception {
        String projectId = UUID.randomUUID().toString();
        String storageKey = projectStorage.buildStorageKey(user.getId(), projectId);
        Path workDir = createWorkDirectory(projectId);

        try {
            String normalizedUrl = normalizeGitUrl(gitUrl);
            log.info("Cloning Git repository from {} to {} (shallow)", normalizedUrl, workDir);

            Git.cloneRepository()
                    .setURI(normalizedUrl)
                    .setDirectory(workDir.toFile())
                    .setCloneAllBranches(false)
                    .setDepth(1)
                    .call()
                    .close();

            removeGitMetadata(workDir);

            Path contentRoot = resolveContentRoot(workDir);
            validateHasSourceFiles(contentRoot);
            projectStorage.storeFromDirectory(contentRoot, storageKey);

            Project project = Project.builder()
                    .user(user)
                    .name(projectName != null && !projectName.isBlank() ? projectName.trim() : extractRepoName(gitUrl))
                    .sourceType(SourceType.GIT)
                    .gitUrl(normalizedUrl)
                    .storagePath(storageKey)
                    .build();

            log.info("Git project imported: {} files, storageKey={}", countSourceFiles(contentRoot), storageKey);
            Project savedProject = projectRepository.save(project);

            indexProjectFiles(savedProject.getId(), contentRoot);

            return savedProject;
        } catch (Exception e) {
            throw translateImportError(e);
        } finally {
            deleteDirectoryQuietly(workDir);
        }
    }

    @Transactional
    public Project createFromZip(User user, MultipartFile zipFile, String projectName) throws Exception {
        String projectId = UUID.randomUUID().toString();
        String storageKey = projectStorage.buildStorageKey(user.getId(), projectId);
        Path workDir = createWorkDirectory(projectId);

        try {
            unzip(zipFile.getInputStream(), workDir);

            Path contentRoot = resolveContentRoot(workDir);
            validateHasSourceFiles(contentRoot);
            projectStorage.storeFromDirectory(contentRoot, storageKey);

            Project project = Project.builder()
                    .user(user)
                    .name(projectName != null && !projectName.isBlank() ? projectName.trim() : stripExtension(zipFile.getOriginalFilename()))
                    .sourceType(SourceType.ZIP)
                    .storagePath(storageKey)
                    .build();

            log.info("Zip project imported: {} files, storageKey={}", countSourceFiles(contentRoot), storageKey);
            Project savedProject = projectRepository.save(project);

            indexProjectFiles(savedProject.getId(), contentRoot);

            return savedProject;
        } catch (Exception e) {
            throw translateImportError(e);
        } finally {
            deleteDirectoryQuietly(workDir);
        }
    }

    public String getStorageKey(Project project) {
        String storageKey = project.getStoragePath();
        try {
            if (!projectStorage.exists(storageKey)) {
                throw new IllegalStateException("Dữ liệu dự án không tồn tại trên server: " + storageKey);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Không thể đọc dữ liệu dự án: " + e.getMessage(), e);
        }
        return storageKey;
    }

    public Project getProjectForUser(Long projectId, User user) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dự án"));

        if (!project.getUser().getId().equals(user.getId()) && user.getRole() == UserRole.USER) {
            throw new IllegalArgumentException("Bạn không có quyền truy cập dự án này");
        }
        return project;
    }

    public void deleteProject(Project project) throws IOException {
        projectStorage.deleteAll(project.getStoragePath());
        projectRepository.delete(project);
    }

    private Path createWorkDirectory(String projectId) throws IOException {
        Path importRoot = Paths.get(properties.getStorage().getPath(), "import-tmp").toAbsolutePath().normalize();
        Files.createDirectories(importRoot);
        Path base = importRoot.resolve(projectId);
        Files.createDirectories(base);
        return base;
    }

    private void removeGitMetadata(Path workDir) {
        Path gitDir = workDir.resolve(".git");
        deleteDirectoryQuietly(gitDir);
    }

    private Exception translateImportError(Exception e) {
        String message = rootMessage(e).toLowerCase();
        if (message.contains("disk quota exceeded")
                || message.contains("no space left")
                || message.contains("nospc")) {
            return new IOException(
                    "Ổ đĩa EC2 đầy khi import (Git clone tạm trên server). "
                            + "Thử Upload Zip thay Git, hoặc admin SSH vào EC2 xóa thư mục import-tmp và mở rộng ổ EBS. "
                            + "Chi tiết: " + rootMessage(e), e);
        }
        return e;
    }

    private String rootMessage(Throwable e) {
        Throwable cause = e;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause.getMessage() != null ? cause.getMessage() : e.getClass().getSimpleName();
    }

    private void unzip(InputStream inputStream, Path targetDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path resolved = targetDir.resolve(entry.getName()).normalize();
                if (!resolved.startsWith(targetDir)) {
                    throw new IOException("Zip entry ngoài thư mục đích");
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(resolved);
                } else {
                    Files.createDirectories(resolved.getParent());
                    Files.copy(zis, resolved, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
    }

    private String extractRepoName(String gitUrl) {
        String name = gitUrl.replaceAll("\\.git$", "");
        int idx = Math.max(name.lastIndexOf('/'), name.lastIndexOf(':'));
        return idx >= 0 ? name.substring(idx + 1) : name;
    }

    private String stripExtension(String filename) {
        if (filename == null || filename.isBlank()) return "Uploaded Project";
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }

    Path resolveContentRoot(Path root) throws IOException {
        Path current = root.toAbsolutePath().normalize();
        if (!Files.isDirectory(current)) {
            return current;
        }

        for (int depth = 0; depth < 3; depth++) {
            List<Path> entries;
            try (Stream<Path> stream = Files.list(current)) {
                entries = stream
                        .filter(p -> !".git".equals(p.getFileName().toString()))
                        .toList();
            }

            if (entries.isEmpty()) {
                return current;
            }

            long fileCount = entries.stream().filter(Files::isRegularFile).count();
            if (fileCount > 0) {
                return current;
            }

            if (entries.size() == 1 && Files.isDirectory(entries.get(0))) {
                current = entries.get(0);
                continue;
            }

            return current;
        }

        return current;
    }

    private void validateHasSourceFiles(Path root) throws IOException {
        if (countSourceFiles(root) == 0) {
            throw new IOException("Không tìm thấy file source code sau khi import. Hãy kiểm tra link Git hoặc file zip.");
        }
    }

    private long countSourceFiles(Path root) throws IOException {
        if (!Files.exists(root)) return 0;
        try (Stream<Path> walk = Files.walk(root)) {
            return walk.filter(Files::isRegularFile)
                    .filter(p -> !p.toString().replace('\\', '/').contains("/.git/"))
                    .count();
        }
    }

    private String normalizeGitUrl(String gitUrl) {
        String url = gitUrl.trim();
        if (url.isEmpty()) {
            throw new IllegalArgumentException("Git URL không được để trống");
        }
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        if (!url.endsWith(".git") && (url.contains("github.com") || url.contains("gitlab.com") || url.contains("bitbucket.org"))) {
            url = url + ".git";
        }
        return url;
    }

    private void deleteDirectoryQuietly(Path path) {
        if (path == null || !Files.exists(path)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException e) {
                    log.warn("Could not delete temp path {}: {}", p, e.getMessage());
                }
            });
        } catch (IOException e) {
            log.warn("Could not clean temp directory {}: {}", path, e.getMessage());
        }
    }

    private void indexProjectFiles(Long projectId, Path contentRoot) {
        log.info("Bắt đầu lập chỉ mục (indexing) các file mã nguồn từ: {}", contentRoot);
        try (Stream<Path> walk = Files.walk(contentRoot)) {
            walk.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(javaFile -> {
                        try {
                            String relativePath = contentRoot.relativize(javaFile).toString().replace('\\', '/');
                            String content = Files.readString(javaFile);
                            vectorStoreService.processAndStoreFile(projectId, relativePath, content);
                        } catch (Exception e) {
                            log.error("Không thể đọc hoặc xử lý file để indexing: {}", javaFile, e);
                        }
                    });
            log.info("Hoàn tất indexing cho các file trong: {}", contentRoot);
        } catch (IOException e) {
            log.error("Lỗi nghiêm trọng khi duyệt cây thư mục để indexing từ: {}", contentRoot, e);
        }
    }
}
