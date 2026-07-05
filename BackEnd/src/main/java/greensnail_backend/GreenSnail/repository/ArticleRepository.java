package greensnail_backend.GreenSnail.repository;

import greensnail_backend.GreenSnail.entity.Article;
import greensnail_backend.GreenSnail.entity.Level;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    List<Article> findByLevel(Level level);
    Optional<Article> findByTitle(String title);
    Optional<Article> findById(Long id);
}

