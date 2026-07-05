package greensnail_backend.GreenSnail.controller;

import greensnail_backend.GreenSnail.entity.User;
import greensnail_backend.GreenSnail.global.api.ApiResponse;
import greensnail_backend.GreenSnail.global.api.ErrorCode;
import greensnail_backend.GreenSnail.global.api.SuccessCode;
import greensnail_backend.GreenSnail.global.exception.CustomException;
import greensnail_backend.GreenSnail.login.dto.JwtDto;
import greensnail_backend.GreenSnail.login.jwt.CustomUserDetails;
import greensnail_backend.GreenSnail.login.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "회원", description = "회원 관련 API 입니다.")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    /**
     * 토큰 재발급 API
     */
    @Operation(summary = "토큰 재발급", description = "Refresh Token을 이용해 새로운 Access Token을 발급하는 API입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않은 토큰", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/reissue")
    public ApiResponse<JwtDto> reissue(HttpServletRequest request) {
        log.info("토큰 재발급 요청 수신...");

        try {
            JwtDto jwt = userService.reissue(request);
            log.info("토큰 재발급 성공 - 새로운 AccessToken 반환");
            return ApiResponse.success(jwt, SuccessCode.OK);
        } catch (CustomException e) {
            log.error("토큰 재발급 중 예외 발생: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("예상치 못한 예외 발생: {}", e.getMessage());
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }

    /**
     * 로그아웃 API
     */
    @Operation(summary = "로그아웃", description = "사용자를 로그아웃 처리하는 API입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @DeleteMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        userService.logout(request);
        return ApiResponse.success(null, SuccessCode.OK);
    }

    /**
     * 사용자 정보 조회 API
     */
    @Operation(summary = "사용자 정보 조회", description = "로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> getUserInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        // 로그인한 사용자 정보 조회
        User user = userService.findByProviderId(userDetails.getProviderId())
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", user.getId());
        userInfo.put("nickname", user.getNickname());

        if (user.getAddress() != null) {
            Map<String, String> address = new HashMap<>();
            address.put("zipcode", user.getAddress().getZipcode());
            address.put("address", user.getAddress().getAddress());
            address.put("addressDetail", user.getAddress().getAddressDetail());
            userInfo.put("address", address);
        }

        return ApiResponse.success(userInfo, SuccessCode.OK);
    }
}