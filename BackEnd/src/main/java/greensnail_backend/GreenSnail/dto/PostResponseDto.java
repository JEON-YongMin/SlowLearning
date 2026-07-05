package greensnail_backend.GreenSnail.dto;

import greensnail_backend.GreenSnail.entity.AgeCategory;
import greensnail_backend.GreenSnail.entity.JobCategory;
import greensnail_backend.GreenSnail.entity.Post;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponseDto {

    private String nickname;
    private String profileImage;
    private Long postId;
    private String title;
    private String content;
    private JobCategory jobCategory;
    private AgeCategory ageCategory;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CommentResponseDto> comments;

    public static PostResponseDto from(Post post) {
        return PostResponseDto.builder()
                .nickname(post.getUser().getNickname())
                .profileImage(post.getUser().getProfileImage())
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .jobCategory(post.getJobCategory())
                .ageCategory(post.getAgeCategory())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .comments(post.getComments() != null ? post.getComments().stream()
                        .map(CommentResponseDto::from)
                        .collect(Collectors.toList()) : List.of())
                .build();
    }
}