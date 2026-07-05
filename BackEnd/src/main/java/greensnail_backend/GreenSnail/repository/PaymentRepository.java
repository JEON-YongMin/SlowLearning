package greensnail_backend.GreenSnail.repository;

import greensnail_backend.GreenSnail.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/*
================ PaymentRepository ================

☆ Payment 엔티티와 데이터베이스 간의 인터페이스를 제공하는 Spring Data JPA 리포지토리
☆ KakaoPayService에서 결제 정보를 저장하거나 조회할 때 사용.
☆ SpringDataJPA를 사용하므로 기본적인 CRUD 메서드 자동으로 제공받고,
  필요한 경우 커스텀 쿼리 메서드 정의 가능.

☆ findTopByStatusOrderByCreatedAtDesc
  : 특정 상태의 결제 정보 중 가장 최근 데이터를 조회.
  KakaoPayService의 결제 승인(approvePayment) 메서드에서 사용.

===================================================

 */

// @Repository 어노테이션은 Spring Data JPA에서 자동으로 빈으로 등록되므로 생략 가능.
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // 상태가 PENDING인 가장 최근 결제 정보 조회
    Optional<Payment> findTopByStatusOrderByCreatedAtDesc(String status);

    // userId와 status로 결제정보 조회
    // 특정 사용자가 동일한 상태로 여러 결제 기록을 가질 수 있기 때문에 모든 결과 반환
    List<Payment> findByUserIdAndStatus(String userId, String status);

    // tid로 결제 정보 조회
    Optional<Payment> findByTid(String tid);
}