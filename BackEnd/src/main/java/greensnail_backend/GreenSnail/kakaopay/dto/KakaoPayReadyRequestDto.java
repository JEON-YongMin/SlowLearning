package greensnail_backend.GreenSnail.kakaopay.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

/*
================ KakaoPayReadyRequestDto ================

☆ 카카오 페이 결제 준비 API(/v1/payment/ready)를 호출할 때 필요한 요청 데이터를 담는 DTO 클래스

☆ 카카오 페이 개발자센터 공식 Request Body Payload 중 필수 필드만 그대로 작성.
   (Tax, 할부 안해도 되잖아요)

☆ 클라이언트로부터 받은 결제 요청 데이터를 받아서 KakaoPayController에서 KakaoPayService로 전달
   (클라이언트 요청을 이 DTO로 변환)
☆ KakaoPayService에서 이 데이터를 기반으로 API 요청을 구성

☆ 특징
- Lombok의 @Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor을 사용해
   보일러플레이트 코드 최소화.
- KakaoPayService에서 이 DTO를 사용해 카카오 페이 API 요청 파라미터를 구성
  (예: params.add("cid", request.getCid())).

=========================================================

 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoPayReadyRequestDto {

    @NotBlank(message = "가맹점 주문 번호는 필수입니다.")
    private String partnerOrderId;

    @NotBlank(message = "가맹점 회원 ID는 필수입니다.")
    private String partnerUserId;

    @NotBlank(message = "상품명은 필수입니다.")
    private String itemName;

    @NotNull(message = "상품 수량은 필수입니다.")
    @Positive(message = "상품 수량은 0보다 커야 합니다.")
    private Integer quantity;

    @NotNull(message = "총 결제 금액은 필수입니다.")
    @Positive(message = "총 결제 금액은 0보다 커야 합니다.")
    private Integer totalAmount;

    @NotNull(message = "비과세 금액은 필수입니다.")
    @PositiveOrZero(message = "비과세 금액은 0 이상이어야 합니다.")
    private Integer taxFreeAmount;
}