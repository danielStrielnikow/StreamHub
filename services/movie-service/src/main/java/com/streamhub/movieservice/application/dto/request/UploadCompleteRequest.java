package com.streamhub.movieservice.application.dto.request;

import java.util.List;

public record UploadCompleteRequest(String uploadId, List<PartInfo> parts) {}
