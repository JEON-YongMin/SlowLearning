package greensnail_backend.GreenSnail.controller;

import greensnail_backend.GreenSnail.dto.ReportRequestDto;
import greensnail_backend.GreenSnail.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/posts/{postId}")
    public ResponseEntity<Void> reportPost(@PathVariable Long postId,
                                           @RequestParam String providerId,
                                           @RequestBody ReportRequestDto dto) {
        reportService.reportPost(postId, providerId, dto.getReason());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/comments/{commentId}")
    public ResponseEntity<Void> reportComment(@PathVariable Long commentId,
                                              @RequestParam String providerId,
                                              @RequestBody ReportRequestDto dto) {
        reportService.reportComment(commentId, providerId, dto.getReason());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{userId}")
    public ResponseEntity<Void> reportUser(@PathVariable Long userId,
                                           @RequestParam String providerId,
                                           @RequestBody ReportRequestDto dto) {
        reportService.reportUser(userId, providerId, dto.getReason());
        return ResponseEntity.ok().build();
    }
}