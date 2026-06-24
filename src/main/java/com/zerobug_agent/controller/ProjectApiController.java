package com.zerobug_agent.controller;

import com.zerobug_agent.config.ZeroBugAgentProperties;
import com.zerobug_agent.dto.GenerationDto;
import com.zerobug_agent.dto.GitImportRequest;
import com.zerobug_agent.dto.GenerateTestRequest;
import com.zerobug_agent.dto.GenerationResultDto;
import com.zerobug_agent.dto.ProjectDto;
import com.zerobug_agent.entity.GenerationRecord;
import com.zerobug_agent.entity.Project;
import com.zerobug_agent.entity.User;
import com.zerobug_agent.entity.UserRole;
import com.zerobug_agent.rag.ProjectService;
import com.zerobug_agent.repository.GenerationRecordRepository;
import com.zerobug_agent.repository.ProjectRepository;
import com.zerobug_agent.service.FileTreeService;
import com.zerobug_agent.service.TestGenerationService;
import com.zerobug_agent.service.UserService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProjectApiController {

    private final UserService userService;
    private final ProjectService projectService;
    private final ProjectRepository projectRepository;
    private final GenerationRecordRepository generationRecordRepository;
    private final ZeroBugAgentProperties properties;
    private final FileTreeService fileTreeService;
    private final TestGenerationService testGenerationService;

    public ProjectApiController(UserService userService,
                                ProjectService projectService,
                                ProjectRepository projectRepository,
                                GenerationRecordRepository generationRecordRepository,
                                ZeroBugAgentProperties properties,
                                FileTreeService fileTreeService,
                                TestGenerationService testGenerationService) {
        this.userService = userService;
        this.projectService = projectService;
        this.projectRepository = projectRepository;
        this.generationRecordRepository = generationRecordRepository;
        this.properties = properties;
        this.fileTreeService = fileTreeService;
        this.testGenerationService = testGenerationService;
    }

    @GetMapping("/projects")
    public ResponseEntity<List<ProjectDto>> listProjects(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        List<Project> projects = user.getRole() == UserRole.ADMIN
                ? projectRepository.findAllByOrderByCreatedAtDesc()
                : projectRepository.findByUserOrderByCreatedAtDesc(user);
        return ResponseEntity.ok(projects.stream().map(ProjectDto::from).toList());
    }

    @GetMapping("/projects/{projectId}")
    public ResponseEntity<?> getProject(@AuthenticationPrincipal UserDetails userDetails,
                                          @PathVariable Long projectId) {
        try {
            User user = getCurrentUser(userDetails);
            Project project = projectService.getProjectForUser(projectId, user);
            return ResponseEntity.ok(ProjectDto.from(project));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/projects/import/git")
    public ResponseEntity<?> importGit(@AuthenticationPrincipal UserDetails userDetails,
                                       @Valid @RequestBody GitImportRequest request) {
        try {
            User user = getCurrentUser(userDetails);
            Project project = projectService.createFromGit(user, request.getGitUrl(), request.getProjectName());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", importSuccessMessage("Import Git"),
                    "project", ProjectDto.from(project)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", importFailureMessage("Git", e.getMessage())));
        }
    }

    @PostMapping("/projects/import/zip")
    public ResponseEntity<?> importZip(@AuthenticationPrincipal UserDetails userDetails,
                                       @RequestParam("zipFile") MultipartFile zipFile,
                                       @RequestParam(value = "projectName", required = false) String projectName) {
        try {
            User user = getCurrentUser(userDetails);
            Project project = projectService.createFromZip(user, zipFile, projectName);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", importSuccessMessage("Upload Zip"),
                    "project", ProjectDto.from(project)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", importFailureMessage("Zip", e.getMessage())));
        }
    }

    @DeleteMapping("/projects/{projectId}")
    public ResponseEntity<?> deleteProject(@AuthenticationPrincipal UserDetails userDetails,
                                           @PathVariable Long projectId) {
        try {
            User user = getCurrentUser(userDetails);
            Project project = projectService.getProjectForUser(projectId, user);
            projectService.deleteProject(project);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", deleteSuccessMessage()));
        } catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Xóa dự án thất bại: " + e.getMessage()));
        }
    }

    @GetMapping("/generations/recent")
    @Transactional(readOnly = true)
    public ResponseEntity<List<GenerationDto>> recentGenerations(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "5") int limit) {
        User user = getCurrentUser(userDetails);
        List<GenerationRecord> records = generationRecordRepository.findByUserOrderByCreatedAtDesc(user);
        return ResponseEntity.ok(records.stream()
                .limit(Math.max(1, Math.min(limit, 20)))
                .map(GenerationDto::from)
                .toList());
    }

    @GetMapping("/projects/{projectId}/files")
    public ResponseEntity<?> getFileTree(@AuthenticationPrincipal UserDetails userDetails,
                                         @PathVariable Long projectId) {
        try {
            User user = getCurrentUser(userDetails);
            Project project = projectService.getProjectForUser(projectId, user);
            return ResponseEntity.ok(fileTreeService.buildFileTree(projectService.getStorageKey(project)));
        } catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Không thể tạo cây thư mục: " + e.getMessage()));
        }
    }

    @GetMapping("/projects/{projectId}/file")
    public ResponseEntity<?> getFileContent(@AuthenticationPrincipal UserDetails userDetails,
                                              @PathVariable Long projectId,
                                              @RequestParam String path) {
        try {
            User user = getCurrentUser(userDetails);
            Project project = projectService.getProjectForUser(projectId, user);
            String content = fileTreeService.readFile(projectService.getStorageKey(project), path);
            return ResponseEntity.ok(Map.of("path", path, "content", content));
        } catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/projects/{projectId}/generate")
    public ResponseEntity<?> generateTest(@AuthenticationPrincipal UserDetails userDetails,
                                          @PathVariable Long projectId,
                                          @Valid @RequestBody GenerateTestRequest request) {
        try {
            User user = getCurrentUser(userDetails);
            Project project = projectService.getProjectForUser(projectId, user);
            GenerationResultDto result = testGenerationService.generate(user, project, request.getRequirements());
            GenerationRecord record = result.getRecord();
            return ResponseEntity.ok(Map.of(
                    "id", record.getId(),
                    "response", record.getResponse(),
                    "createdAt", record.getCreatedAt().toString(),
                    "aiSource", result.getAiSource(),
                    "awsMessage", result.getAwsMessage(),
                    "success", true
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", e.getMessage(),
                    "success", false,
                    "awsMessage", e.getMessage() != null && e.getMessage().contains("Bedrock")
                            ? e.getMessage()
                            : "Lỗi khi sinh test: " + e.getMessage()
            ));
        }
    }

    private String importSuccessMessage(String action) {
        if ("s3".equalsIgnoreCase(properties.getStorage().getType())) {
            return action + " thành công — metadata đã lưu RDS, file source đã upload Amazon S3.";
        }
        return action + " thành công.";
    }

    private String importFailureMessage(String type, String detail) {
        if ("s3".equalsIgnoreCase(properties.getStorage().getType())) {
            return "Import " + type + " thất bại (RDS/S3): " + detail;
        }
        return "Import " + type + " thất bại: " + detail;
    }

    private String deleteSuccessMessage() {
        if ("s3".equalsIgnoreCase(properties.getStorage().getType())) {
            return "Đã xóa dự án — gỡ metadata RDS và file trên Amazon S3.";
        }
        return "Đã xóa dự án.";
    }

    private User getCurrentUser(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername());
    }
}
