package com.zerobug_agent.storage;

import com.zerobug_agent.config.ZeroBugAgentProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Component
@ConditionalOnProperty(name = "zerobug.storage.type", havingValue = "s3")
@RequiredArgsConstructor
public class S3ProjectStorage implements ProjectStorage {

    private final S3Client s3Client;
    private final ZeroBugAgentProperties properties;

    @Override
    public String buildStorageKey(Long userId, String projectId) {
        return "users/" + userId + "/" + projectId + "/";
    }

    @Override
    public void storeFromDirectory(Path contentRoot, String storageKey) throws IOException {
        String bucket = bucket();
        String prefix = normalizePrefix(storageKey);

        try (Stream<Path> walk = Files.walk(contentRoot)) {
            walk.filter(Files::isRegularFile).forEach(path -> {
                String relative = contentRoot.relativize(path).toString().replace('\\', '/');
                String key = prefix + relative;
                s3Client.putObject(
                        PutObjectRequest.builder().bucket(bucket).key(key).build(),
                        RequestBody.fromFile(path));
            });
        }
    }

    @Override
    public void deleteAll(String storageKey) throws IOException {
        String bucket = bucket();
        String prefix = normalizePrefix(storageKey);
        String continuationToken = null;

        do {
            ListObjectsV2Request.Builder request = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix(prefix);
            if (continuationToken != null) {
                request.continuationToken(continuationToken);
            }

            ListObjectsV2Response response = s3Client.listObjectsV2(request.build());
            if (!response.contents().isEmpty()) {
                List<ObjectIdentifier> objects = response.contents().stream()
                        .map(obj -> ObjectIdentifier.builder().key(obj.key()).build())
                        .toList();
                s3Client.deleteObjects(DeleteObjectsRequest.builder()
                        .bucket(bucket)
                        .delete(Delete.builder().objects(objects).build())
                        .build());
            }
            continuationToken = response.isTruncated() ? response.nextContinuationToken() : null;
        } while (continuationToken != null);
    }

    @Override
    public boolean exists(String storageKey) throws IOException {
        String bucket = bucket();
        String prefix = normalizePrefix(storageKey);
        ListObjectsV2Response response = s3Client.listObjectsV2(ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix)
                .maxKeys(1)
                .build());
        return !response.contents().isEmpty() || !response.commonPrefixes().isEmpty();
    }

    @Override
    public List<String> listRelativeFilePaths(String storageKey, Predicate<String> filter) throws IOException {
        String bucket = bucket();
        String prefix = normalizePrefix(storageKey);
        List<String> paths = new ArrayList<>();
        String continuationToken = null;

        do {
            ListObjectsV2Request.Builder request = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix(prefix);
            if (continuationToken != null) {
                request.continuationToken(continuationToken);
            }

            ListObjectsV2Response response = s3Client.listObjectsV2(request.build());
            for (S3Object object : response.contents()) {
                if (object.key().endsWith("/")) {
                    continue;
                }
                String relative = object.key().substring(prefix.length());
                if (!relative.isBlank() && filter.test(relative)) {
                    paths.add(relative);
                }
            }
            continuationToken = response.isTruncated() ? response.nextContinuationToken() : null;
        } while (continuationToken != null);

        return paths.stream().sorted().toList();
    }

    @Override
    public String readFileContent(String storageKey, String relativePath) throws IOException {
        String normalizedRelative = relativePath.replace('\\', '/');
        if (normalizedRelative.contains("..")) {
            throw new IOException("Đường dẫn không hợp lệ");
        }

        String key = normalizePrefix(storageKey) + normalizedRelative;
        try {
            return s3Client.getObjectAsBytes(GetObjectRequest.builder()
                            .bucket(bucket())
                            .key(key)
                            .build())
                    .asString(StandardCharsets.UTF_8);
        } catch (NoSuchKeyException e) {
            throw new IOException("File không tồn tại");
        }
    }

    private String bucket() {
        String bucket = properties.getStorage().getS3Bucket();
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("S3 bucket chưa được cấu hình (zerobug.storage.s3-bucket)");
        }
        return bucket;
    }

    private String normalizePrefix(String storageKey) {
        return storageKey.endsWith("/") ? storageKey : storageKey + "/";
    }
}
