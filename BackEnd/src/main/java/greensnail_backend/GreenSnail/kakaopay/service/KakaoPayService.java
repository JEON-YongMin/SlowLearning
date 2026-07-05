package greensnail_backend.GreenSnail.kakaopay.service;

import greensnail_backend.GreenSnail.entity.Payment;
import greensnail_backend.GreenSnail.global.api.ErrorCode;
import greensnail_backend.GreenSnail.global.exception.CustomException;
import greensnail_backend.GreenSnail.kakaopay.dto.*;
import greensnail_backend.GreenSnail.kakaopay.model.SubscriptionModel;
import greensnail_backend.GreenSnail.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KakaoPayService {

    private static final Logger logger = LoggerFactory.getLogger(KakaoPayService.class);

    private final RestTemplate restTemplate;
    private final PaymentRepository paymentRepository;
    private final SubscriptionModel subscriptionModel;

    @Value("${kakaopay.secretKey}")
    private String secretKey;

    @Value("${kakaopay.cid}")
    private String cid;

    @Value("${kakaopay.api-url}")
    private String apiUrl;

    @Value("${kakaopay.redirect-url.success}")
    private String approvalUrl;

    @Value("${kakaopay.redirect-url.cancel}")
    private String cancelUrl;

    @Value("${kakaopay.redirect-url.fail}")
    private String failUrl;

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "SECRET_KEY " + secretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Transactional
    public KakaoPayReadyResponseDto readyPayment(String providerId) {
        KakaoPayReadyRequestDto request = new KakaoPayReadyRequestDto();
        request.setPartnerOrderId(UUID.randomUUID().toString());
        request.setPartnerUserId(providerId);
        request.setItemName(subscriptionModel.getModelName());
        request.setTotalAmount(subscriptionModel.getPrice());
        request.setTaxFreeAmount(0);
        request.setQuantity(1);

        HttpHeaders header = getHeaders();
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("cid", cid);
        requestBody.put("partner_order_id", request.getPartnerOrderId());
        requestBody.put("partner_user_id", request.getPartnerUserId());
        requestBody.put("item_name", subscriptionModel.getModelName());
        requestBody.put("quantity", String.valueOf(request.getQuantity()));
        requestBody.put("total_amount", String.valueOf(subscriptionModel.getPrice()));
        requestBody.put("tax_free_amount", String.valueOf(request.getTaxFreeAmount()));
        requestBody.put("approval_url", approvalUrl);
        requestBody.put("cancel_url", cancelUrl);
        requestBody.put("fail_url", failUrl);

        HttpEntity<Map<String, String>> body = new HttpEntity<>(requestBody, header);

        logger.info("카카오 페이 API 호출 - URL: {}, Body: {}", apiUrl + "/v1/payment/ready", requestBody);

        KakaoPayReadyResponseDto response;
        try {
            response = restTemplate.postForObject(
                    apiUrl + "/v1/payment/ready",
                    body,
                    KakaoPayReadyResponseDto.class);
        } catch (HttpClientErrorException e) {
            logger.error("카카오 페이 API 호출 실패 - HTTP 상태 코드: {}, 응답 메시지: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new CustomException(ErrorCode.UNAUTHORIZED, "카카오 페이 인증 실패: secretKey를 확인하세요.");
            } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new CustomException(ErrorCode.ACCESS_DENIED, "카카오 페이 가맹점 코드 오류: cid를 확인하세요.");
            } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "카카오 페이 요청 파라미터 오류: " + e.getMessage());
            }
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "카카오 페이 API 호출 실패: " + e.getMessage());
        } catch (Exception e) {
            logger.error("카카오 페이 API 호출 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "카카오 페이 API 호출 실패: " + e.getMessage());
        }

        Payment payment = new Payment();
        assert response != null;
        payment.setTid(response.getTid());
        payment.setOrderId(request.getPartnerOrderId());
        payment.setUserId(request.getPartnerUserId());
        payment.setAmount(BigDecimal.valueOf(request.getTotalAmount()));
        payment.setItemName(request.getItemName());
        payment.setStatus("PENDING");
        payment.setCreatedAt(response.getCreatedAt());
        paymentRepository.save(payment);

        return response;
    }

    @Transactional
    public KakaoPayApproveResponseDto approvePayment(String pgToken) {
        Payment payment = paymentRepository.findTopByStatusOrderByCreatedAtDesc("PENDING")
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        KakaoPayApproveRequestDto request = new KakaoPayApproveRequestDto();
        request.setCid(cid);
        request.setTid(payment.getTid());
        request.setPartnerOrderId(payment.getOrderId());
        request.setPartnerUserId(payment.getUserId());
        request.setPgToken(pgToken);

        return approvePayment(request, payment);
    }

    @Transactional
    public KakaoPayApproveResponseDto approvePayment(KakaoPayApproveRequestDto request, Payment payment) {
        HttpHeaders header = getHeaders();
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("cid", request.getCid());
        requestBody.put("tid", request.getTid());
        requestBody.put("partner_order_id", request.getPartnerOrderId());
        requestBody.put("partner_user_id", request.getPartnerUserId());
        requestBody.put("pg_token", request.getPgToken());

        HttpEntity<Map<String, String>> body = new HttpEntity<>(requestBody, header);

        KakaoPayApproveResponseDto response;
        try {
            response = restTemplate.postForObject(
                    apiUrl + "/v1/payment/approve",
                    body,
                    KakaoPayApproveResponseDto.class);
        } catch (HttpClientErrorException e) {
            logger.error("카카오 페이 API 호출 실패 - HTTP 상태 코드: {}, 응답 메시지: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new CustomException(ErrorCode.UNAUTHORIZED, "카카오 페이 인증 실패: secretKey를 확인하세요.");
            } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new CustomException(ErrorCode.ACCESS_DENIED, "카카오 페이 가맹점 코드 오류: cid를 확인하세요.");
            } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "카카오 페이 요청 파라미터 오류: " + e.getMessage());
            }
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "카카오 페이 API 호출 실패: " + e.getMessage());
        } catch (Exception e) {
            logger.error("카카오 페이 API 호출 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "카카오 페이 API 호출 실패: " + e.getMessage());
        }

        payment.setStatus("SUCCESS");
        assert response != null;
        payment.setApprovedAt(response.getApprovedAt());
        payment.setItemName(response.getItemName());
        payment.setSubscriptionStartDate(response.getApprovedAt()); // 구독 시작일 설정
        payment.setNextBillingDate(response.getApprovedAt().plusMonths(1)); // 다음 결제일 설정 (구독 주기 1개월 가정)
        paymentRepository.save(payment);

        return response;
    }

    @Transactional
    public void cancelPayment(String tid) {
        Payment payment = paymentRepository.findByTid(tid)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));

        Map<String, String> params = new HashMap<>();
        params.put("cid", cid);
        params.put("tid", tid);
        params.put("cancel_amount", String.valueOf(payment.getAmount()));
        params.put("cancel_tax_free_amount", "0");

        HttpHeaders header = getHeaders();
        HttpEntity<Map<String, String>> body = new HttpEntity<>(params, header);

        ResponseEntity<String> responseEntity;
        try {
            responseEntity = restTemplate.exchange(
                    apiUrl + "/v1/payment/cancel",
                    HttpMethod.POST,
                    body,
                    String.class
            );
        } catch (HttpClientErrorException e) {
            logger.error("카카오 페이 API 호출 실패 - HTTP 상태 코드: {}, 응답 메시지: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new CustomException(ErrorCode.UNAUTHORIZED, "카카오 페이 인증 실패: secretKey를 확인하세요.");
            } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new CustomException(ErrorCode.ACCESS_DENIED, "카카오 페이 가맹점 코드 오류: cid를 확인하세요.");
            } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "카카오 페이 요청 파라미터 오류: " + e.getMessage());
            }
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "카카오 페이 API 호출 실패: " + e.getMessage());
        } catch (Exception e) {
            logger.error("카카오 페이 API 호출 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "카카오 페이 API 호출 실패: " + e.getMessage());
        }

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "카카오 페이 API 응답 실패: " + responseEntity.getBody());
        }

        payment.setStatus("CANCELED");
        paymentRepository.save(payment);
    }

    @Transactional
    public void failPayment(String tid) {
        Payment payment = paymentRepository.findByTid(tid)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
        payment.setStatus("FAILED");
        paymentRepository.save(payment);
    }
}