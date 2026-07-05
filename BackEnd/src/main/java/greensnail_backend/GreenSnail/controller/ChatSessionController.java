package greensnail_backend.GreenSnail.controller;

import greensnail_backend.GreenSnail.entity.ChatSession;
import greensnail_backend.GreenSnail.login.jwt.CustomUserDetails;
import greensnail_backend.GreenSnail.service.ChatSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatsession")
public class ChatSessionController {

    private final ChatSessionService chatSessionService;

    @PostMapping("/save")
    public ResponseEntity<String> saveSession(@AuthenticationPrincipal CustomUserDetails userDetails,
                                              @RequestBody String sessionJson) {

        // providerId 기준으로 userId 찾아서 해써요
        chatSessionService.saveChatSession(userDetails.getProviderId(), sessionJson);

        return ResponseEntity.ok("saved");
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ChatSession>> getRecent(@AuthenticationPrincipal CustomUserDetails userDetails) {

        List<ChatSession> sessions = chatSessionService.getRecentSessions(userDetails.getProviderId());

        return ResponseEntity.ok(sessions);
    }
}
