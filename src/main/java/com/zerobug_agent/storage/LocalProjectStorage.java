package com.zerobug_agent.storage;

import com.zerobug_agent.config.ZeroBugAgentProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Component
@ConditionalOnProperty(name = "zerobug.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalProjectStorage implements ProjectStorage {

    private final ZeroBugAgentProperties properties;

    public LocalProjectStorage(ZeroBugAgentProperties properties) {
        this.properties = properties;
    }

    @Override
    public String buildStorageKey(Long userId, String projectId) {
        return Paths.get(properties.getStorage().getPath(), String.valueOf(userId), projectId)
                .toAbsolutePath()
                .normalize()
                .toString();
    }

    @Override
    public void storeFromDirectory(Path contentRoot, String storageKey) throws IOException {
        Path target = Paths.get(storageKey).toAbsolutePath().normalize();
        if (contentRoot.toAbsolutePath().normalize().equals(target)) {
            return;
        }
        Files.createDirectories(target.getParent());
        if (Files.exists(target)) {
            deleteDirectory(target);
        }
        copyDirectory(contentRoot, target);
    }

    @Override
    public void deleteAll(String storageKey) throws IOException {
        Path path = Paths.get(storageKey);
        if (Files.exists(path)) {
            deleteDirectory(path);
        }
    }

    @Override
    public boolean exists(String storageKey) throws IOException {
        return Files.isDirectory(Paths.get(storageKey));
    }

    @Override
    public List<String> listRelativeFilePaths(String storageKey, Predicate<String> filter) throws IOException {
        Path root = Paths.get(storageKey);
        if (!Files.exists(root)) {
            return List.of();
        }
        try (Stream<Path> walk = Files.walk(root)) {
            return walk.filter(Files::isRegularFile)
                    .map(path -> root.relativize(path).toString().replace('\\', '/'))
                    .filter(filter)
                    .sorted()
                    .toList();
        }
    }

    @Override
    public String readFileContent(String storageKey, String relativePath) throws IOException {
        Path root = Paths.get(storageKey).toAbsolutePath().normalize();
        Path resolved = root.resolve(relativePath).normalize();
        if (!resolved.startsWith(root)) {
            throw new IOException("Đường dẫn không hợp lệ");
        }
        if (!Files.exists(resolved) || Files.isDirectory(resolved)) {
            throw new IOException("File không tồn tại");
        }
        return Files.readString(resolved, StandardCharsets.UTF_8);
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        try (Stream<Path> walk = Files.walk(source)) {
            for (Path path : walk.sorted().toList()) {
                Path dest = target.resolve(source.relativize(path));
                if (Files.isDirectory(path)) {
                    Files.createDirectories(dest);
                } else {
                    Files.createDirectories(dest.getParent());
                    Files.copy(path, dest, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private void deleteDirectory(Path path) throws IOException {
        try (Stream<Path> walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
