package greensnail_backend.GreenSnail.dto;

import greensnail_backend.GreenSnail.entity.Comment;
import greensnail_backend.GreenSnail.entity.User;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponseDto {

    private String nickname;
    private String profileImage;
    private Long postId;
    private Long commentId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CommentResponseDto from(Comment comment) {
        User user = comment.getUser();
        return CommentResponseDto.builder()
                .nickname(user != null ? user.getNickname() : "알 수 없음")
                .profileImage(user != null ? user.getProfileImage() : null)
                .postId(comment.getPost().getId())
                .commentId(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}