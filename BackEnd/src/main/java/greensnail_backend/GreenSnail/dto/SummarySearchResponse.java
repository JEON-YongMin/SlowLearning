package greensnail_backend.GreenSnail.dto;

import java.time.LocalDateTime;

public class SummarySearchResponse {
    private Long articleId;
    private String title;
    private LocalDateTime summarizedAt;

    public SummarySearchResponse(Long articleId, String title, LocalDateTime summarizedAt) {
        this.articleId = articleId;
        this.title = title;
        this.summarizedAt = summarizedAt;
    }

    public Long getArticleId() {
        return articleId;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getSummarizedAt() {
        return summarizedAt;
    }
}
