package com.cassiomolin.logaggregation.movie.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movie {

    private Long id;

    private String title;

    private Integer year;

    private List<Review> reviews;
}
