package com.streamhub.movieservice.application.mapper;

import com.streamhub.movieservice.application.dto.request.MovieRequest;
import com.streamhub.movieservice.application.dto.response.MovieResponse;
import com.streamhub.movieservice.model.Movie;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface MovieMapper {
    MovieResponse toResponse(Movie movie);
    List<MovieResponse> toListResponse(List<Movie> movies);
    Movie toEntity(MovieRequest request);
}
