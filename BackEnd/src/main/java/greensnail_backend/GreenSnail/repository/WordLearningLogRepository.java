package greensnail_backend.GreenSnail.repository;

import greensnail_backend.GreenSnail.entity.WordLearningLog;
import greensnail_backend.GreenSnail.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WordLearningLogRepository extends JpaRepository<WordLearningLog, Long> {

    List<WordLearningLog> findByProviderIdAndQuizDate(String providerId, LocalDate quizDate);

    boolean existsByProviderIdAndQuizDateAndIsSolvedTrue(String providerId, LocalDate quizDate);

    boolean existsByProviderIdAndWord(String providerId, Word word);

    List<WordLearningLog> findByProviderIdOrderByQuizDateDesc(String providerId);

    Optional<WordLearningLog> findByProviderIdAndWordId(String providerId, Long wordId);

    int countByProviderIdAndSolvedAt(String providerId, LocalDate date);
}
