package com.streamhub.movieservice.application.dto.request;

import com.streamhub.movieservice.model.Genre;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MovieRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotNull Genre genre,
        @Min(1) int durationMinutes,
        @Min(1888) int releaseYear,
        @NotBlank String director,
        List<String> cast
) {}
