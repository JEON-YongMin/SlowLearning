package greensnail_backend.GreenSnail.login.service;

import greensnail_backend.GreenSnail.entity.User;
import greensnail_backend.GreenSnail.global.api.ErrorCode;
import greensnail_backend.GreenSnail.global.exception.CustomException;
import greensnail_backend.GreenSnail.login.jwt.CustomUserDetails;
import greensnail_backend.GreenSnail.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JpaUserDetailsManager implements UserDetailsManager {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // PasswordEncoder를 의존성 주입
    public JpaUserDetailsManager(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Spring Security 내부에서 사용하는 사용자 정보 로드
     * providerId 기반으로 로드
     */
    @Override
    public UserDetails loadUserByUsername(String providerId) throws UsernameNotFoundException {
        User user = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> {
                    log.warn("유저 정보 없음 (provider_id): {}", providerId);
                    throw new CustomException(ErrorCode.ENTITY_NOT_FOUND, "사용자를 찾을 수 없습니다.");
                });
        return CustomUserDetails.fromEntity(user);
    }

    /**
     * 사용자 생성 (카카오 로그인 시 신규 가입 처리)
     */
    @Override
    public void createUser(UserDetails user) {
        log.info("사용자 생성 시도 중 (provider_id): {}", user.getUsername());

        if (userExists(user.getUsername())) {
            log.warn("이미 존재하는 사용자 (provider_id): {}", user.getUsername());
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "이미 사용 중인 사용자 아이디입니다.");
        }

        try {
            User newUser = ((CustomUserDetails) user).toEntity();
            userRepository.save(newUser);
            log.info("사용자 생성 완료 (provider_id): {}", user.getUsername());
        } catch (ClassCastException e) {
            log.error("UserDetails → CustomUserDetails 변환 실패 (provider_id): {}", user.getUsername(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "내부 서버 오류가 발생했습니다.");
        }
    }

    /**
     * 사용자 존재 여부 확인 (providerId 기반)
     */
    @Override
    public boolean userExists(String providerId) {
        log.info("사용자 존재 여부 확인 (provider_id): {}", providerId);
        return userRepository.existsByProviderId(providerId);
    }

    /**
     * 사용자 정보 업데이트 (미구현)
     */
    @Override
    public void updateUser(UserDetails user) {
        log.error("사용자 정보 업데이트는 지원되지 않음 (provider_id): {}", user.getUsername());
        throw new UnsupportedOperationException("사용자 업데이트 기능은 아직 지원되지 않습니다.");
    }

    /**
     * 사용자 삭제 (미구현)
     */
    @Override
    public void deleteUser(String providerId) {
        log.error("사용자 삭제는 지원되지 않음 (provider_id): {}", providerId);
        throw new UnsupportedOperationException("사용자 삭제 기능은 아직 지원되지 않습니다.");
    }

    /**
     * 비밀번호 변경 (소셜 로그인은 비밀번호 사용 안함)
     */
    @Override
    public void changePassword(String oldPassword, String newPassword) {
        log.error("비밀번호 변경은 지원되지 않음.");
        throw new UnsupportedOperationException("비밀번호 변경 기능은 아직 지원되지 않습니다.");
    }
}