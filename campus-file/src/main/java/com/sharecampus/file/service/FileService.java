package com.sharecampus.file.service;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final MinioClient minioClient;
    @Value("${minio.bucket}") private String bucket;

    @PostConstruct
    public void init() {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception e) { log.warn("MinIO未连接，文件服务降级: {}", e.getMessage()); }
    }

    public String upload(MultipartFile file) throws Exception {
        String objectName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        try (InputStream is = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket).object(objectName)
                    .stream(is, file.getSize(), -1)
                    .contentType(file.getContentType()).build());
        }
        return "/file/" + objectName;
    }

    public InputStream download(String objectName) throws Exception {
        return minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(objectName).build());
    }
}
