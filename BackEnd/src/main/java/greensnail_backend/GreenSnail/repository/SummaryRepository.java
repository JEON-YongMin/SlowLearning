package greensnail_backend.GreenSnail.repository;

import greensnail_backend.GreenSnail.dto.SummarySearchResponse;
import greensnail_backend.GreenSnail.entity.Summary;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SummaryRepository extends JpaRepository<Summary, Long> {

    Summary findTopByProviderIdAndArticle_IdOrderByCreatedAtDesc(String providerId, Long articleId);

    @EntityGraph(attributePaths = {"article"})
    List<Summary> findAllByProviderIdOrderByCreatedAtDesc(String providerId);

    @Query("SELECT new greensnail_backend.GreenSnail.dto.SummarySearchResponse(" +
            "s.article.id, s.article.title, s.createdAt) " +
            "FROM Summary s " +
            "WHERE s.providerId = :providerId AND s.article.title LIKE %:keyword%")
    List<SummarySearchResponse> searchByKeywordAndProviderId(@Param("providerId") String providerId,
                                                             @Param("keyword") String keyword);

    List<Summary> findAllByArticle_Id(Long articleId);


    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
            "FROM Summary s " +
            "WHERE s.providerId = :providerId " +
            "AND s.clearedAt = :date " +
            "AND s.isCleared = true")
    boolean existsByProviderIdAndClearedAtDateAndIsClearedTrue(
            @Param("providerId") String providerId,
            @Param("date") LocalDate date);
}