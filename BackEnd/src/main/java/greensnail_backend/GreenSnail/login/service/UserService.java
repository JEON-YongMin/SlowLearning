package greensnail_backend.GreenSnail.login.service;

import greensnail_backend.GreenSnail.entity.User;
import greensnail_backend.GreenSnail.global.api.ErrorCode;
import greensnail_backend.GreenSnail.global.exception.CustomException;
import greensnail_backend.GreenSnail.login.dto.JwtDto;
import greensnail_backend.GreenSnail.login.jwt.RefreshToken;
import greensnail_backend.GreenSnail.login.jwt.TokenProvider;
import greensnail_backend.GreenSnail.repository.RefreshTokenRepository;
import greensnail_backend.GreenSnail.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;
    private final JpaUserDetailsManager userDetailsManager;

    /**
     * provider_id(String)으로 회원 여부 확인
     */
    public Boolean checkMemberByProviderId(String providerId) {
        return userRepository.existsByProviderId(providerId);
    }

    /**
     * provider_id(String)으로 회원 찾기
     */
    public Optional<User> findByProviderId(String providerId) {
        return userRepository.findByProviderId(providerId);
    }

    /**
     * Refresh Token 저장 또는 갱신 (provider_id 기반)
     */
    @Transactional
    public void saveRefreshToken(String providerId, String refreshToken) {
        // User 조회 (providerId 기반)
        User user = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // RefreshToken 생성 또는 업데이트
        RefreshToken token = refreshTokenRepository.findByUser(user)
                .map(existingToken -> {
                    existingToken.updateRefreshToken(refreshToken);
                    return existingToken;
                })
                .orElseGet(() -> {
                    log.info("새 RefreshToken 생성 시도 (user_id={}, refreshToken={})",
                            user.getId(), refreshToken);
                    return RefreshToken.builder()
                            .user(user) // user와 1:1 매핑
                            .refreshToken(refreshToken)
                            .ttl(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7) // 7일 유효
                            .build();
                });

        // RefreshToken 저장
        refreshTokenRepository.save(token);
        log.info("RefreshToken 저장 완료 (user_id={})", user.getId());
    }

    /**
     * JWT 토큰 생성 및 RefreshToken 저장
     */
    @Transactional
    public JwtDto jwtMakeSave(String providerId) {
        // provider_id 기반 유저 인증
        UserDetails details = userDetailsManager.loadUserByUsername(providerId);

        // JWT 생성
        JwtDto jwt = tokenProvider.generateTokens(details);

        // provider_id 기반 Refresh Token 저장
        saveRefreshToken(providerId, jwt.getRefreshToken());
        return jwt;
    }

    /**
     * Refresh Token 기반 Access Token 재발급
     */
    @Transactional
    public JwtDto reissue(HttpServletRequest request) {
        log.info("Access Token 재발급 요청 시작...");

        // Access Token에서 providerId 추출
        String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }

        // Access Token의 Claims 파싱 (만료된 토큰이어도 Claims 추출 가능)
        Claims claims;
        try {
            claims = tokenProvider.parseClaims(accessToken);
        } catch (Exception e) {
            log.error("Access Token이 유효하지 않음: {}", e.getMessage());
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "토큰이 유효하지 않습니다.");
        }

        String providerId = claims.getSubject();
        log.info("Access Token에서 추출한 providerId: {}", providerId);

        if (providerId == null || providerId.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "토큰이 유효하지 않습니다.");
        }

        // providerId 기반으로 User 조회
        Optional<User> userOpt = findByProviderId(providerId);
        if (userOpt.isEmpty()) {
            log.error("providerId={} 에 해당하는 사용자를 찾을 수 없음", providerId);
            throw new CustomException(ErrorCode.ENTITY_NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }
        User user = userOpt.get();
        log.info("User 조회 성공 (user_id={}, providerId={})", user.getId(), user.getProviderId());

        // DB에서 Refresh Token 조회
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE, "리프레시 토큰이 유효하지 않습니다."));

        // Refresh Token 유효성 검사
        if (!tokenProvider.validateToken(refreshTokenEntity.getRefreshToken())) {
            refreshTokenRepository.deleteByUser(user); // 만료된 Refresh Token 삭제
            log.error("Refresh Token이 만료됨 - 삭제 완료 (user_id={})", user.getId());
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "토큰이 만료되었습니다.");
        }

        // 새 Access Token 발급
        UserDetails userDetails = userDetailsManager.loadUserByUsername(providerId);
        JwtDto newJwt = tokenProvider.generateTokens(userDetails);
        log.info("새로운 Access Token 발급 완료");

        // Refresh Token 갱신 (기존 토큰 삭제 후 새로운 토큰 저장)
        refreshTokenEntity.updateRefreshToken(newJwt.getRefreshToken());
        refreshTokenRepository.save(refreshTokenEntity);

        return newJwt;
    }

    /**
     * 로그아웃 (Refresh Token 삭제)
     */
    @Transactional
    public void logout(HttpServletRequest request) {
        // Access Token에서 providerId 추출
        String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }

        Claims claims = tokenProvider.parseClaims(accessToken);
        String providerId = claims.getSubject();

        if (providerId == null || providerId.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "토큰이 유효하지 않습니다.");
        }

        // providerId 기반으로 User 조회
        Optional<User> userOpt = findByProviderId(providerId);
        if (userOpt.isEmpty()) {
            throw new CustomException(ErrorCode.ENTITY_NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }
        User user = userOpt.get();

        // Refresh Token 삭제 (DB에서 제거)
        refreshTokenRepository.deleteByUser(user);
    }
}