package com.cassiomolin.logaggregation.post.web.controller;

import com.cassiomolin.logaggregation.post.domain.Post;
import com.cassiomolin.logaggregation.post.service.PostService;
import com.cassiomolin.logaggregation.post.web.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService service;

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Post> getPost(@PathVariable Long id) {
        Post post = service.getPost(id).orElseThrow(ResourceNotFoundException::new);
        return ResponseEntity.ok(post);
    }
}
