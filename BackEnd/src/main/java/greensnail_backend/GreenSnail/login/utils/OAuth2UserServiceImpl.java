package greensnail_backend.GreenSnail.login.utils;

import greensnail_backend.GreenSnail.entity.User;
import greensnail_backend.GreenSnail.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 카카오 OAuth2 유저 서비스
 * - provider_id 기반으로 회원 확인 및 신규 회원 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserServiceImpl extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    /**
     * 카카오 로그인 유저 정보 조회 및 회원 처리 (provider_id 기반)
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("카카오 OAuth2 로그인 시도");

        // 카카오 유저 정보 추출
        String providerId = oAuth2User.getAttributes().get("id").toString();
        String nickname = ((Map<String, Object>) oAuth2User.getAttributes().get("properties")).getOrDefault("nickname", "카카오사용자").toString();

        // provider_id 기반 회원 여부만 확인
        Optional<User> userOptional = userRepository.findByProviderId(providerId);

        // 유저 정보를 OAuth2User의 attributes에 담아서 반환
        Map<String, Object> extendedAttributes = new HashMap<>(oAuth2User.getAttributes());
        extendedAttributes.put("provider_id", providerId);
        extendedAttributes.put("nickname", nickname);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                extendedAttributes,
                "provider_id"
        );
    }
}