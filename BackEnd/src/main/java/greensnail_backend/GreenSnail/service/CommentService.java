package greensnail_backend.GreenSnail.service;

import greensnail_backend.GreenSnail.dto.*;
import greensnail_backend.GreenSnail.entity.Comment;
import greensnail_backend.GreenSnail.entity.Post;
import greensnail_backend.GreenSnail.entity.User;
import greensnail_backend.GreenSnail.global.api.ErrorCode;
import greensnail_backend.GreenSnail.global.exception.CustomException;
import greensnail_backend.GreenSnail.repository.CommentRepository;
import greensnail_backend.GreenSnail.repository.PostRepository;
import greensnail_backend.GreenSnail.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentResponseDto createComment(Long postId, String providerId, CommentRequestDto dto) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        User writerUser = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Comment comment = Comment.builder()
                .content(dto.getComment())
                .post(post)
                .user(writerUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return CommentResponseDto.from(commentRepository.save(comment));
    }

    @Transactional
    public CommentResponseDto updateComment(Long commentId, CommentUpdateRequestDto dto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        comment.setContent(dto.getRecomment());
        comment.setUpdatedAt(LocalDateTime.now());

        return CommentResponseDto.from(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
        }
        commentRepository.deleteById(commentId);
    }
}