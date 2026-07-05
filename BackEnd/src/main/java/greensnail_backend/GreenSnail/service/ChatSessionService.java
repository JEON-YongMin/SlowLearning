package greensnail_backend.GreenSnail.service;

import greensnail_backend.GreenSnail.entity.ChatSession;
import greensnail_backend.GreenSnail.entity.User;
import greensnail_backend.GreenSnail.repository.ChatSessionRepository;
import greensnail_backend.GreenSnail.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final UserRepository userRepository;

    // providerId로 userId 찾아서 저장~
    public void saveChatSession(String providerId, String sessionJson) {
        Long userId = findUserIdByProviderId(providerId);

        List<ChatSession> existingSessions = chatSessionRepository.findByUserIdOrderByCreatedAtAsc(userId);

        if (existingSessions.size() >= 5) {
            chatSessionRepository.delete(existingSessions.get(0));
        }

        ChatSession newSession = ChatSession.builder()
                .userId(userId)
                .sessionJson(sessionJson)
                .build();

        chatSessionRepository.save(newSession);
    }

    public List<ChatSession> getRecentSessions(String providerId) {
        Long userId = findUserIdByProviderId(providerId);
        return chatSessionRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId);
    }

    private Long findUserIdByProviderId(String providerId) {
        User user = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new RuntimeException("해당 providerId의 사용자를 찾을 수 없습니다: " + providerId));
        return user.getId();
    }
}
