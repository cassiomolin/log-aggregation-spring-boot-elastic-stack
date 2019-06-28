package com.cassiomolin.logaggregation.post.service;

import com.cassiomolin.logaggregation.post.domain.Post;
import com.cassiomolin.logaggregation.post.domain.PostWithComments;

import java.util.List;
import java.util.Optional;

public interface PostService {

    List<Post> getPosts();

    Optional<PostWithComments> getPost(Long id);
}
