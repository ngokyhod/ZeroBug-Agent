package com.zerobug_agent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zerobug_agent.storage.ProjectStorage;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

@Service
public class FileTreeService {

    private static final Set<String> IGNORED_DIRS = Set.of(
            ".git", "node_modules", "target", "build", "dist", ".idea", ".vscode", "__pycache__"
    );

    private final ProjectStorage projectStorage;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FileTreeService(ProjectStorage projectStorage) {
        this.projectStorage = projectStorage;
    }

    public ArrayNode buildFileTree(String storageKey) throws IOException {
        List<String> filePaths = projectStorage.listRelativeFilePaths(storageKey, path -> true);
        return buildTreeFromPaths(filePaths);
    }

    private ArrayNode buildTreeFromPaths(List<String> filePaths) {
        TreeNode root = new TreeNode("", true);
        for (String filePath : filePaths) {
            String[] parts = filePath.split("/");
            TreeNode current = root;
            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                boolean isFolder = i < parts.length - 1;
                current = current.children.computeIfAbsent(part, k -> new TreeNode(k, isFolder));
            }
        }

        ArrayNode tree = objectMapper.createArrayNode();
        root.children.values().stream()
                .sorted(Comparator.comparing(n -> n.name.toLowerCase()))
                .forEach(node -> tree.add(toJsonNode(node, "")));
        return tree;
    }

    private ObjectNode toJsonNode(TreeNode node, String parentPath) {
        ObjectNode json = objectMapper.createObjectNode();
        String relativePath = parentPath.isEmpty() ? node.name : parentPath + "/" + node.name;
        json.put("name", node.name);
        json.put("path", relativePath);

        if (node.directory) {
            json.put("type", "folder");
            if (IGNORED_DIRS.contains(node.name)) {
                json.put("ignored", true);
                json.set("children", objectMapper.createArrayNode());
                return json;
            }
            ArrayNode children = objectMapper.createArrayNode();
            node.children.values().stream()
                    .sorted(Comparator.comparing(n -> n.name.toLowerCase()))
                    .forEach(child -> children.add(toJsonNode(child, relativePath)));
            json.set("children", children);
        } else {
            json.put("type", "file");
            String ext = getExtension(node.name);
            json.put("language", mapLanguage(ext));
        }
        return json;
    }

    public String readFile(String storageKey, String relativePath) throws IOException {
        String content = projectStorage.readFileContent(storageKey, relativePath);
        if (content.length() > 512_000) {
            return "// File quá lớn để hiển thị (>512KB)\n";
        }
        return content;
    }

    public String collectRelevantSource(String storageKey, String requirements, int maxChars) throws IOException {
        String lowerReq = requirements.toLowerCase();
        Predicate<String> javaMainFilter = path ->
                path.endsWith(".java")
                        && !path.contains("/test/")
                        && !path.contains("\\test\\");

        List<String> javaFiles = new ArrayList<>(projectStorage.listRelativeFilePaths(storageKey, javaMainFilter));
        javaFiles.sort(Comparator.comparing(path -> relevanceScore(path, lowerReq), Comparator.reverseOrder()));

        StringBuilder sb = new StringBuilder();
        for (String relativePath : javaFiles) {
            String content = readFile(storageKey, relativePath);
            String block = "// File: " + relativePath + "\n" + content + "\n\n";
            if (sb.length() + block.length() > maxChars) break;
            sb.append(block);
        }
        return sb.toString();
    }

    private int relevanceScore(String path, String requirements) {
        String pathLower = path.toLowerCase();
        int score = 0;
        for (String token : requirements.split("\\W+")) {
            if (token.length() > 2 && pathLower.contains(token.toLowerCase())) {
                score += 10;
            }
        }
        if (pathLower.contains("service")) score += 5;
        if (pathLower.contains("controller")) score += 5;
        return score;
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1).toLowerCase() : "";
    }

    private String mapLanguage(String ext) {
        return switch (ext) {
            case "java" -> "java";
            case "js", "jsx" -> "javascript";
            case "ts", "tsx" -> "typescript";
            case "py" -> "python";
            case "go" -> "go";
            case "cs" -> "csharp";
            case "xml" -> "xml";
            case "json" -> "json";
            case "yml", "yaml" -> "yaml";
            case "html" -> "html";
            case "css" -> "css";
            case "sql" -> "sql";
            case "md" -> "markdown";
            default -> "plaintext";
        };
    }

    private static class TreeNode {
        final String name;
        final boolean directory;
        final Map<String, TreeNode> children = new LinkedHashMap<>();

        TreeNode(String name, boolean directory) {
            this.name = name;
            this.directory = directory;
        }
    }
}
