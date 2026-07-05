package greensnail_backend.GreenSnail.kakaopay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

/*
================ KakaoPayApproveResponseDto ================

☆ 카카오 페이 결제 승인 API(/v1/payment/approve) 호출 후 반환되는 응답 데이터를 담는 DTO
☆ 결제 승인 API는 사용자가 결제를 완료한 후 최종적으로 결제를 확정하는 단계에서 호출되며,
   카카오 페이가 결제 결과를 반환

☆ 이 DTO는 그 응답 데이터를 구조화해서 처리하기 위해 사용
☆ KakaoPayService에서 API 호출 후 이 DTO로 응답을 받아 처리

☆ 특징
- Lombok의 @Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor을 사용해
  보일러플레이트 코드 최소화.
- KakaoPayService에서 RestTemplate으로 API 호출 후 응답을 이 DTO로 매핑.
- 중첩 객체(amount)를 처리하기 위해 내부 클래스를 정의.

============================================================
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoPayApproveResponseDto {

    private String aid;
    private String tid;
    private String cid;
    private String sid;

    @JsonProperty("partner_order_id")
    private String partnerOrderId;

    @JsonProperty("partner_user_id")
    private String partnerUserId;

    @JsonProperty("payment_method_type")
    private String paymentMethodType;

    @JsonProperty("item_name")
    private String itemName;

    @JsonProperty("item_code")
    private String itemCode;

    @JsonProperty("payload")
    private String payload;

    private Integer quantity;

    @JsonProperty("amount")
    private Amount amount;

    @JsonProperty("card_info")
    private Object cardInfo;

    @JsonProperty("sequential_payment_methods")
    private Object sequentialPaymentMethods;

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonProperty("approved_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime approvedAt;

    @Getter
    @Setter
    @ToString
    public static class Amount {
        private Integer total;
        @JsonProperty("tax_free")
        private Integer taxFree;
        private Integer vat;
        private Integer point;
        private Integer discount;
        @JsonProperty("green_deposit")
        private Integer greenDeposit;
    }
}