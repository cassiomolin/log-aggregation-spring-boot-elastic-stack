package com.cassiomolin.logaggregation.movie.service;

import com.cassiomolin.logaggregation.movie.domain.Movie;
import com.cassiomolin.logaggregation.movie.domain.Review;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieService {

    @Value("${review-service.base-url}")
    private String reviewServiceBaseUrl;

    private final RestTemplate restTemplate;

    private static final List<Movie> MOVIES = new ArrayList<>();

    static {

        MOVIES.add(Movie.builder()
                .id(1L)
                .title("The Dark Knight")
                .year(2008)
                .build());

        MOVIES.add(Movie.builder()
                .id(2L)
                .title("Avengers")
                .year(2012)
                .build());
    }

    public Optional<Movie> getMovie(Long id) {

        log.info("Finding details of movie with id {}", id);

        Optional<Movie> optionalMovie = MOVIES.stream()
                .filter(movie -> movie.getId().equals(id))
                .findFirst();

        optionalMovie.ifPresent(movie -> {
            List<Review> reviews = this.findReviewsForMovie(movie);
            movie.setReviews(reviews);
        });

        return optionalMovie;
    }

    private List<Review> findReviewsForMovie(Movie movie) {

        log.info("Finding reviews of movie with id {}", movie.getId());

        String url = UriComponentsBuilder.fromHttpUrl(reviewServiceBaseUrl)
                .path("reviews")
                .queryParam("movieId", movie.getId())
                .toUriString();

        ResponseEntity<List<Review>> response = restTemplate.exchange(url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Review>>() {});

        List<Review> reviews = Objects.isNull(response.getBody()) ? new ArrayList<>() : response.getBody();
        log.info("Found {} review(s) of movie with id {}", reviews.size(), movie.getId());
        return reviews;
    }
}
