package com.zerobug_agent.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "document_chunks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentChunkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_path", length = 1024)
    private String filePath;

    @Column(name = "package_name")
    private String packageName;

    @Column(name = "class_name")
    private String className;

    @Column(name = "method_name")
    private String methodName;

    @Column(name = "method_signature", length = 1024)
    private String methodSignature;

    @Column(name = "annotations")
    private String annotations; // Có thể lưu dưới dạng JSON String

    @Column(name = "dependencies")
    private String dependencies; // Lưu danh sách Dependency (Field Type)

    @Column(name = "constructors")
    private String constructors; // Lưu dạng chuỗi hoặc JSON

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "start_line")
    private int startLine;

    @Column(name = "end_line")
    private int endLine;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Column(columnDefinition = "vector(1024)")
    private float[] embedding;
}