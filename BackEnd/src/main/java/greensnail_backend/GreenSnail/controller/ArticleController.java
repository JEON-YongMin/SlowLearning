package greensnail_backend.GreenSnail.controller;

import greensnail_backend.GreenSnail.dto.ArticleListDto;
import greensnail_backend.GreenSnail.dto.ArticleDetailDto;
import greensnail_backend.GreenSnail.entity.Level;
import greensnail_backend.GreenSnail.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping("/list")
    public ResponseEntity<List<ArticleListDto>> getArticlesByLevel(@RequestParam("level") Level level) {
        return ResponseEntity.ok(articleService.getArticlesByLevel(level));
    }

    @GetMapping("/{articleId}")
    public ResponseEntity<ArticleDetailDto> getArticleDetail(@PathVariable Long articleId) {
        return ResponseEntity.ok(articleService.getArticleDetail(articleId));
    }
}
