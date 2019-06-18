package com.cassiomolin.logaggregation.movie.web.controller;

import com.cassiomolin.logaggregation.movie.domain.Movie;
import com.cassiomolin.logaggregation.movie.service.MovieService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/movies")
public class MovieController {

    private final MovieService service;

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Movie> getMovie(@PathVariable Long id) {

        log.info("Getting details from movie with id {}", id);
        Movie movie = service.getMovie(id).orElseThrow(() -> new RuntimeException("Not found")); // FIXME
        return ResponseEntity.ok(movie);
    }
}
