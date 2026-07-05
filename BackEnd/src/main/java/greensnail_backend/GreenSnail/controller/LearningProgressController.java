package greensnail_backend.GreenSnail.controller;

import greensnail_backend.GreenSnail.global.api.ApiResponse;
import greensnail_backend.GreenSnail.global.api.SuccessCode;
import greensnail_backend.GreenSnail.global.exception.CustomException;
import greensnail_backend.GreenSnail.global.api.ErrorCode;
import greensnail_backend.GreenSnail.service.LearningProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
@Tag(name = "캘린더 호출용 Learning Progress", description = "입력한 날짜가 포함된 달의 학습 내역을 String의 숫자열로 반환")
@SecurityRequirement(name = "bearerAuth")
public class LearningProgressController {

    private final LearningProgressService learningProgressService;

    @GetMapping
    @Operation(
            summary = "learning progress 불러오기",
            description = "입력한 날짜에 해당하는 달의 학습 이력 호출하는 API"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Progress retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ApiResponse<String> getProgress(
            @Parameter(description = "Date within the target month (yyyy-MM-dd)", required = true, example = "2025-06-")
            @RequestParam LocalDate date) {
        String providerId = extractProviderIdFromSecurityContext();
        learningProgressService.updateProgress(providerId, date);
        String progress = learningProgressService.getProgress(providerId, date);
        return ApiResponse.success(progress, SuccessCode.OK);
    }

    private String extractProviderIdFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "인증 정보가 없습니다.");
        }

        String providerId = authentication.getName(); // CustomUserDetails.getUsername() 호출, providerId 반환
        if (providerId == null || providerId.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "인증 정보에서 providerId를 추출할 수 없습니다.");
        }

        return providerId;
    }
}