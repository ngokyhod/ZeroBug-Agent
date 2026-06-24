package com.zerobug_agent.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

public interface ProjectStorage {

    String buildStorageKey(Long userId, String projectId);

    void storeFromDirectory(Path contentRoot, String storageKey) throws IOException;

    void deleteAll(String storageKey) throws IOException;

    boolean exists(String storageKey) throws IOException;

    List<String> listRelativeFilePaths(String storageKey, Predicate<String> filter) throws IOException;

    String readFileContent(String storageKey, String relativePath) throws IOException;
}
