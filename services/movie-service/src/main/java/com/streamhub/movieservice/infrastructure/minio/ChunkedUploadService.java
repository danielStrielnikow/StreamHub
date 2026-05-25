package com.streamhub.movieservice.infrastructure.minio;

import com.streamhub.movieservice.application.dto.request.PartInfo;
import com.streamhub.movieservice.application.dto.response.UploadInitResponse;

import java.io.InputStream;
import java.util.List;

public interface ChunkedUploadService {
    UploadInitResponse initVideoUpload(String movieId, String filename, String contentType);
    void uploadChunk(String uploadId, int partNumber, InputStream data, long size);
    String completeVideoUpload(String uploadId, List<PartInfo> partInfos);
    void abortVideoUpload(String uploadId);
}
