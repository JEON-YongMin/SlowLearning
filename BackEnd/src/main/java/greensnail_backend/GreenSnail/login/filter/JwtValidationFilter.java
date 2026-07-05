package greensnail_backend.GreenSnail.login.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import greensnail_backend.GreenSnail.global.api.ApiResponse;
import greensnail_backend.GreenSnail.global.api.ErrorCode;
import greensnail_backend.GreenSnail.login.jwt.CustomUserDetails;
import greensnail_backend.GreenSnail.login.jwt.TokenProvider;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * JWT 유효성 검사 (provider_id 기반)
 * - 모든 요청에서 JWT 유효성을 검사해 인증 객체(SecurityContext)를 주입
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtValidationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final Environment environment;

    // 필터 제외 경로 패턴 목록 - 모든 Swagger 관련 경로 포함
    private final List<String> excludedPatterns = Arrays.asList(
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/h2-console/**",
            "/oauth2/authorization/**",
            "/login/oauth2/code/**",
            "/login/success",
            "/auth/login/**",
            "/api/test/**",
            "/api/users/reissue",
            "/api/auth/token",
            "/api/auth/register",
            "/api/auth/register-with-image",
            "/api/auth/login",
            "/uploads/**",
            "/payment/success",
            "/error",
            "/favicon.ico",
            "/auth/kakao"
    );

    /**
     * 요청 경로가 제외 패턴과 일치하는지 확인
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        // 개발 환경에서는 모든 요청 허용 (선택적)
        String[] activeProfiles = environment.getActiveProfiles();
        boolean isDevProfile = Arrays.stream(activeProfiles)
                .anyMatch(profile -> profile.equals("dev"));

        if (isDevProfile) {
            log.debug("개발 환경에서 모든 요청 허용: {}", path);
            return true;
        }

        // 패턴 매칭을 사용하여 경로 필터링
        boolean shouldExclude = excludedPatterns.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));

        if (shouldExclude) {
            log.debug("필터에서 제외된 경로: {}", path);
        }

        return shouldExclude;
    }

    /**
     * 요청 시 JWT 인증 필터링
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {
        log.debug("요청 URL: {}", request.getServletPath());

        // 제외된 경로는 필터링하지 않고 통과
        if (shouldNotFilter(request)) {
            chain.doFilter(request, response);
            return;
        }

        // Authorization 헤더에서 토큰 가져오기
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // 토큰이 없거나 형식이 잘못된 경우
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("인증 헤더 없음 또는 잘못된 형식: {}", request.getServletPath());
            sendErrorResponse(response, ErrorCode.UNAUTHORIZED, "인증이 필요합니다");
            return;
        }

        // "Bearer " 접두사 제거
        String token = authHeader.substring(7);

        try {
            // 토큰 유효성 검사
            if (!tokenProvider.validateToken(token)) {
                log.warn("유효하지 않은 토큰: {}", request.getServletPath());
                sendErrorResponse(response, ErrorCode.INVALID_INPUT_VALUE, "유효한 토큰이 없습니다");
                return;
            }

            // Claims 추출 및 provider_id 가져오기
            var claims = tokenProvider.parseClaims(token);
            String providerId = claims.getSubject(); // subject에 provider_id 포함됨

            if (providerId == null || providerId.isEmpty()) {
                log.warn("provider_id 추출 실패: {}", request.getServletPath());
                sendErrorResponse(response, ErrorCode.INVALID_INPUT_VALUE, "유효한 토큰이 없습니다");
                return;
            }

            // 권한 정보 추출 및 SecurityContextHolder에 주입
            var authorities = tokenProvider.getAuthFromClaims(claims);

            // CustomUserDetails 객체로 변경
            CustomUserDetails userDetails = new CustomUserDetails(
                    providerId,
                    "",
                    authorities
            );

            // 인증 객체 생성
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(authToken);
            log.debug("JWT 인증 성공 - providerId: {}", providerId);

            // 다음 필터로 요청 전달
            chain.doFilter(request, response);

        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.warn("잘못된 서명: {}", e.getMessage());
            sendErrorResponse(response, ErrorCode.INVALID_INPUT_VALUE, "유효한 토큰이 없습니다");
        } catch (ExpiredJwtException e) {
            log.warn("토큰 만료: {}", e.getMessage());
            sendErrorResponse(response, ErrorCode.INVALID_INPUT_VALUE, "토큰이 만료되었습니다");
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 토큰: {}", e.getMessage());
            sendErrorResponse(response, ErrorCode.INVALID_INPUT_VALUE, "유효한 토큰이 없습니다");
        } catch (IllegalArgumentException e) {
            log.warn("유효하지 않은 요청: {}", e.getMessage());
            sendErrorResponse(response, ErrorCode.INVALID_INPUT_VALUE, "유효한 토큰이 없습니다");
        } catch (Exception e) {
            log.error("알 수 없는 예외 발생: {}", e.getMessage());
            sendErrorResponse(response, ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 인증 실패 시 에러 응답 반환 (401 Unauthorized)
     */
    private void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        sendErrorResponse(response, errorCode, errorCode.getMessage());
    }

    private void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(
                new ObjectMapper().writeValueAsString(ApiResponse.error(errorCode, message))
        );
    }
}