package com.streamhub.movieservice.api.rest;

import com.streamhub.movieservice.application.dto.request.MovieRequest;
import com.streamhub.movieservice.application.dto.response.MovieResponse;
import com.streamhub.movieservice.domain.service.MovieService;
import com.streamhub.movieservice.model.Genre;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @PostMapping
    public ResponseEntity<MovieResponse> create(@Valid @RequestBody MovieRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.createMovie(request));
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
        return ResponseEntity.ok(movieService.uploadVideo(id, file));
    }

    @PostMapping("/{id}/thumbnail")
    public ResponseEntity<MovieResponse> uploadThumbnail(@PathVariable String id, @RequestParam MultipartFile file) {
        return ResponseEntity.ok(movieService.uploadThumbnail(id, file));
    }
}
