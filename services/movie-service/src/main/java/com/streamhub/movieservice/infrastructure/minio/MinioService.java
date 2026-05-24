package com.streamhub.movieservice.infrastructure.minio;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    @Async("uploadExecutor")
    public CompletableFuture<String> uploadVideo(String movieId, MultipartFile file) {
        return CompletableFuture.completedFuture(upload(moviesBucket, movieId + "/" + file.getOriginalFilename(), file));
    }

    @Async("uploadExecutor")
    public CompletableFuture<String> uploadThumbnail(String movieId, MultipartFile file) {
        return CompletableFuture.completedFuture(upload(thumbnailsBucket, movieId + "/" + file.getOriginalFilename(), file));
    }

    private String upload(String bucket, String objectName, MultipartFile file) {
        try {
            ensureBucketExists(bucket);
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            return minioUrl + "/" + bucket + "/" + objectName;
        } catch (Exception e) {
            log.error("Failed to upload file to MinIO: {}", e.getMessage());
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
