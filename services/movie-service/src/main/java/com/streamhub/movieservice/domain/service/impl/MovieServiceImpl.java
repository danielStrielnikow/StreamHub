package com.streamhub.movieservice.domain.service.impl;

import com.streamhub.movieservice.api.exceptiopn.ResourceNotFoundException;
import com.streamhub.movieservice.application.dto.request.MovieRequest;
import com.streamhub.movieservice.application.dto.response.MovieResponse;
import com.streamhub.movieservice.application.mapper.MovieMapper;
import com.streamhub.movieservice.domain.service.MovieService;
import com.streamhub.movieservice.infrastructure.minio.MinioService;
import com.streamhub.movieservice.model.Movie;
import com.streamhub.movieservice.model.enums.Genre;
import com.streamhub.movieservice.model.enums.VideoStatus;
import com.streamhub.movieservice.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {
    
    private static final long CHUNK_SIZE = 1024 * 1024; // 1MB

    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;
    private final MinioService minioService;

    @Override
    public MovieResponse createMovie(MovieRequest request) {
        Movie movie = movieMapper.toEntity(request);
        return movieMapper.toResponse(movieRepository.save(movie));
    }

    @Override
    public MovieResponse getMovieById(String id) {
        return movieMapper.toResponse(findById(id));
    }

    @Override
    public List<MovieResponse> getAllMovies() {
        return movieMapper.toListResponse(movieRepository.findAll());
    }

    @Override
    public List<MovieResponse> getMoviesByGenre(Genre genre) {
        return movieMapper.toListResponse(movieRepository.findByGenre(genre));
    }

    @Override
    public List<MovieResponse> searchMovies(String title) {
        return movieMapper.toListResponse(movieRepository.findByTitleContainingIgnoreCase(title));
    }

    @Override
    public MovieResponse updateMovie(String id, MovieRequest request) {
        Movie movie = findById(id);
        movie.setTitle(request.title());
        movie.setDescription(request.description());
        movie.setGenre(request.genre());
        movie.setDurationMinutes(request.durationMinutes());
        movie.setReleaseYear(request.releaseYear());
        movie.setDirector(request.director());
        movie.setCast(request.cast());
        return movieMapper.toResponse(movieRepository.save(movie));
    }

    @Override
    public void deleteMovie(String id) {
        movieRepository.delete(findById(id));
    }

    @Override
    public MovieResponse uploadVideo(String id, MultipartFile file) {
        Movie movie = findById(id);
        movie.setVideoStatus(VideoStatus.PROCESSING);
        movieRepository.save(movie);

        try {
            byte[] bytes = file.getBytes();
            String contentType = file.getContentType() != null ? file.getContentType() : "video/mp4";
            String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : id + ".mp4";

            minioService.uploadVideo(id, bytes, contentType, filename)
                    .thenAccept(result -> {
                        Movie m = findById(id);
                        m.setVideoUrl(result.url());
                        m.setVideoKey(result.objectKey());
                        m.setVideoStatus(VideoStatus.READY);
                        movieRepository.save(m);
                        log.info("Video uploaded for movie: {}", id);
                    })
                    .exceptionally(ex -> {
                        Movie m = findById(id);
                        m.setVideoStatus(VideoStatus.FAILED);
                        movieRepository.save(m);
                        log.error("Video upload failed for movie {}:", id, ex);
                        return null;
                    });
        } catch (Exception e) {
            movie.setVideoStatus(VideoStatus.FAILED);
            movieRepository.save(movie);
            log.error("Failed to read file for movie {}:", id, e);
        }

        return movieMapper.toResponse(movie);
    }

    @Override
    public MovieResponse uploadThumbnail(String id, MultipartFile file) {
        try {
            Movie movie = findById(id);
            byte[] bytes = file.getBytes();
            String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";
            String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : id + ".jpg";
            MinioService.UploadResult result = minioService.uploadThumbnail(id, bytes, contentType, filename).join();
            movie.setThumbnailUrl(result.url());
            return movieMapper.toResponse(movieRepository.save(movie));
        } catch (Exception e) {
            log.error("Failed to upload thumbnail for movie {}:", id, e);
            throw new RuntimeException("Thumbnail upload failed", e);
        }
    }

    @Override
    public ResponseEntity<StreamingResponseBody> streamVideo(String id, String rangeHeader) {
        Movie movie = findById(id);
        if (movie.getVideoKey() == null) {
            throw new ResourceNotFoundException("Video not uploaded for movie: " + id);
        }
        
        long fileSize = minioService.getVideoSize(movie.getVideoKey());
        long start = 0;
        long end = fileSize - 1;

        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String[] parts = rangeHeader.substring(6).split("-");
            start = Long.parseLong(parts[0]);
            end = parts.length > 1 && !parts[1].isEmpty()
                    ? Long.parseLong(parts[1])
                    : Math.min(start + CHUNK_SIZE - 1, fileSize - 1);
        }
        
        long chunkSize = end - start + 1;
        long finalStart = start;
        long finalEnd = end;

        StreamingResponseBody body = outputStream -> {
            try (InputStream stream = minioService.getVideoStream(movie.getVideoKey(), finalStart, chunkSize)) {
                stream.transferTo(outputStream);
            }
        };

        HttpStatus status = rangeHeader != null ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK;

        return ResponseEntity.status(status)
                .header(HttpHeaders.CONTENT_TYPE, "video/mp4")
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(chunkSize))
                .header(HttpHeaders.CONTENT_RANGE, "bytes " + finalStart + "-" + finalEnd + "/" + fileSize)
                .body(body);
    }

    private Movie findById(String id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found: " + id));
    }
}



        
  
