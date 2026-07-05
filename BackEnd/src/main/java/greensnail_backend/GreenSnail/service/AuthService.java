package greensnail_backend.GreenSnail.service;

import greensnail_backend.GreenSnail.dto.RegisterRequestDto;
import greensnail_backend.GreenSnail.dto.RegisterResponseDto;
import greensnail_backend.GreenSnail.entity.User;
import greensnail_backend.GreenSnail.entity.UserType;
import greensnail_backend.GreenSnail.global.api.ErrorCode;
import greensnail_backend.GreenSnail.global.exception.CustomException;
import greensnail_backend.GreenSnail.login.dto.JwtDto;
import greensnail_backend.GreenSnail.login.dto.LoginRequestDto;
import greensnail_backend.GreenSnail.login.jwt.CustomUserDetails;
import greensnail_backend.GreenSnail.login.jwt.TokenProvider;
import greensnail_backend.GreenSnail.repository.RefreshTokenRepository;
import greensnail_backend.GreenSnail.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final S3Service s3Service;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 회원가입 (프로필 이미지 없는 버전)
     */
    @Transactional
    public RegisterResponseDto register(RegisterRequestDto registerDto) {
        String username = registerDto.getUsername();
        String password = registerDto.getPassword();

        // 아이디 형식 검사 (영어와 숫자만 허용)
        if (!username.matches("^[a-zA-Z0-9]+$")) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "아이디는 영문자와 숫자만 사용 가능합니다.");
        }

        // 아이디 길이 검사 (4~20자)
        if (username.length() < 4 || username.length() > 20) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "아이디는 4자 이상 20자 이하로 입력해주세요.");
        }

        // 아이디 중복 확인
        if (userRepository.existsByUsername(username)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "이미 사용 중인 아이디입니다.");
        }

        // 비밀번호와 비밀번호 확인 일치 여부 검사
        if (!password.equals(registerDto.getPasswordConfirm())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        // 비밀번호 정책 검사 추가
        if (password.length() < 8 || password.length() > 20) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "비밀번호는 8자 이상 20자 이하로 입력해주세요.");
        }

        // 영문자, 숫자, 특수문자 포함 여부 검사
        if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$")) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "비밀번호는 영문자, 숫자, 특수문자를 포함해야 합니다.");
        }

        // 사용자 엔티티 생성 및 저장
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .nickname(registerDto.getNickname())
                .birthDate(registerDto.getBirthDate())
                .userType(UserType.GENERAL)
                .providerId("local_" + registerDto.getUsername()) // 로컬 사용자 providerId 추가
                .deletable(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("회원가입 완료: userId={}, username={}, providerId={}", savedUser.getId(), savedUser.getUsername(), savedUser.getProviderId());

        // 응답 DTO 생성
        return RegisterResponseDto.builder()
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .nickname(savedUser.getNickname())
                .success(true)
                .build();
    }

    /**
     * 회원가입 (프로필 이미지 포함 버전)
     */
    @Transactional
    public RegisterResponseDto registerWithImage(RegisterRequestDto registerDto, MultipartFile profileImage) {
        String username = registerDto.getUsername();
        String password = registerDto.getPassword();

        // 아이디 형식 검사 (영어와 숫자만 허용)
        if (!username.matches("^[a-zA-Z0-9]+$")) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "아이디는 영문자와 숫자만 사용 가능합니다.");
        }

        // 아이디 길이 검사 (4~20자)
        if (username.length() < 4 || username.length() > 20) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "아이디는 4자 이상 20자 이하로 입력해주세요.");
        }

        // 아이디 중복 확인
        if (userRepository.existsByUsername(username)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "이미 사용 중인 아이디입니다.");
        }

        // 비밀번호와 비밀번호 확인 일치 여부 검사
        if (!password.equals(registerDto.getPasswordConfirm())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        // 비밀번호 정책 검사 추가
        if (password.length() < 8 || password.length() > 20) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "비밀번호는 8자 이상 20자 이하로 입력해주세요.");
        }

        // 영문자, 숫자, 특수문자 포함 여부 검사
        if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$")) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "비밀번호는 영문자, 숫자, 특수문자를 포함해야 합니다.");
        }

        // 사용자 엔티티 생성
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .nickname(registerDto.getNickname())
                .birthDate(registerDto.getBirthDate())
                .userType(UserType.GENERAL)
                .providerId("local_" + registerDto.getUsername()) // 로컬 사용자 providerId 추가
                .deletable(true)
                .build();

        // 프로필 이미지가 있다면 S3에 업로드
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                String imageUrl = s3Service.uploadFile(profileImage, "profile");
                user.setProfileImage(imageUrl);
                log.info("프로필 이미지 업로드 완료: {}", imageUrl);
            } catch (Exception e) {
                log.error("프로필 이미지 업로드 실패: {}", e.getMessage());
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "프로필 이미지 업로드에 실패했습니다.");
            }
        }

        User savedUser = userRepository.save(user);
        log.info("회원가입 완료: userId={}, username={}, providerId={}", savedUser.getId(), savedUser.getUsername(), savedUser.getProviderId());

        // 응답 DTO 생성
        return RegisterResponseDto.builder()
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .nickname(savedUser.getNickname())
                .success(true)
                .build();
    }

    /**
     * 사용자 이름(username)으로 회원 탈퇴 - 외래 키 제약 조건 문제 해결
     */
    @Transactional
    public void withdrawByUsername(String username, String password, HttpServletRequest request) {
        log.info("username으로 회원 탈퇴 시작: username={}", username);

        try {
            // 먼저 username으로 사용자 찾기 시도
            User user = userRepository.findByUsername(username)
                    .orElseGet(() -> {
                        // username으로 찾지 못하면 providerId로 시도
                        log.info("username으로 찾지 못함, providerId로 시도: {}", username);
                        return userRepository.findByProviderId(username)
                                .orElseThrow(() -> {
                                    log.error("username/providerId={}에 해당하는 사용자를 찾을 수 없음", username);
                                    return new CustomException(ErrorCode.ENTITY_NOT_FOUND, "사용자를 찾을 수 없습니다.");
                                });
                    });

            log.info("사용자 조회 성공: id={}, username={}, providerId={}, userType={}",
                    user.getId(), user.getUsername(), user.getProviderId(), user.getUserType());

            // 일반 회원인 경우 비밀번호 확인
            if (user.getUserType() == UserType.GENERAL) {
                if (password == null || password.trim().isEmpty()) {
                    throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "비밀번호를 입력해주세요.");
                }
                if (!passwordEncoder.matches(password, user.getPassword())) {
                    throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "비밀번호가 일치하지 않습니다.");
                }
            }

            // 탈퇴 가능 여부 확인
            if (!user.isDeletable()) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "탈퇴할 수 없는 계정입니다.");
            }

            // 회원 정보 삭제 처리 (외래 키 제약 조건 해결)
            deleteUserDataSafely(user);

        } catch (CustomException e) {
            log.error("회원 탈퇴 실패 (CustomException): username={}, error={}", username, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("회원 탈퇴 실패 (Exception): username={}, error={}", username, e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "회원 탈퇴 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 로그인
     */
    @Transactional
    public JwtDto login(LoginRequestDto loginDto) {
        // 사용자 아이디로 회원 찾기
        User user = userRepository.findByUsername(loginDto.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT_VALUE, "아이디 또는 비밀번호가 일치하지 않습니다."));

        // 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        // CustomUserDetails 생성
        UserDetails userDetails = CustomUserDetails.builder()
                .userId(user.getId())
                .providerId(user.getProviderId())
                .nickname(user.getNickname())
                .authorities(Collections.emptyList())
                .build();

        // JWT 토큰 생성
        JwtDto jwt = tokenProvider.generateTokens(userDetails);
        log.info("로그인 성공: username={}", user.getUsername());

        return jwt;
    }

    /**
     * 로그아웃
     */
    @Transactional
    public void logout(String providerId, HttpServletRequest request) {
        log.info("로그아웃 시작: providerId={}", providerId);

        try {
            // 일반 회원인 경우 (username = providerId)
            userRepository.findByUsername(providerId).ifPresent(user -> {
                log.info("일반 회원 로그아웃: userId={}, username={}", user.getId(), user.getUsername());
                refreshTokenRepository.deleteByUser(user);
            });

            // 소셜 로그인 회원인 경우 (providerId = providerId)
            userRepository.findByProviderId(providerId).ifPresent(user -> {
                log.info("소셜 로그인 회원 로그아웃: userId={}, providerId={}", user.getId(), user.getProviderId());
                refreshTokenRepository.deleteByUser(user);
            });

            log.info("로그아웃 완료: providerId={}", providerId);
        } catch (Exception e) {
            log.error("로그아웃 실패: providerId={}, error={}", providerId, e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "로그아웃 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 일반 회원 탈퇴 - 사용자 ID와 비밀번호로 탈퇴
     */
    @Transactional
    public void withdrawByUserId(Long userId, String password, HttpServletRequest request) {
        try {
            // 사용자 ID로 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND, "사용자를 찾을 수 없습니다."));

            // 일반 회원인 경우 비밀번호 확인
            if (user.getUserType() == UserType.GENERAL) {
                if (password == null || password.trim().isEmpty()) {
                    throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "비밀번호를 입력해주세요.");
                }
                if (!passwordEncoder.matches(password, user.getPassword())) {
                    throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "비밀번호가 일치하지 않습니다.");
                }
            }

            // 탈퇴 가능 여부 확인
            if (!user.isDeletable()) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "탈퇴할 수 없는 계정입니다.");
            }

            // 회원 정보 삭제 처리
            deleteUserDataSafely(user);

        } catch (CustomException e) {
            log.error("회원 탈퇴 실패 (CustomException): userId={}, error={}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("회원 탈퇴 실패 (Exception): userId={}, error={}", userId, e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "회원 탈퇴 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 소셜 로그인 회원 탈퇴 - Provider ID로 탈퇴
     */
    @Transactional
    public void withdrawByProviderId(String providerId, HttpServletRequest request) {
        log.info("소셜 회원 탈퇴 시작: providerId={}", providerId);

        try {
            // 데이터베이스에서 모든 사용자 정보 확인 (디버깅용)
            List<User> allUsers = userRepository.findAll();
            log.info("전체 사용자 수: {}", allUsers.size());

            for (User user : allUsers) {
                log.info("사용자 정보: id={}, providerId={}, username={}, deletable={}",
                        user.getId(), user.getProviderId(), user.getUsername(), user.isDeletable());
            }

            // 소셜 로그인 사용자 조회 (providerId 사용)
            User user = userRepository.findByProviderId(providerId)
                    .orElseThrow(() -> {
                        log.error("providerId={}에 해당하는 사용자를 찾을 수 없음", providerId);
                        return new CustomException(ErrorCode.ENTITY_NOT_FOUND, "사용자를 찾을 수 없습니다.");
                    });

            log.info("사용자 조회 성공: id={}, providerId={}, deletable={}",
                    user.getId(), user.getProviderId(), user.isDeletable());

            // 탈퇴 가능 여부 확인
            if (!user.isDeletable()) {
                log.error("탈퇴할 수 없는 계정: id={}, providerId={}", user.getId(), user.getProviderId());
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "탈퇴할 수 없는 계정입니다.");
            }

            // 회원 정보 삭제 처리
            deleteUserDataSafely(user);

        } catch (CustomException e) {
            log.error("소셜 회원 탈퇴 실패 (CustomException): providerId={}, error={}", providerId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("소셜 회원 탈퇴 실패 (Exception): providerId={}, error={}", providerId, e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "회원 탈퇴 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 사용자 관련 데이터 안전하게 삭제 (JdbcTemplate 사용)
     */
    private void deleteUserDataSafely(User user) {
        try {
            log.info("회원 탈퇴 데이터 삭제 시작: userId={}, providerId={}", user.getId(), user.getProviderId());

            // 1. 프로필 이미지가 있다면 S3에서 삭제 (오류가 나도 계속 진행)
            if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                try {
                    s3Service.deleteFile(user.getProfileImage());
                    log.info("프로필 이미지 삭제 완료: {}", user.getProfileImage());
                } catch (Exception e) {
                    log.warn("프로필 이미지 삭제 실패 (계속 진행): {}", e.getMessage());
                }
            }

            // 2. JdbcTemplate로 외래 키 참조 데이터들을 안전하게 삭제
            try {
                // Summary 테이블 데이터 삭제
                int deletedSummaries = jdbcTemplate.update("DELETE FROM summary WHERE user_id = ?", user.getId());
                log.info("Summary 데이터 삭제 완료: userId={}, 삭제된 레코드 수={}", user.getId(), deletedSummaries);

                // 기타 연관 테이블들도 필요시 삭제 (예시)
                // int deletedPosts = jdbcTemplate.update("DELETE FROM posts WHERE user_id = ?", user.getId());
                // log.info("Posts 데이터 삭제 완료: userId={}, 삭제된 레코드 수={}", user.getId(), deletedPosts);

                // int deletedComments = jdbcTemplate.update("DELETE FROM comments WHERE user_id = ?", user.getId());
                // log.info("Comments 데이터 삭제 완료: userId={}, 삭제된 레코드 수={}", user.getId(), deletedComments);

            } catch (Exception e) {
                log.error("외래 키 참조 데이터 삭제 실패: userId={}, error={}", user.getId(), e.getMessage());
                // 실패해도 계속 진행
            }

            // 3. RefreshToken 삭제
            try {
                refreshTokenRepository.deleteByUser(user);
                log.info("RefreshToken 삭제 완료: userId={}", user.getId());
            } catch (Exception e) {
                log.warn("RefreshToken 삭제 실패 (계속 진행): {}", e.getMessage());
            }

            // 4. 마지막에 사용자 삭제
            userRepository.deleteById(user.getId());
            log.info("회원 탈퇴 완료: userId={}, providerId={}", user.getId(), user.getProviderId());

        } catch (Exception e) {
            log.error("회원 탈퇴 데이터 삭제 중 심각한 오류 발생: userId={}, error={}", user.getId(), e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "회원 탈퇴 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 기존 deleteUserData 메서드 (호환성 유지)
     */
    @Deprecated
    private void deleteUserData(User user) {
        deleteUserDataSafely(user);
    }
}