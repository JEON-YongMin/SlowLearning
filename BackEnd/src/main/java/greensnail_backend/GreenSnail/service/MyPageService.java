package greensnail_backend.GreenSnail.service;

import greensnail_backend.GreenSnail.dto.MyPageResponseDto;
import greensnail_backend.GreenSnail.dto.SupportOrganizationDto;
import greensnail_backend.GreenSnail.entity.DiagnosisResult;
import greensnail_backend.GreenSnail.entity.Payment;
import greensnail_backend.GreenSnail.entity.SupportOrganization;
import greensnail_backend.GreenSnail.entity.User;
import greensnail_backend.GreenSnail.global.api.ErrorCode;
import greensnail_backend.GreenSnail.global.exception.CustomException;
import greensnail_backend.GreenSnail.repository.DiagnosisResultRepository;
import greensnail_backend.GreenSnail.repository.PaymentRepository;
import greensnail_backend.GreenSnail.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;
    private final DiagnosisResultRepository diagnosisResultRepository;
    private final PaymentRepository paymentRepository;
    private final SupportOrganizationService supportOrganizationService;

    @Transactional(readOnly = true)
    public MyPageResponseDto getMyPageInfo(String providerId) {
        // 1. 사용자 정보 조회
        User user = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 2. 프로필 정보 설정
        MyPageResponseDto.MyPageResponseDtoBuilder builder = MyPageResponseDto.builder()
                .userId(user.getId())
                .providerId(user.getProviderId())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage());

        // 3. 최근 진단 결과 조회 및 설정
        builder.latestDiagnosis(getLatestDiagnosisInfo(user));

        // 4. 카카오페이 결제 정보 조회 및 설정
        builder.subscription(getSubscriptionInfo(user.getProviderId()));

        // 5. 공공기관 지원 단체 목록 추가
        List<SupportOrganizationDto> supportOrganizations = supportOrganizationService.getOrganizations();
        builder.supportOrganizations(supportOrganizations);
        return builder.build();
    }

    private MyPageResponseDto.DiagnosisInfo getLatestDiagnosisInfo(User user) {
        List<DiagnosisResult> diagnosisResults = diagnosisResultRepository.findAll();
        DiagnosisResult latestDiagnosis = diagnosisResults.stream()
                .filter(result -> result.getUser().getId().equals(user.getId()))
                .max((o1, o2) -> o1.getCreatedAt().compareTo(o2.getCreatedAt()))
                .orElse(null);

        if (latestDiagnosis != null) {
            return MyPageResponseDto.DiagnosisInfo.builder()
                    .score(latestDiagnosis.getScore())
                    .resultType(latestDiagnosis.getResultType())
                    .diagnosisDate(latestDiagnosis.getCreatedAt()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .ageGroup(latestDiagnosis.getAgeGroup().toString())
                    .build();
        }
        return null;
    }

    private MyPageResponseDto.SubscriptionInfo getSubscriptionInfo(String userId) {
        // 가장 최근 결제 조회
        Optional<Payment> latestPayment = paymentRepository.findAll().stream()
                .filter(payment -> payment.getUserId().equals(userId))
                .filter(payment -> "SUCCESS".equals(payment.getStatus()))
                .max((p1, p2) -> p1.getApprovedAt() != null && p2.getApprovedAt() != null ?
                        p1.getApprovedAt().compareTo(p2.getApprovedAt()) : 0);

        if (latestPayment.isPresent()) {
            Payment payment = latestPayment.get();
            // 결제가 성공했다면 구독이 활성화되었다고 가정
            // 실제로는 구독 상태 관리 로직이 추가되어야 함 -> ex: 구독 상태 추적, 자동 갱신 여부, 결제 실패 처리, 구독 업그레이드
            // 근데 단순하게 갑시다
            return MyPageResponseDto.SubscriptionInfo.builder()
                    .planName(payment.getItemName())  // 결제 항목명 사용
                    .status("활성")
                    .nextBillingDate(payment.getNextBillingDate() != null ?
                            payment.getNextBillingDate().toLocalDate().toString() : null)
                    .build();
        }

        // 결제 내역이 없는 경우
        return MyPageResponseDto.SubscriptionInfo.builder()
                .planName("무료")
                .status("비활성")
                .nextBillingDate(null)
                .build();
    }
}