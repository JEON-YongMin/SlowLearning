package greensnail_backend.GreenSnail.controller;

import greensnail_backend.GreenSnail.dto.MyPageResponseDto;
import greensnail_backend.GreenSnail.global.api.ApiResponse;
import greensnail_backend.GreenSnail.global.api.SuccessCode;
import greensnail_backend.GreenSnail.login.jwt.CustomUserDetails;
import greensnail_backend.GreenSnail.service.MyPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "마이페이지", description = "마이페이지 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/mypage")
public class MyPageController {

    private final MyPageService myPageService;

    @Operation(summary = "마이페이지 조회", description = "사용자의 마이페이지 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "마이페이지 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping
    public ApiResponse<MyPageResponseDto> getMyPage(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("마이페이지 조회 요청: providerId={}", userDetails.getProviderId());

        MyPageResponseDto myPageInfo = myPageService.getMyPageInfo(userDetails.getProviderId());

        return ApiResponse.success(myPageInfo, SuccessCode.OK);
    }
}