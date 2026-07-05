package greensnail_backend.GreenSnail.repository;

import greensnail_backend.GreenSnail.entity.Word;
import greensnail_backend.GreenSnail.entity.WrongAnswerNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WrongAnswerNoteRepository extends JpaRepository<WrongAnswerNote, Long> {

    List<WrongAnswerNote> findByProviderIdAndIsSolvedFalse(String providerId);

    boolean existsByProviderIdAndWord(String providerId, Word word);

    Optional<WrongAnswerNote> findByProviderIdAndWordId(String providerId, Long wordId);
}
