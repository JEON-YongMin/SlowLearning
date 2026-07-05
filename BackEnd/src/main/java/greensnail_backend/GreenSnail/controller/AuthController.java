package greensnail_backend.GreenSnail.controller;

import greensnail_backend.GreenSnail.dto.RegisterRequestDto;
import greensnail_backend.GreenSnail.dto.RegisterResponseDto;
import greensnail_backend.GreenSnail.global.api.ApiResponse;
import greensnail_backend.GreenSnail.global.api.ErrorCode;
import greensnail_backend.GreenSnail.global.api.SuccessCode;
import greensnail_backend.GreenSnail.global.exception.CustomException;
import greensnail_backend.GreenSnail.login.dto.JwtDto;
import greensnail_backend.GreenSnail.login.dto.LoginRequestDto;
import greensnail_backend.GreenSnail.login.jwt.CustomUserDetails;
import greensnail_backend.GreenSnail.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "인증", description = "인증 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "새로운 사용자 등록 API 입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 사용자", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/register")
    public ApiResponse<RegisterResponseDto> register(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("passwordConfirm") String passwordConfirm,
            @RequestParam("name") String name,
            @RequestParam("nickname") String nickname,
            @RequestParam(value = "birthDate", required = false) String birthDate) {

        log.info("회원가입 요청 수신: username={}", username);

        // RegisterRequestDto 객체 생성
        RegisterRequestDto registerDto = RegisterRequestDto.builder()
                .username(username)
                .password(password)
                .passwordConfirm(passwordConfirm)
                .name(name)
                .nickname(nickname)
                .birthDate(birthDate)
                .build();

        RegisterResponseDto response = authService.register(registerDto);
        return ApiResponse.success(response, SuccessCode.CREATED);
    }

    @Operation(summary = "회원가입 (프로필 이미지 포함)", description = "프로필 이미지를 포함한 새로운 사용자 등록 API 입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 사용자", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping(value = "/register-with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<RegisterResponseDto> registerWithImage(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("passwordConfirm") String passwordConfirm,
            @RequestParam("name") String name,
            @RequestParam("nickname") String nickname,
            @RequestParam(value = "birthDate", required = false) String birthDate,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {

        log.info("프로필 이미지 포함 회원가입 요청 수신: username={}", username);

        // RegisterRequestDto 객체 생성
        RegisterRequestDto registerDto = RegisterRequestDto.builder()
                .username(username)
                .password(password)
                .passwordConfirm(passwordConfirm)
                .name(name)
                .nickname(nickname)
                .birthDate(birthDate)
                .build();

        RegisterResponseDto response = authService.registerWithImage(registerDto, profileImage);
        return ApiResponse.success(response, SuccessCode.CREATED);
    }

    @Operation(summary = "로그인", description = "사용자 로그인 API 입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/login")
    public ApiResponse<JwtDto> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password) {

        log.info("로그인 요청 수신: username={}", username);

        // LoginRequestDto 객체 생성
        LoginRequestDto loginDto = LoginRequestDto.builder()
                .username(username)
                .password(password)
                .build();

        JwtDto jwt = authService.login(loginDto);
        return ApiResponse.success(jwt, SuccessCode.OK);
    }

    @Operation(summary = "로그아웃", description = "사용자 로그아웃 API 입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request) {

        log.info("로그아웃 요청 수신: providerId={}", userDetails.getProviderId());
        authService.logout(userDetails.getProviderId(), request);
        return ApiResponse.success(null, SuccessCode.OK);
    }

    @Operation(summary = "회원 탈퇴", description = "일반 사용자 계정을 탈퇴하는 API 입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @DeleteMapping("/withdraw")
    public ApiResponse<Void> withdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request,
            @RequestParam(value = "password", required = false) String password) {

        log.info("회원 탈퇴 요청 수신: userId={}, providerId={}, password={}",
                userDetails.getUserId(), userDetails.getProviderId(),
                password != null ? "제공됨" : "없음");

        try {
            // 사용자 정보 로깅 (디버깅용)
            log.info("UserDetails 정보: userId={}, providerId={}",
                    userDetails.getUserId(), userDetails.getProviderId());

            // providerId로 탈퇴 처리 (통합 로직)
            String identifier = userDetails.getProviderId();
            if (identifier == null || identifier.trim().isEmpty()) {
                log.error("사용자 식별 정보가 없습니다.");
                throw new CustomException(ErrorCode.ENTITY_NOT_FOUND, "사용자 식별 정보가 없습니다.");
            }

            // AuthService의 withdrawByUsername 메서드 호출
            // (이 메서드가 username과 providerId 모두 처리함)
            authService.withdrawByUsername(identifier, password, request);

            log.info("회원 탈퇴 성공: identifier={}", identifier);
            return ApiResponse.success(null, SuccessCode.OK);

        } catch (CustomException e) {
            log.error("회원 탈퇴 CustomException: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("회원 탈퇴 중 예상치 못한 예외 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "회원 탈퇴 처리 중 오류가 발생했습니다.");
        }
    }

    @Operation(summary = "소셜 회원 탈퇴 (카카오 사용자)", description = "카카오 사용자 계정을 탈퇴하는 API 입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @DeleteMapping("/withdraw/social")
    public ApiResponse<Void> withdrawSocial(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request) {

        log.info("소셜 회원 탈퇴 요청 수신: providerId={}", userDetails.getProviderId());

        try {
            // 카카오 사용자 탈퇴 (비밀번호 검증 없음)
            authService.withdrawByProviderId(userDetails.getProviderId(), request);

            log.info("소셜 회원 탈퇴 성공: providerId={}", userDetails.getProviderId());
            return ApiResponse.success(null, SuccessCode.OK);

        } catch (CustomException e) {
            log.error("소셜 회원 탈퇴 CustomException: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("소셜 회원 탈퇴 중 예상치 못한 예외 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "소셜 회원 탈퇴 처리 중 오류가 발생했습니다.");
        }
    }
}