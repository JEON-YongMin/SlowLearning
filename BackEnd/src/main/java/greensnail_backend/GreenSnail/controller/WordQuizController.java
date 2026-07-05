package greensnail_backend.GreenSnail.controller;

import greensnail_backend.GreenSnail.dto.QuizAnswerDto;
import greensnail_backend.GreenSnail.dto.WordQuizItem;
import greensnail_backend.GreenSnail.dto.WordQuizResult;
import greensnail_backend.GreenSnail.dto.WordQuizResultItem;
import greensnail_backend.GreenSnail.login.jwt.CustomUserDetails;
import greensnail_backend.GreenSnail.service.WordQuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/word-quiz")
@RequiredArgsConstructor
public class WordQuizController {

    private final WordQuizService wordQuizService;

    @GetMapping("/{date}")
    public ResponseEntity<WordQuizResult> getWordQuizForDate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        String providerId = userDetails.getProviderId();
        WordQuizResult result = wordQuizService.getWordsForDate(providerId, date);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{date}/solve")
    public ResponseEntity<Void> markAsSolved(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        String providerId = userDetails.getProviderId();
        wordQuizService.markQuizAsSolved(providerId, date);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{date}/submit")
    public ResponseEntity<List<WordQuizResultItem>> submitQuizAnswers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody List<QuizAnswerDto> answers) {

        String providerId = userDetails.getProviderId();
        List<WordQuizResultItem> resultItems = wordQuizService.checkQuizAnswers(providerId, date, answers);
        return ResponseEntity.ok(resultItems);
    }

    @GetMapping("/wrong-notes")
    public ResponseEntity<List<WordQuizItem>> getWrongAnswerNotes(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String providerId = userDetails.getProviderId();
        List<WordQuizItem> notes = wordQuizService.getWrongAnswerNotes(providerId);
        return ResponseEntity.ok(notes);
    }

    @PostMapping("/wrong-notes/submit")
    public ResponseEntity<WordQuizResultItem> submitWrongNote(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody QuizAnswerDto answer) {

        String providerId = userDetails.getProviderId();
        WordQuizResultItem result = wordQuizService.submitWrongNoteAnswer(providerId, answer);
        return ResponseEntity.ok(result);
    }
}