package greensnail_backend.GreenSnail.dto;

import greensnail_backend.GreenSnail.entity.Article;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ArticleListDto {
    private Long articleId;
    private String title;

    public static ArticleListDto from(Article article) {
        return ArticleListDto.builder()
                .articleId(article.getId())
                .title(article.getTitle())
                .build();
    }
}
