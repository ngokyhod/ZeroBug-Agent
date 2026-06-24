package com.zerobug_agent.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Stream;

@Component
public class ImportWorkDirCleanup {

    private final ZeroBugAgentProperties properties;

    private static final Logger log = LoggerFactory.getLogger(ImportWorkDirCleanup.class);

    public ImportWorkDirCleanup(ZeroBugAgentProperties properties) {
        this.properties = properties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void cleanupStaleImportDirs() {
        Path importRoot = Paths.get(properties.getStorage().getPath(), "import-tmp");
        if (!Files.isDirectory(importRoot)) {
            return;
        }

        try (Stream<Path> entries = Files.list(importRoot)) {
            entries.filter(Files::isDirectory).forEach(this::deleteDirectoryQuietly);
            log.info("Cleaned stale Git/Zip import temp folders under {}", importRoot.toAbsolutePath());
        } catch (IOException e) {
            log.warn("Could not clean import temp directory {}: {}", importRoot, e.getMessage());
        }
    }

    private void deleteDirectoryQuietly(Path path) {
        try (Stream<Path> walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                    // best effort
                }
            });
        } catch (IOException e) {
            log.warn("Could not delete import temp path {}: {}", path, e.getMessage());
        }
    }
}
