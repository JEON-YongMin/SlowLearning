package greensnail_backend.GreenSnail.controller;

import greensnail_backend.GreenSnail.dto.*;
import greensnail_backend.GreenSnail.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponseDto> createComment(@PathVariable Long postId,
                                                            @RequestParam String providerId,
                                                            @RequestBody CommentRequestDto dto) {
        return ResponseEntity.ok(commentService.createComment(postId, providerId, dto));
    }

    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(@PathVariable Long commentId,
                                                            @RequestBody CommentUpdateRequestDto dto) {
        return ResponseEntity.ok(commentService.updateComment(commentId, dto));
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok().build();
    }
}
