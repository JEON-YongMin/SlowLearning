package greensnail_backend.GreenSnail.service;

import greensnail_backend.GreenSnail.dto.PostRequestDto;
import greensnail_backend.GreenSnail.dto.PostResponseDto;
import greensnail_backend.GreenSnail.dto.PostUpdateRequestDto;
import greensnail_backend.GreenSnail.entity.AgeCategory;
import greensnail_backend.GreenSnail.entity.JobCategory;
import greensnail_backend.GreenSnail.entity.Post;
import greensnail_backend.GreenSnail.entity.User;
import greensnail_backend.GreenSnail.global.api.ErrorCode;
import greensnail_backend.GreenSnail.global.exception.CustomException;
import greensnail_backend.GreenSnail.repository.PostRepository;
import greensnail_backend.GreenSnail.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public PostResponseDto createPost(String providerId, PostRequestDto dto) {
        User user = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Post post = Post.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .jobCategory(dto.getJobCategory())
                .ageCategory(dto.getAgeCategory())
                .user(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return toResponseDto(postRepository.save(post));
    }

    @Transactional
    public PostResponseDto updatePost(Long postId, PostUpdateRequestDto dto) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        post.setTitle(dto.getRetitle());
        post.setContent(dto.getRecontent());
        post.setJobCategory(dto.getJobCategory());
        post.setAgeCategory(dto.getAgeCategory());
        post.setUpdatedAt(LocalDateTime.now());

        return toResponseDto(postRepository.save(post));
    }

    @Transactional
    public void deletePost(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }
        postRepository.deleteById(postId);
    }

    public List<PostResponseDto> filterPosts(JobCategory job, AgeCategory age) {
        List<Post> posts;

        if (job != null && age != null) {
            posts = postRepository.findByJobCategoryAndAgeCategoryWithComments(job, age);
        } else if (job != null) {
            posts = postRepository.findByJobCategory(job);
        } else if (age != null) {
            posts = postRepository.findByAgeCategory(age);
        } else {
            posts = postRepository.findAllWithUserAndCommentsOrderByCreatedAtDesc();
        }

        return posts.stream()
                .filter(post -> post.getUser() != null) // null 체크 추가
                .map(this::toResponseDto)
                .toList();
    }

    public List<PostResponseDto> searchPostsByTitle(String keyword) {
        List<Post> posts = postRepository.findByTitleContainingWithComments(keyword);
        return posts.stream()
                .filter(post -> post.getUser() != null) // null 체크 추가
                .map(this::toResponseDto)
                .toList();
    }

    public List<PostResponseDto> getAllPosts() {
        return postRepository.findAllWithUserAndCommentsOrderByCreatedAtDesc()
                .stream()
                .filter(post -> post.getUser() != null) // null 체크 추가
                .map(this::toResponseDto)
                .toList();
    }

    public List<PostResponseDto> getMyPosts(String providerId) {
        return postRepository.findByUser_ProviderIdOrderByCreatedAtDesc(providerId)
                .stream()
                .filter(post -> post.getUser() != null) // null 체크 추가
                .map(this::toResponseDto)
                .toList();
    }

    private PostResponseDto toResponseDto(Post post) {
        if (post.getUser() == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        return PostResponseDto.from(post);
    }
}