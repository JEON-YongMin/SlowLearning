package greensnail_backend.GreenSnail.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyPageResponseDto {

    // 사용자 프로필 정보
    private Long userId;            // 사용자 ID
    private String providerId;      // 제공자 ID (카카오 등)
    private String nickname;
    private String profileImage;    // 프로필 이미지 URL, null이면 기본 이미지 사용

    // 구독제 결제 정보 (현재 구현되지 않음)
    private SubscriptionInfo subscription;

    // 자가진단 최근 결과
    private DiagnosisInfo latestDiagnosis;

    // 프로그램 지원 공공기관 단체 정보
    private List<SupportOrganizationDto> supportOrganizations;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubscriptionInfo {
        private String planName;           // 구독 플랜명
        private String nextBillingDate;    // 다음 결제일 (yyyy-MM-dd)
        private String status;             // 구독 상태 (활성, 비활성, 만료 등)
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiagnosisInfo {
        private int score;                 // 진단 점수
        private String resultType;         // 일반군, 경계선 지능 탐색군, 경계선 지능 위험군
        private String diagnosisDate;      // 진단 날짜 (yyyy-MM-dd HH:mm:ss)
        private String ageGroup;           // 연령대 (UNDER_14, OVER_14)
    }
}