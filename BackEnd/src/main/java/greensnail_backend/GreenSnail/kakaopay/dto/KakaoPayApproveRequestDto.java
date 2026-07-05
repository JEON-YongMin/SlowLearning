package greensnail_backend.GreenSnail.kakaopay.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/*
================ KakaoPayApproveRequestDto ================

☆ 카카오 페이 결제 승인 API(/v1/payment/approve)를 호출할 때 필요한 요청 데이터를 담는 DTO
☆ 결제 준비 후 사용자가 결제를 완료하면,
   카카오 페이가 백엔드의 /kakaopay/success 엔드포인트로 pg_token을 전달
☆ KakaoPayService는 이 pg_token과 다른 필수 데이터를 이 DTO에 담아 결제 승인 요청을 보냄

☆ 특징
- Lombok의 @Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor을 사용해
   보일러플레이트 코드 최소화.
- KakaoPayService에서 이 DTO를 사용해 결제 승인 API 요청 파라미터를 구성.

===========================================================

 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoPayApproveRequestDto {

    @Schema(description = "가맹점 고유 번호")
    @NotBlank(message = "가맹점 코드는 필수입니다.")
    private String cid;

    @Schema(description = "결제 고유 번호")
    @NotBlank(message = "결제 고유 번호는 필수입니다.")
    private String tid;

    @Schema(description = "가맹점 주문 번호")
    @NotBlank(message = "가맹점 주문 번호는 필수입니다.")
    private String  partnerOrderId;

    @Schema(description = "가맹점 회원 ID")
    @NotBlank(message = "가맹점 회원 ID는 필수입니다.")
    private String partnerUserId;

    @Schema(description = "결제 승인 요청을 인증하는 토큰")
    @NotBlank(message = "결제 승인 토큰은 필수입니다.")
    private String pgToken;
}