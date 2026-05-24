package com.streamhub.movieservice.application.dto.response;

import com.streamhub.movieservice.model.Genre;

import java.time.LocalDateTime;
import java.util.List;

public record MovieResponse(
        String id,
        String title,
        String description,
        Genre genre,
        int durationMinutes,
        int releaseYear,
        String director,
        List<String> cast,
        String videoUrl,
        String thumbnailUrl,
        Double rating,
        LocalDateTime createdAt
) {}
