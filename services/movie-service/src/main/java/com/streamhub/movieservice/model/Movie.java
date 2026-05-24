package com.streamhub.movieservice.model;

import com.streamhub.common.domain.BaseDocument;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "movies")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Movie extends BaseDocument {
    private String title;
    private String description;
    private Genre genre;
    private int durationMinutes;
    private int releaseYear;
    private String director;
    private List<String> cast;
    private String videoUrl;
    private String thumbnailUrl;
    private Double rating;
}
