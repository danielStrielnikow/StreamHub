package com.streamhub.movieservice.domain.service;

import com.streamhub.movieservice.application.dto.request.MovieRequest;
import com.streamhub.movieservice.application.dto.request.UploadCompleteRequest;
import com.streamhub.movieservice.application.dto.request.UploadInitRequest;
import com.streamhub.movieservice.application.dto.response.MovieResponse;
import com.streamhub.movieservice.application.dto.response.UploadInitResponse;
import com.streamhub.movieservice.model.enums.Genre;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

public interface MovieService {
    MovieResponse createMovie(MovieRequest request);
    MovieResponse getMovieById(String id);
    List<MovieResponse> getAllMovies();
    List<MovieResponse> getMoviesByGenre(Genre genre);
    List<MovieResponse> searchMovies(String title);
    MovieResponse updateMovie(String id, MovieRequest request);
    void deleteMovie(String id);
    MovieResponse uploadVideo(String id, MultipartFile file);
    MovieResponse uploadThumbnail(String id, MultipartFile file);
    ResponseEntity<StreamingResponseBody> streamVideo(String id, String rangeHeader);
    UploadInitResponse initVideoUpload(String id, UploadInitRequest request);
    String uploadVideoChunk(String id, String uploadId, int partNumber, HttpServletRequest request);
    MovieResponse completeVideoUpload(String id, UploadCompleteRequest request);
    void abortVideoUpload(String id, String uploadId);
}
