package greensnail_backend.GreenSnail.controller;

import greensnail_backend.GreenSnail.global.api.ErrorCode;
import greensnail_backend.GreenSnail.global.exception.CustomException;
import greensnail_backend.GreenSnail.kakaopay.util.LogUtils;
import greensnail_backend.GreenSnail.kakaopay.dto.KakaoPayApproveResponseDto;
import greensnail_backend.GreenSnail.kakaopay.dto.KakaoPayReadyResponseDto;
import greensnail_backend.GreenSnail.kakaopay.service.KakaoPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class KakaoPayController {

    private final KakaoPayService kakaoPayService;
    private static final Logger logger = LoggerFactory.getLogger(KakaoPayController.class);


    /*
    =============== ReadyPayment ===============
    JWT 인증으로 providerId를 추출해서 service에 넘긴다.
    SecurityContextHolder로 ProviderId 추출.
    별도 정의한 LogUtils로 로깅 중앙화.
    ============================================
     */
    @Operation(summary = "카카오 페이 결제 준비", description = "카카오 페이 결제 준비를 요청합니다.")
    @PostMapping("/ready")
    public ResponseEntity<KakaoPayReadyResponseDto> readyPayment() {
        String methodName = "readyPayment";
        String providerId = extractProviderIdFromSecurityContext();
        LogUtils.info(logger, methodName, "결제 준비 요청", "providerId: " + providerId);

        KakaoPayReadyResponseDto response = kakaoPayService.readyPayment(providerId);
        LogUtils.info(logger, methodName, "결제 준비 응답", response);
        return ResponseEntity.ok(response);
    }


    /*
    =============== ApprovePayment ===============
    결제 승인을 처리하는 메서드
    클라이언트로부터 받은 pgToken을 사용해 결제 승인 요청, 결과 반환
    pgToken을 받아 service의 approvePayment에 전달
    ==============================================
     */
    @Operation(summary = "카카오 페이 결제 승인", description = "카카오 페이 결제 승인을 처리합니다.")
    @GetMapping("/success")
    public ResponseEntity<KakaoPayApproveResponseDto> paymentSuccess(
            @Parameter(description = "결제 승인을 위한 토큰") @RequestParam("pg_token") String pgToken) {
        String methodName = "paymentSuccess";
        LogUtils.info(logger, methodName, "결제 승인 요청", "pgToken: " + pgToken);

        KakaoPayApproveResponseDto response = kakaoPayService.approvePayment(pgToken);
        LogUtils.info(logger, methodName, "결제 승인 응답", response);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "카카오 페이 결제 취소", description = "카카오 페이 결제 취소 시 호출됩니다.")
    @GetMapping("/cancel")
    public ResponseEntity<String> paymentCancel(
            @Parameter(description = "결제 고유 ID") @RequestParam("tid") String tid) {
        String methodName = "paymentCancel";
        LogUtils.info(logger, methodName, "결제 취소 요청", "tid: " + tid);

        kakaoPayService.cancelPayment(tid);
        LogUtils.info(logger, methodName, "결제 취소 완료", "tid: " + tid);
        return ResponseEntity.ok("Payment Canceled");
    }

    @Operation(summary = "카카오 페이 결제 실패", description = "카카오 페이 결제 실패 시 호출됩니다.")
    @GetMapping("/fail")
    public ResponseEntity<String> paymentFail(
            @Parameter(description = "결제 고유 ID") @RequestParam("tid") String tid) {
        String methodName = "paymentFail";
        LogUtils.info(logger, methodName, "결제 실패 요청", "tid: " + tid);

        kakaoPayService.failPayment(tid);
        LogUtils.info(logger, methodName, "결제 실패 처리 완료", "tid: " + tid);
        return ResponseEntity.ok("Payment Failed");
    }

    private String extractProviderIdFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED, "인증 정보가 없습니다.");
        }

        String providerId = authentication.getName(); // CustomUserDetails.getUsername() 호출, providerId 반환
        if (providerId == null || providerId.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "인증 정보에서 providerId를 추출할 수 없습니다.");
        }

        return providerId;
    }
}