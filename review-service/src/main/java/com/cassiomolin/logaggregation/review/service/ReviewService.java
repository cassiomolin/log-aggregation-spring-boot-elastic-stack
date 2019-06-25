package com.cassiomolin.logaggregation.review.service;

import com.cassiomolin.logaggregation.review.domain.Review;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
public class ReviewService {

    private static final List<Review> REVIEWS = new ArrayList<>();

    static {

        REVIEWS.add(Review.builder()
                .id(1L)
                .movieId(1L)
                .content("Awesome!")
                .build());

        REVIEWS.add(Review.builder()
                .id(2L)
                .movieId(1L)
                .content("Perfect!")
                .build());

        REVIEWS.add(Review.builder()
                .id(3L)
                .movieId(2L)
                .content("Best movie ever!")
                .build());
    }

    public List<Review> getReviewsForMovie(Long movieId) {

        log.info("Finding reviews of movie with id {}", movieId);

        List<Review> reviews = REVIEWS.stream()
                .filter(review -> review.getMovieId().equals(movieId))
                .collect(toList());

        log.info("Found {} review(s) of movie with id {}", reviews.size(), movieId);
        return reviews;
    }
}
