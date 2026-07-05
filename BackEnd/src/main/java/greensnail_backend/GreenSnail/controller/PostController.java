package greensnail_backend.GreenSnail.controller;

import greensnail_backend.GreenSnail.dto.PostRequestDto;
import greensnail_backend.GreenSnail.dto.PostResponseDto;
import greensnail_backend.GreenSnail.dto.PostUpdateRequestDto;
import greensnail_backend.GreenSnail.entity.JobCategory;
import greensnail_backend.GreenSnail.entity.AgeCategory;
import greensnail_backend.GreenSnail.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponseDto> createPost(
            @RequestHeader("Provider-Id") String providerId,
            @RequestBody PostRequestDto dto) {
        return ResponseEntity.ok(postService.createPost(providerId, dto));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostResponseDto> updatePost(
            @PathVariable Long postId,
            @RequestBody PostUpdateRequestDto dto
    ) {
        return ResponseEntity.ok(postService.updatePost(postId, dto));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/filter")
    public ResponseEntity<List<PostResponseDto>> filterPosts(
            @RequestParam(required = false) JobCategory jobCategory,
            @RequestParam(required = false) AgeCategory ageCategory
    ) {
        return ResponseEntity.ok(postService.filterPosts(jobCategory, ageCategory));
    }

    @GetMapping("/search")
    public ResponseEntity<List<PostResponseDto>> searchPostsByTitle(
            @RequestParam String keyword
    ) {
        return ResponseEntity.ok(postService.searchPostsByTitle(keyword));
    }

    @GetMapping
    public ResponseEntity<List<PostResponseDto>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @GetMapping("/my-posts")
    public ResponseEntity<List<PostResponseDto>> getMyPosts(
            @RequestHeader("Provider-Id") String providerId) {
        return ResponseEntity.ok(postService.getMyPosts(providerId));
    }
}