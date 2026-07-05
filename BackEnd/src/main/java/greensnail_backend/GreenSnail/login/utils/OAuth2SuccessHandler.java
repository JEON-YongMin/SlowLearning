package greensnail_backend.GreenSnail.login.utils;

import greensnail_backend.GreenSnail.entity.Address;
import greensnail_backend.GreenSnail.entity.User;
import greensnail_backend.GreenSnail.login.dto.JwtDto;
import greensnail_backend.GreenSnail.login.jwt.CustomUserDetails;
import greensnail_backend.GreenSnail.login.service.JpaUserDetailsManager;
import greensnail_backend.GreenSnail.login.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * OAuth2 로그인 성공 시 처리 핸들러
 * - provider_id 기반 회원 확인 및 JWT 발급
 * - 프론트엔드로 리다이렉트 (JWT 토큰 포함)
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JpaUserDetailsManager jpaUserDetailsManager;
    private final UserService userService;

    @Value("${frontend.redirect-url:http://localhost:3000/login/callback}")
    private String frontendRedirectUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        // OAuth2User 정보 추출
        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        String providerId = oAuth2User.getAttribute("provider_id").toString();
        String nickname = oAuth2User.getAttribute("nickname").toString();

        log.info("OAuth2 로그인 성공: providerId={}, nickname={}", providerId, nickname);

        // 신규 회원 등록 (Security 인증 등록)
        if (!jpaUserDetailsManager.userExists(providerId)) {
            // User 엔티티 생성
            User newUser = User.builder()
                    .providerId(providerId)
                    .nickname(nickname)
                    .deletable(true)
                    .build();

            newUser.setAddress(new Address("10540", "경기도 고양시 덕양구 항공대학로 76", "한국항공대학교"));

            // Security 인증 등록
            CustomUserDetails userDetails = new CustomUserDetails(newUser);
            jpaUserDetailsManager.createUser(userDetails);
            log.info("신규 회원 등록 완료 (providerId={})", providerId);
        } else {
            log.info("기존 회원 로그인 (providerId={})", providerId);
        }

        // JWT 발급
        JwtDto jwt = userService.jwtMakeSave(providerId);
        log.info("JWT 발급 및 RefreshToken 저장 완료 (providerId: {})", providerId);

        // 프론트엔드로 리다이렉트 (Query Parameter로 JWT 전달)
        String redirectUrl = UriComponentsBuilder.fromUriString(frontendRedirectUrl)
                .queryParam("accessToken", jwt.getAccessToken())
                .queryParam("refreshToken", jwt.getRefreshToken())
                .build()
                .toUriString();

        log.info("프론트엔드로 리다이렉트: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}