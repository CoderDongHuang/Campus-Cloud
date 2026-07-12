package com.sharecampus.file.service;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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
        // 图片自动生成缩略图
        String contentType = file.getContentType();
        if (contentType != null && contentType.startsWith("image/")) {
            generateThumbnail(objectName);
        }
        return "/file/" + objectName;
    }

    /** 用 ImageMagick 生成缩略图 */
    private void generateThumbnail(String objectName) {
        try {
            String thumbName = objectName.replace(".", "_thumb.");
            java.io.File tmp = java.io.File.createTempFile("upload_", ".tmp");
            java.io.File thumb = java.io.File.createTempFile("thumb_", ".tmp");
            minioClient.downloadObject(DownloadObjectArgs.builder()
                    .bucket(bucket).object(objectName).filename(tmp.getAbsolutePath()).build());
            Process p = Runtime.getRuntime().exec(
                    new String[]{"magick", tmp.getAbsolutePath(), "-resize", "200x200", thumb.getAbsolutePath()});
            p.waitFor();
            minioClient.uploadObject(UploadObjectArgs.builder()
                    .bucket(bucket).object(thumbName).filename(thumb.getAbsolutePath()).build());
            tmp.delete(); thumb.delete();
            log.info("缩略图已生成: {}", thumbName);
        } catch (Exception e) {
            log.warn("缩略图生成失败: {}", objectName, e);
        }
    }

    public InputStream download(String objectName) throws Exception {
        return minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(objectName).build());
    }

    /** 生成前端直传预签名URL（有效期10分钟） */
    public String presignedUploadUrl(String objectName) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(io.minio.http.Method.PUT)
                        .bucket(bucket)
                        .object(objectName)
                        .expiry(10 * 60)
                        .build());
    }

    /** 分片上传——上传分片到临时路径 */
    public String uploadChunk(String objectName, int chunkIndex, MultipartFile chunk) throws Exception {
        String chunkObject = objectName + ".part." + String.format("%04d", chunkIndex);
        try (InputStream is = chunk.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket).object(chunkObject)
                    .stream(is, chunk.getSize(), -1).build());
        }
        return chunkObject;
    }

    /** 分片上传——合并所有分片 */
    public String mergeChunks(String objectName, int totalChunks) throws Exception {
        List<ComposeSource> sources = new java.util.ArrayList<>();
        for (int i = 0; i < totalChunks; i++) {
            sources.add(ComposeSource.builder()
                    .bucket(bucket)
                    .object(objectName + ".part." + String.format("%04d", i))
                    .build());
        }
        minioClient.composeObject(ComposeObjectArgs.builder()
                .bucket(bucket).object(objectName)
                .sources(sources).build());

        // 删除临时分片
        for (int i = 0; i < totalChunks; i++) {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName + ".part." + String.format("%04d", i))
                    .build());
        }
        return "/file/" + objectName;
    }
}
