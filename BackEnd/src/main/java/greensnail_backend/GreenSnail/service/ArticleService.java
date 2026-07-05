package greensnail_backend.GreenSnail.service;

import greensnail_backend.GreenSnail.dto.ArticleListDto;
import greensnail_backend.GreenSnail.dto.ArticleDetailDto;
import greensnail_backend.GreenSnail.entity.Article;
import greensnail_backend.GreenSnail.entity.Level;
import greensnail_backend.GreenSnail.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;

    public List<ArticleListDto> getArticlesByLevel(Level level) {
        return articleRepository.findByLevel(level).stream()
                .map(ArticleListDto::from)
                .toList();
    }

    public ArticleDetailDto getArticleDetail(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("글을 찾을 수 없습니다."));
        return ArticleDetailDto.from(article);
    }
}
