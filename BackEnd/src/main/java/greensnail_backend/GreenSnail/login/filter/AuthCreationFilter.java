package greensnail_backend.GreenSnail.login.filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import greensnail_backend.GreenSnail.login.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 기반 인증 필터 (provider_id 기반)
 * - /api/users/reissue 요청 시 AccessToken으로 provider_id 기반 인증 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthCreationFilter extends OncePerRequestFilter {
    private final TokenProvider tokenProvider;

    /**
     * 특정 URL(/api/users/reissue)만 필터 적용
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // 오직 /api/users/reissue 요청에서만 동작
        return !request.getServletPath().equals("/api/users/reissue");
    }

    /**
     * JWT 기반 provider_id 인증 처리
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        log.info("요청 URL: {}", request.getServletPath());

        // Authorization 헤더에서 Bearer 토큰 추출
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        log.info("Authorization 헤더 값: {}", authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring("Bearer ".length());

            // 토큰에서 Claims 추출 및 provider_id 가져오기
            Claims claims = tokenProvider.parseClaims(token);
            String providerId = claims.getSubject(); // subject에 provider_id 포함됨

            log.info("재발급 요청: provider_id 기반 익명 인증 객체 생성 (provider_id: {})", providerId);

            // 익명 인증 객체 생성 (provider_id 포함)
            Authentication authentication = new AnonymousAuthenticationToken(
                    providerId,                            // provider_id를 key로 사용
                    "anonymousUser-" + providerId,         // 인증된 사용자 표시
                    tokenProvider.getAuthFromClaims(claims)
            );

            // SecurityContext에 인증 객체 주입
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            log.info("provider_id 기반 익명 인증 객체 생성 및 SecurityContextHolder 주입 완료 (provider_id: {})", providerId);
        }

        // 다음 필터 실행
        filterChain.doFilter(request, response);
    }
}