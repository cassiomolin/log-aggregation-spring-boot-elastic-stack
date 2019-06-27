package com.cassiomolin.logaggregation.post.service;

import com.cassiomolin.logaggregation.post.domain.Comment;
import com.cassiomolin.logaggregation.post.domain.Post;
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
public class PostService {

    @Value("${comment-service.base-url}")
    private String commentServiceBaseUrl;

    private final RestTemplate restTemplate;

    private static final List<Post> POSTS = new ArrayList<>();

    static {

        POSTS.add(Post.builder()
                .id(1L)
                .title("The Dark Knight")
                .year(2008)
                .build());

        POSTS.add(Post.builder()
                .id(2L)
                .title("Avengers")
                .year(2012)
                .build());
    }

    public Optional<Post> getPost(Long id) {

        log.info("Finding details of post with id {}", id);

        Optional<Post> optionalPost = POSTS.stream()
                .filter(post -> post.getId().equals(id))
                .findFirst();

        optionalPost.ifPresent(post -> {
            List<Comment> comments = this.findCommentsForPost(post);
            post.setComments(comments);
        });

        return optionalPost;
    }

    private List<Comment> findCommentsForPost(Post post) {

        log.info("Finding comments of post with id {}", post.getId());

        String url = UriComponentsBuilder.fromHttpUrl(commentServiceBaseUrl)
                .path("comments")
                .queryParam("postId", post.getId())
                .toUriString();

        ResponseEntity<List<Comment>> response = restTemplate.exchange(url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Comment>>() {});

        List<Comment> comments = Objects.isNull(response.getBody()) ? new ArrayList<>() : response.getBody();
        log.info("Found {} comment(s) of post with id {}", comments.size(), post.getId());
        return comments;
    }
}
