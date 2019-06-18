package com.cassiomolin.logaggregation.review.web.controller;

import com.cassiomolin.logaggregation.review.domain.Review;
import com.cassiomolin.logaggregation.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Review>> getReviewsForMovie(@RequestParam Long movieId) {

        log.info("Getting reviews for movie with id {}", movieId);
        List<Review> reviews = service.getReviewsForMovie(movieId);
        log.info("Found {} review(s) for movie with id {}", reviews.size(), movieId);

        return ResponseEntity.ok(reviews);
    }
}
