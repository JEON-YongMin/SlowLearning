package greensnail_backend.GreenSnail.kakaopay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

/*
================ KakaoPayReadyResponseDto ================

☆ 카카오 페이 결제 준비 API(/v1/payment/ready) 호출 후 반환되는 응답 데이터를 담는 DTO
☆ 카카오 페이에서 결제 준비가 완료되면 응답으로 결제 URL과 거래 ID를 포함한 데이터를 보내주는데,
   이 데이터를 구조화해서 처리
☆ KakaoPayService에서 API 호출 후 이 DTO로 응답을 받아 KakaoPayController로 전달

☆ AndroidStudio와 연동을 위해 nextRedirectMobileUrl,nextRedirectAppUrl,androidAppScheme 추가.

==========================================================

 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoPayReadyResponseDto {

    @NotBlank(message = "결제 고유 ID는 필수입니다.")
    private String tid;

    @JsonProperty("next_redirect_pc_url")
    @NotBlank(message = "PC 리다이렉트 URL은 필수입니다.")
    private String nextRedirectPcUrl;

    @JsonProperty("next_redirect_mobile_url")
    @NotBlank(message = "Mobile 리다이렉트 URL은 필수입니다.")
    private String nextRedirectMobileUrl;

    @JsonProperty("next_redirect_app_url")
    private String nextRedirectAppUrl;

    @JsonProperty("android_app_scheme")
    private String androidAppScheme;

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt; //
}