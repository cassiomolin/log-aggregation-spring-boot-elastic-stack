package com.cassiomolin.logaggregation.movie.service;

import com.cassiomolin.logaggregation.movie.domain.Movie;
import com.cassiomolin.logaggregation.movie.domain.Review;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieService {

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

    public Optional<Movie> getMovie(Long movieId) {

        Optional<Movie> optionalMovie = MOVIES.stream()
                .filter(movie -> movie.getId().equals(movieId))
                .findFirst();

        optionalMovie.ifPresent(movie -> {
            List<Review> reviews = this.findReviewsForMovie(movie);
            movie.setReviews(reviews);
        });

        return optionalMovie;
    }

    private List<Review> findReviewsForMovie(Movie movie) {

        log.info("Finding reviews for movies with id {}", movie.getId());

        String url = UriComponentsBuilder.fromHttpUrl("http://localhost:8002/reviews")
                .queryParam("movieId", movie.getId())
                .toUriString();

        ResponseEntity<List<Review>> response = restTemplate.exchange(url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Review>>() {});

        return response.getBody();
    }
}
