package greensnail_backend.GreenSnail.controller;

import greensnail_backend.GreenSnail.entity.User;
import greensnail_backend.GreenSnail.global.api.ApiResponse;
import greensnail_backend.GreenSnail.login.jwt.CustomUserDetails;
import greensnail_backend.GreenSnail.login.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "사용자", description = "사용자 정보 관리 API")
public class UserProfileController {

    private final UserService userService;

    @GetMapping("/profile")
    @Operation(
            summary = "사용자 프로필 조회",
            description = "JWT 토큰으로 인증된 사용자의 프로필 정보를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<User>> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("사용자 프로필 조회 요청: providerId={}", userDetails.getProviderId());

        User user = userService.findByProviderId(userDetails.getProviderId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return ResponseEntity.ok(ApiResponse.success(user));
    }
}