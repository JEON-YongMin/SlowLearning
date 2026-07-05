package greensnail_backend.GreenSnail.controller;

import greensnail_backend.GreenSnail.dto.DiaryRequestDto;
import greensnail_backend.GreenSnail.dto.DiaryResponseDto;
import greensnail_backend.GreenSnail.entity.Diary;
import greensnail_backend.GreenSnail.entity.User;
import greensnail_backend.GreenSnail.login.jwt.CustomUserDetails;
import greensnail_backend.GreenSnail.repository.UserRepository;
import greensnail_backend.GreenSnail.service.DiaryService;
import greensnail_backend.GreenSnail.utils.DateFormatter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;
    private final UserRepository userRepository;

    private User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            Long userId = userDetails.getUserId();
            if (userId != null) {
                return userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다. userId=" + userId));
            }

            String providerId = userDetails.getProviderId();
            if (providerId != null) {
                return userRepository.findByProviderId(providerId)
                        .orElseThrow(() -> new RuntimeException("해당 사용자가 존재하지 않습니다. providerId=" + providerId));
            }

            throw new RuntimeException("userId와 providerId가 모두 null입니다.");
        } else {
            throw new RuntimeException("인증 정보가 없습니다.");
        }
    }

    /**
     * 일기 작성 API
     * @param date 작성할 날짜
     * @param request 일기 내용
     * @return 작성된 일기
     */
    @Operation(summary = "일기 작성", description = "사용자가 지정한 날짜에 일기를 작성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "일기 작성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 날짜 형식 또는 미래 날짜"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PostMapping
    public ResponseEntity<?> createDiary(
            @Parameter(description = "작성할 날짜") @RequestParam String date,
            @Parameter(description = "작성할 일기의 내용") @RequestBody DiaryRequestDto request) {

        String formattedDate = DateFormatter.formatDate(date);
        LocalDate localDate = LocalDate.parse(formattedDate);

        // 미래 날짜 X
        if (localDate.isAfter(LocalDate.now())) {
            return ResponseEntity.badRequest().body("미래 날짜에는 일기를 작성할 수 없습니다.");
        }

        User user = getAuthenticatedUser();
        Diary diary = diaryService.createDiary(user, localDate, request.getContent());
        return ResponseEntity.ok(DiaryResponseDto.fromEntity(diary));
    }

    /**
     * 일기 피드백 수정 API
     * @param date 피드백을 수정할 날짜
     * @param feedback 피드백 내용
     * @return 수정된 일기
     */
    @Operation(summary = "일기 피드백 수정", description = "사용자가 일기에 대한 AI 피드백을 제공합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "피드백 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 날짜 형식"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @PatchMapping("/feedback")
    public ResponseEntity<DiaryResponseDto> updateFeedback(
            @Parameter(description = "피드백을 업데이트할 날짜") @RequestParam String date,
            @Parameter(description = "피드백 내용") @RequestBody String feedback) {

        String formattedDate = DateFormatter.formatDate(date);
        LocalDate localDate = LocalDate.parse(formattedDate);

        // 피드백 업데이트
        User user = getAuthenticatedUser();
        Diary updatedDiary = diaryService.updateAiFeedback(user, localDate, feedback);
        return ResponseEntity.ok(DiaryResponseDto.fromEntity(updatedDiary));
    }

    /**
     * 특정 날짜의 일기 조회 API
     * @param date 조회할 날짜
     * @return 조회된 일기 또는 "작성된 일기가 없습니다." 메시지
     */
    @Operation(summary = "일기 조회", description = "사용자가 작성한 특정 날짜의 일기를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "일기 조회 성공"),
            @ApiResponse(responseCode = "404", description = "일기가 존재하지 않음")
    })
    @GetMapping
    public ResponseEntity<?> getDiaryByDate(
            @Parameter(description = "조회할 날짜") @RequestParam String date) {

        String formattedDate = DateFormatter.formatDate(date);
        LocalDate localDate = LocalDate.parse(formattedDate);

        User user = getAuthenticatedUser();

        try {
            Diary diary = diaryService.getDiaryByDate(user, localDate);
            return ResponseEntity.ok(DiaryResponseDto.fromEntity(diary));
        } catch (Exception e) {
            return ResponseEntity.ok("작성된 일기가 없습니다.");
        }
    }
}
