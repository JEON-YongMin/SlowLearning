package greensnail_backend.GreenSnail.repository;

import greensnail_backend.GreenSnail.entity.WordQuiz;
import greensnail_backend.GreenSnail.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WordQuizRepository extends JpaRepository<WordQuiz, Long> {

    Optional<WordQuiz> findByWord(Word word);
}
