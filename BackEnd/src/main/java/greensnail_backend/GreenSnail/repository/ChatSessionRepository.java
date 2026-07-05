package greensnail_backend.GreenSnail.repository;

import greensnail_backend.GreenSnail.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    List<ChatSession> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);
    List<ChatSession> findByUserIdOrderByCreatedAtAsc(Long userId);
}
