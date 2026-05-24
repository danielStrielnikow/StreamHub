package com.streamhub.movieservice.domain.service.impl;

import com.streamhub.movieservice.api.exceptiopn.ResourceNotFoundException;
import com.streamhub.movieservice.application.dto.request.MovieRequest;
import com.streamhub.movieservice.application.dto.response.MovieResponse;
import com.streamhub.movieservice.application.mapper.MovieMapper;
import com.streamhub.movieservice.domain.service.MovieService;
import com.streamhub.movieservice.infrastructure.minio.MinioService;
import com.streamhub.movieservice.model.Genre;
import com.streamhub.movieservice.model.Movie;
import com.streamhub.movieservice.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;
    private final MinioService minioService;

    @Override
    @CacheEvict(value = "movies", allEntries = true)
    public MovieResponse createMovie(MovieRequest request) {
        Movie movie = movieMapper.toEntity(request);
        return movieMapper.toResponse(movieRepository.save(movie));
    }

    @Override
    @Cacheable(value = "movies", key = "#id")
    public MovieResponse getMovieById(String id) {
        return movieMapper.toResponse(findById(id));
    }

    @Override
    @Cacheable(value = "movies", key = "'all'")
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
    @CacheEvict(value = "movies", allEntries = true)
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
    @CacheEvict(value = "movies", allEntries = true)
    public void deleteMovie(String id) {
        movieRepository.delete(findById(id));
    }

    @Override
    @CacheEvict(value = "movies", allEntries = true)
    public MovieResponse uploadVideo(String id, MultipartFile file) {
        Movie movie = findById(id);
        String url = minioService.uploadVideo(id, file);
        movie.setVideoUrl(url);
        return movieMapper.toResponse(movieRepository.save(movie));
    }

    @Override
    @CacheEvict(value = "movies", allEntries = true)
    public MovieResponse uploadThumbnail(String id, MultipartFile file) {
        Movie movie = findById(id);
        String url = minioService.uploadThumbnail(id, file);
        movie.setThumbnailUrl(url);
        return movieMapper.toResponse(movieRepository.save(movie));
    }

    private Movie findById(String id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found: " + id));
    }
}



        
  
