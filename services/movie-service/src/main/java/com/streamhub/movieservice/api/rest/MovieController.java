package com.streamhub.movieservice.api.rest;

import com.streamhub.movieservice.application.dto.request.MovieRequest;
import com.streamhub.movieservice.application.dto.request.UploadCompleteRequest;
import com.streamhub.movieservice.application.dto.request.UploadInitRequest;
import com.streamhub.movieservice.application.dto.response.MovieResponse;
import com.streamhub.movieservice.application.dto.response.UploadInitResponse;
import com.streamhub.movieservice.domain.service.MovieService;
import com.streamhub.movieservice.model.enums.Genre;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @PostMapping
    public ResponseEntity<MovieResponse> create(@Valid @RequestBody MovieRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.createMovie(request));
    }

    @GetMapping("/{id}/stream")
    public ResponseEntity<StreamingResponseBody> stream(
            @PathVariable String id,
            @RequestHeader(value = "Range", required = false) String range) {
        return movieService.streamVideo(id, range);
    }
    

    @GetMapping("/{id}")
    public ResponseEntity<MovieResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    @GetMapping
    public ResponseEntity<List<MovieResponse>> getAll() {
        return ResponseEntity.ok(movieService.getAllMovies());
    }

    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<MovieResponse>> getByGenre(@PathVariable Genre genre) {
        return ResponseEntity.ok(movieService.getMoviesByGenre(genre));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MovieResponse>> search(@RequestParam String title) {
        return ResponseEntity.ok(movieService.searchMovies(title));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovieResponse> update(@PathVariable String id, @Valid @RequestBody MovieRequest request) {
        return ResponseEntity.ok(movieService.updateMovie(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/video")
    public ResponseEntity<MovieResponse> uploadVideo(@PathVariable String id, @RequestParam MultipartFile file) {
        return ResponseEntity.accepted().body(movieService.uploadVideo(id, file));
    }

    @PostMapping("/{id}/thumbnail")
    public ResponseEntity<MovieResponse> uploadThumbnail(@PathVariable String id, @RequestParam MultipartFile file) {
        return ResponseEntity.ok(movieService.uploadThumbnail(id, file));
    }

    @PostMapping("/{id}/upload/init")
    public ResponseEntity<UploadInitResponse> initUpload(
            @PathVariable String id,
            @RequestBody UploadInitRequest request) {
        return ResponseEntity.ok(movieService.initVideoUpload(id, request));
    }

    @PutMapping("/{id}/upload/part")
    public ResponseEntity<Map<String, Object>> uploadChunk(
            @PathVariable String id,
            @RequestParam String uploadId,
            @RequestParam int partNumber,
            HttpServletRequest request) {
        String result = movieService.uploadVideoChunk(id, uploadId, partNumber, request);
        return ResponseEntity.ok(Map.of("partNumber", partNumber, "status", result));
    }

    @PostMapping("/{id}/upload/complete")
    public ResponseEntity<MovieResponse> completeUpload(
            @PathVariable String id,
            @RequestBody UploadCompleteRequest request) {
        return ResponseEntity.ok(movieService.completeVideoUpload(id, request));
    }

    @DeleteMapping("/{id}/upload/{uploadId}")
    public ResponseEntity<Void> abortUpload(
            @PathVariable String id,
            @PathVariable String uploadId) {
        movieService.abortVideoUpload(id, uploadId);
        return ResponseEntity.noContent().build();
    }
}
