package greensnail_backend.GreenSnail.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
================ Payment(Entity) ================

☆ 결제 정보를 저장하기 위한 JPA 엔티티 클래스
☆ 카카오 페이 결제와 관련된 데이터를 데이터베이스에 저장하고 관리하는 데 사용

☆ KakaoPayService에서 결제 준비와 승인 시 Payment 엔티티를 사용해 데이터를 저장 및 업데이트

☆ 특징
- @Entity와 @Table 어노테이션을 사용해 JPA 엔티티로 정의.
- @Id와 @GeneratedValue로 기본 키 설정.
- @Column으로 필드 제약 조건 설정 (예: nullable = false).
- Lombok의 @Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor을 사용해
  보일러플레이트 코드 최소화.

=> 현재는 H2 Database의 payment 테이블과 매핑
=================================================
 */


@Entity
@Table(name = "payment", indexes = {
        @Index(name = "idx_payment_tid", columnList = "tid"),
        @Index(name = "idx_payment_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "결제 고유 ID는 필수입니다.")
    private String tid;

    @Column(nullable = false)
    @NotBlank(message = "가맹점 주문 번호는 필수입니다.")
    private String orderId;

    @Column(nullable = false)
    @NotBlank(message = "가맹점 회원 ID는 필수입니다.")
    private String userId;

    @Column(nullable = false)
    @Positive(message = "결제 금액은 0보다 커야 합니다.")
    private BigDecimal amount;

    private String itemName;

    @Column(nullable = false)
    @NotBlank(message = "결제 상태는 필수입니다.")
    private String status; // 결제 상태

    private LocalDateTime createdAt; // payment 생성 시간

    private LocalDateTime approvedAt; // payment 승인 시간

    private LocalDateTime subscriptionStartDate; // 구독 시작일

    private LocalDateTime nextBillingDate; // 다음 결제일
}