package com.streamhub.movieservice.infrastructure.minio;

import com.streamhub.movieservice.application.dto.request.PartInfo;
import com.streamhub.movieservice.application.dto.response.UploadInitResponse;
import io.minio.*;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService implements ChunkedUploadService {

    private final MinioClient minioClient;

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.bucket.movies}")
    private String moviesBucket;

    @Value("${minio.bucket.thumbnails}")
    private String thumbnailsBucket;

    public record UploadResult(String url, String objectKey) {}

    private final ConcurrentHashMap<String, UploadState> activeUploads = new ConcurrentHashMap<>();
    private record UploadState(String objectKey, String bucket, String contentType) {}

    // simple async upload (small files) 

    @Async("uploadExecutor")
    public CompletableFuture<UploadResult> uploadVideo(String movieId, byte[] bytes,
                                                       String contentType, String filename) {
        try {
            String objectKey = movieId + "/" + filename;
            String url = upload(moviesBucket, objectKey, bytes, contentType);
            return CompletableFuture.completedFuture(new UploadResult(url, objectKey));
        } catch (Exception e) {
            throw new RuntimeException("File upload failed", e);
        }
    }

    @Async("uploadExecutor")
    public CompletableFuture<UploadResult> uploadThumbnail(String movieId, byte[] bytes,
                                                           String contentType, String filename) {
        try {
            String objectKey = movieId + "/" + filename;
            String url = upload(thumbnailsBucket, objectKey, bytes, contentType);
            return CompletableFuture.completedFuture(new UploadResult(url, objectKey));
        } catch (Exception e) {
            throw new RuntimeException("File upload failed", e);
        }
    }

    // chunked upload 

    public UploadInitResponse initVideoUpload(String movieId, String filename, String contentType) {
        try {
            String objectKey = movieId + "/" + filename;
            ensureBucketExists(moviesBucket);
            String uploadId = UUID.randomUUID().toString();
            activeUploads.put(uploadId, new UploadState(objectKey, moviesBucket, contentType));
            return new UploadInitResponse(uploadId, objectKey);
        } catch (Exception e) {
            log.error("Failed to init upload for movie {}:", movieId, e);
            throw new RuntimeException("Failed to initialize upload", e);
        }
    }

    public void uploadChunk(String uploadId, int partNumber, InputStream data, long size) {
        UploadState state = activeUploads.get(uploadId);
        if (state == null) throw new RuntimeException("Unknown uploadId: " + uploadId);
        try {
            String tempKey = "parts/" + uploadId + "/" + partNumber;
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(state.bucket())
                    .object(tempKey)
                    .stream(data, size, -1)
                    .contentType(state.contentType())
                    .build());
        } catch (Exception e) {
            log.error("Failed to upload chunk {} for uploadId {}:", partNumber, uploadId, e);
            throw new RuntimeException("Failed to upload chunk", e);
        }
    }

    public String completeVideoUpload(String uploadId, List<PartInfo> partInfos) {
        UploadState state = activeUploads.remove(uploadId);
        if (state == null) throw new RuntimeException("Unknown uploadId: " + uploadId);
        try {
            List<ComposeSource> sources = partInfos.stream()
                    .sorted(Comparator.comparingInt(PartInfo::partNumber))
                    .map(p -> ComposeSource.builder()
                            .bucket(state.bucket())
                            .object("parts/" + uploadId + "/" + p.partNumber())
                            .build())
                    .collect(Collectors.toList());

            minioClient.composeObject(ComposeObjectArgs.builder()
                    .bucket(state.bucket())
                    .object(state.objectKey())
                    .sources(sources)
                    .build());

            deleteTempParts(state.bucket(), partInfos.stream()
                    .map(p -> "parts/" + uploadId + "/" + p.partNumber())
                    .collect(Collectors.toList()));

            return minioUrl + "/" + state.bucket() + "/" + state.objectKey();
        } catch (Exception e) {
            log.error("Failed to complete upload {}:", uploadId, e);
            throw new RuntimeException("Failed to complete upload", e);
        }
    }

    public void abortVideoUpload(String uploadId) {
        UploadState state = activeUploads.remove(uploadId);
        if (state == null) return;
        try {
            List<String> keys = new ArrayList<>();
            for (Result<Item> r : minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(state.bucket())
                    .prefix("parts/" + uploadId + "/")
                    .build())) {
                keys.add(r.get().objectName());
            }
            deleteTempParts(state.bucket(), keys);
        } catch (Exception e) {
            log.warn("Failed to abort upload {}:", uploadId, e);
        }
    }

    //  streaming 

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

    //  private 

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

    private void deleteTempParts(String bucket, List<String> keys) {
        if (keys.isEmpty()) return;
        List<DeleteObject> deletes = keys.stream()
                .map(DeleteObject::new)
                .collect(Collectors.toList());
        minioClient.removeObjects(RemoveObjectsArgs.builder()
                .bucket(bucket)
                .objects(deletes)
                .build())
                .forEach(r -> { try { r.get(); } catch (Exception ignored) {} });
    }

    private void ensureBucketExists(String bucket) throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }
}
