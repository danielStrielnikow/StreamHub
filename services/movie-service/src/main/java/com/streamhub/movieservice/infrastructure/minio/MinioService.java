package com.streamhub.movieservice.infrastructure.minio;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.bucket.movies}")
    private String moviesBucket;

    @Value("${minio.bucket.thumbnails}")
    private String thumbnailsBucket;

    public record UploadResult(String url, String objectKey) {}

    @Async("uploadExecutor")
    public CompletableFuture<UploadResult> uploadVideo(String movieId, byte[] bytes, String contentType, String filename) {
        try {
            String objectKey = movieId + "/" + filename;
            String url = upload(moviesBucket, objectKey, bytes, contentType);
            return CompletableFuture.completedFuture(new UploadResult(url, objectKey));
        } catch (Exception e) {
            throw new RuntimeException("File upload failed", e);
        }
    }

    @Async("uploadExecutor")
    public CompletableFuture<UploadResult> uploadThumbnail(String movieId, byte[] bytes, String contentType, String filename) {
        try {
            String objectKey = movieId + "/" + filename;
            String url = upload(thumbnailsBucket, objectKey, bytes, contentType);
            return CompletableFuture.completedFuture(new UploadResult(url, objectKey));
        } catch (Exception e) {
            throw new RuntimeException("File upload failed", e);
        }
    }

    public InputStream getVideoStream(String objectKey, long offset, long length) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(moviesBucket)
                    .object(objectKey)
                    .offset(offset)
                    .length(length)
                    .build());
        } catch (Exception e) {
            log.error("Failed to get video stream: {}", e.getMessage());
            throw new RuntimeException("Failed to stream video", e);
        }
    }

    public long getVideoSize(String objectKey) {
        try {
            return minioClient.statObject(StatObjectArgs.builder()
                    .bucket(moviesBucket)
                    .object(objectKey)
                    .build()).size();
        } catch (Exception e) {
            log.error("Failed to get video size: {}", e.getMessage());
            throw new RuntimeException("Failed to get video size", e);
        }
    }

    private String upload(String bucket, String objectName, byte[] bytes, String contentType) {
        try {
            ensureBucketExists(bucket);
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(new java.io.ByteArrayInputStream(bytes), bytes.length, -1)
                    .contentType(contentType)
                    .build());
            return minioUrl + "/" + bucket + "/" + objectName;
        } catch (Exception e) {
            log.error("Failed to upload file to MinIO:", e);
            throw new RuntimeException("File upload failed", e);
        }
    }

    private void ensureBucketExists(String bucket) throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }
}
