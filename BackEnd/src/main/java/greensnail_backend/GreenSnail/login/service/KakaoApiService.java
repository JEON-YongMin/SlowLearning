// 파일 위치: src/main/java/greensnail_backend/GreenSnail/login/service/KakaoApiService.java

package greensnail_backend.GreenSnail.login.service;

import greensnail_backend.GreenSnail.global.api.ErrorCode;
import greensnail_backend.GreenSnail.global.exception.CustomException;
import greensnail_backend.GreenSnail.login.dto.KakaoUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoApiService {

    private final RestTemplate restTemplate;
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    /**
     * 카카오 accessToken으로 사용자 정보 조회
     */
    public KakaoUserInfo getUserInfoFromKakao(String accessToken) {
        try {
            log.info("카카오 API 호출 시작");

            // 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 카카오 API 호출
            ResponseEntity<Map> response = restTemplate.exchange(
                    KAKAO_USER_INFO_URL,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("카카오 API 호출 실패: status={}", response.getStatusCode());
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "카카오 토큰이 유효하지 않습니다.");
            }

            Map<String, Object> responseBody = response.getBody();
            String providerId = responseBody.get("id").toString();

            // properties에서 닉네임 추출
            Map<String, Object> properties = (Map<String, Object>) responseBody.get("properties");
            String nickname = properties != null ?
                    (String) properties.getOrDefault("nickname", "카카오사용자") : "카카오사용자";

            log.info("카카오 사용자 정보 조회 성공: providerId={}, nickname={}", providerId, nickname);

            return new KakaoUserInfo(providerId, nickname);

        } catch (Exception e) {
            log.error("카카오 API 호출 중 오류 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "카카오 토큰 검증에 실패했습니다.");
        }
    }
}