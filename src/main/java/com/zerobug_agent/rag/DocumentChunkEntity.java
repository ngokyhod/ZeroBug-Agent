package com.zerobug_agent.rag;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.List;

@Entity
@Table(name = "document_chunks")
public class DocumentChunkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "file_path", length = 1024)
    private String filePath;

    @Column(name = "package_name")
    private String packageName;

    @Column(name = "class_name")
    private String className;

    @Column(name = "method_name")
    private String methodName;

    @Column(name = "method_signature", length = 2000)
    private String methodSignature;

    @Column(columnDefinition = "TEXT")
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "annotations", columnDefinition = "jsonb")
    private List<String> annotations;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dependencies", columnDefinition = "jsonb")
    private List<String> dependencies;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "constructors", columnDefinition = "jsonb")
    private List<String> constructors;

    @Column(name = "start_line")
    private int startLine;

    @Column(name = "end_line")
    private int endLine;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Column(columnDefinition = "vector(1024)")
    private float[] embedding;

    // --- BẮT ĐẦU PHẦN CONSTRUCTOR & GETTER/SETTER THỦ CÔNG ---

    public DocumentChunkEntity() {
        // Constructor rỗng bắt buộc cho Hibernate
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getMethodName() { return methodName; }
    public void setMethodName(String methodName) { this.methodName = methodName; }

    public String getMethodSignature() { return methodSignature; }
    public void setMethodSignature(String methodSignature) { this.methodSignature = methodSignature; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<String> getAnnotations() { return annotations; }
    public void setAnnotations(List<String> annotations) { this.annotations = annotations; }

    public List<String> getDependencies() { return dependencies; }
    public void setDependencies(List<String> dependencies) { this.dependencies = dependencies; }

    public List<String> getConstructors() { return constructors; }
    public void setConstructors(List<String> constructors) { this.constructors = constructors; }

    public int getStartLine() { return startLine; }
    public void setStartLine(int startLine) { this.startLine = startLine; }

    public int getEndLine() { return endLine; }
    public void setEndLine(int endLine) { this.endLine = endLine; }

    public float[] getEmbedding() { return embedding; }
    public void setEmbedding(float[] embedding) { this.embedding = embedding; }
}