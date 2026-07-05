package greensnail_backend.GreenSnail.controller;

import greensnail_backend.GreenSnail.dto.DiagnosisRequestDto;
import greensnail_backend.GreenSnail.dto.DiagnosisResultDto;
import greensnail_backend.GreenSnail.dto.QuestionDto;
import greensnail_backend.GreenSnail.entity.AgeGroup;
import greensnail_backend.GreenSnail.entity.DiagnosisResult;
import greensnail_backend.GreenSnail.entity.User;
import greensnail_backend.GreenSnail.login.jwt.CustomUserDetails;
import greensnail_backend.GreenSnail.repository.UserRepository;
import greensnail_backend.GreenSnail.service.DiagnosisService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diagnosis")
public class DiagnosisController {

    private final DiagnosisService diagnosisService;
    private final UserRepository userRepository;

    public DiagnosisController(DiagnosisService diagnosisService, UserRepository userRepository) {
        this.diagnosisService = diagnosisService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "연령대별 진단 질문 조회", description = "사용자의 연령대(AgeGroup)를 기준으로 진단 질문 목록을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "질문 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 연령대 파라미터")
    })
    @GetMapping("/questions")
    public List<QuestionDto> getQuestions(
            @Parameter(description = "연령대 (UNDER_14, OVER_14 등)") @RequestParam AgeGroup ageGroup) {
        return diagnosisService.getQuestionsByAgeGroup(ageGroup);
    }

    @Operation(summary = "진단 결과 제출", description = "사용자의 응답을 기반으로 진단 결과를 계산하고 저장합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "진단 결과 계산 및 저장 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 요청")
    })
    @PostMapping("/result")
    public DiagnosisResultDto submitResult(
            @Parameter(description = "사용자의 진단 응답 데이터") @RequestBody DiagnosisRequestDto requestDto,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String providerId = userDetails.getProviderId();
        User user = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없습니다."));

        return diagnosisService.calculateResult(requestDto, user);
    }

    @Operation(summary = "모든 진단 결과 조회", description = "DB에 저장된 모든 사용자의 진단 결과 리스트를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "진단 결과 리스트 조회 성공")
    })
    @GetMapping("/results")
    public List<DiagnosisResult> getAllResults() {
        return diagnosisService.getAllResults();
    }
}
