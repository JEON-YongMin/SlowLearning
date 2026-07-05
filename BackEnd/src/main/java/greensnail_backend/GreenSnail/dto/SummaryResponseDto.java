package greensnail_backend.GreenSnail.dto;

import greensnail_backend.GreenSnail.entity.Summary;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SummaryResponseDto {
    private Long summaryId;
    private Long articleId;
    private String userSummary;
    private String aiFeedback;
    private LocalDateTime createdAt;

    public static SummaryResponseDto from(Summary summary) {
        return SummaryResponseDto.builder()
                .summaryId(summary.getId())
                .articleId(summary.getArticle().getId())
                .userSummary(summary.getUserSummary())
                .aiFeedback(summary.getFeedback())
                .createdAt(summary.getCreatedAt())
                .build();
    }
}
