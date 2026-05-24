package com.streamhub.movieservice.domain.service;

import com.streamhub.movieservice.application.dto.request.MovieRequest;
import com.streamhub.movieservice.application.dto.response.MovieResponse;
import com.streamhub.movieservice.model.Genre;
import org.springframework.web.multipart.MultipartFile;

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
}
