package greensnail_backend.GreenSnail.dto;

import greensnail_backend.GreenSnail.entity.Summary;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MySummaryDto {

    private Long summaryId;
    private String articleTitle;
    private String userSummary;
    private String aiFeedback;
    private LocalDateTime createdAt;

    public static MySummaryDto from(Summary summary) {
        return MySummaryDto.builder()
                .summaryId(summary.getId())
                .articleTitle(summary.getArticle().getTitle())
                .userSummary(summary.getUserSummary())
                .aiFeedback(summary.getFeedback())
                .createdAt(summary.getCreatedAt())
                .build();
    }
}