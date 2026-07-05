package greensnail_backend.GreenSnail.dto;

import greensnail_backend.GreenSnail.entity.Article;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ArticleDetailDto {
    private Long articleId;
    private String title;
    private String content;

    public static ArticleDetailDto from(Article article) {
        return ArticleDetailDto.builder()
                .articleId(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .build();
    }
}
