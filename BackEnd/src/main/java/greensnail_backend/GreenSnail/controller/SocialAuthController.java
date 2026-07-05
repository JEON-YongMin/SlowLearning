// 파일 위치: src/main/java/greensnail_backend/GreenSnail/controller/SocialAuthController.java

package greensnail_backend.GreenSnail.controller;

import greensnail_backend.GreenSnail.entity.Address;
import greensnail_backend.GreenSnail.entity.User;
import greensnail_backend.GreenSnail.global.api.ApiResponse;
import greensnail_backend.GreenSnail.login.dto.JwtDto;
import greensnail_backend.GreenSnail.login.dto.KakaoLoginRequest;
import greensnail_backend.GreenSnail.login.dto.KakaoLoginResponse;
import greensnail_backend.GreenSnail.login.dto.KakaoUserInfo;
import greensnail_backend.GreenSnail.login.jwt.CustomUserDetails;
import greensnail_backend.GreenSnail.login.service.JpaUserDetailsManager;
import greensnail_backend.GreenSnail.login.service.KakaoApiService;
import greensnail_backend.GreenSnail.login.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "소셜 인증", description = "카카오 소셜 로그인 API")
public class SocialAuthController {

    private final KakaoApiService kakaoApiService;
    private final UserService userService;
    private final JpaUserDetailsManager jpaUserDetailsManager;

    @PostMapping("/kakao")
    @Operation(
            summary = "카카오 로그인",
            description = "카카오에서 발급한 accessToken을 전달하면, 서버가 자체 JWT를 발급해 반환합니다."
    )
    public ResponseEntity<ApiResponse<KakaoLoginResponse>> kakaoLogin(
            @Valid @RequestBody KakaoLoginRequest request) {

        log.info("카카오 소셜 로그인 요청 시작");

        // 1. 카카오 API로 사용자 정보 조회
        KakaoUserInfo kakaoUserInfo = kakaoApiService.getUserInfoFromKakao(request.getAccessToken());
        String providerId = kakaoUserInfo.getProviderId();
        String nickname = kakaoUserInfo.getNickname();

        log.info("카카오 사용자 정보 조회 완료: providerId={}, nickname={}", providerId, nickname);

        // 2. 기존 회원 여부 확인 및 신규 회원 등록
        User user;
        if (!jpaUserDetailsManager.userExists(providerId)) {
            // 신규 회원 등록
            user = User.builder()
                    .providerId(providerId)
                    .nickname(nickname)
                    .deletable(true)
                    .build();

            // 기본 주소 설정
            user.setAddress(new Address("10540", "경기도 고양시 덕양구 항공대학로 76", "한국항공대학교"));

            // Security 인증 등록
            CustomUserDetails userDetails = new CustomUserDetails(user);
            jpaUserDetailsManager.createUser(userDetails);

            // 저장된 사용자 정보 다시 조회
            Optional<User> savedUser = userService.findByProviderId(providerId);
            user = savedUser.orElseThrow(() ->
                    new RuntimeException("사용자 등록 후 조회에 실패했습니다."));

            log.info("신규 회원 등록 완료: userId={}, providerId={}", user.getId(), providerId);
        } else {
            // 기존 회원 조회
            user = userService.findByProviderId(providerId)
                    .orElseThrow(() -> new RuntimeException("기존 회원 조회에 실패했습니다."));
            log.info("기존 회원 로그인: userId={}, providerId={}", user.getId(), providerId);
        }

        // 3. JWT 토큰 발급
        JwtDto jwt = userService.jwtMakeSave(providerId);
        log.info("JWT 토큰 발급 완료: providerId={}", providerId);

        // 4. 응답 데이터 구성
        KakaoLoginResponse response = KakaoLoginResponse.builder()
                .accessToken(jwt.getAccessToken())
                .refreshToken(jwt.getRefreshToken())
                .userInfo(KakaoLoginResponse.UserInfo.builder()
                        .userId(user.getId())
                        .providerId(user.getProviderId())
                        .nickname(user.getNickname())
                        .build())
                .build();

        log.info("카카오 소셜 로그인 완료: userId={}", user.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}