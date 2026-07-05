package greensnail_backend.GreenSnail.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String providerId;  // 카카오 고유 ID (소셜 로그인용)

    @Column(unique = true)
    private String username;    // 사용자 아이디 (일반 로그인용)

    private String password;    // 비밀번호 (암호화됨)

    private String nickname;    // 닉네임

    private String profileImage; // 프로필 이미지 URL

    private String birthDate;   // 생년월일

    @Embedded
    private Address address;    // 주소 정보

    @Builder.Default
    private boolean deletable = true;  // 기본값을 true로 설정

    @Enumerated(EnumType.STRING)
    private UserType userType;  // 사용자 타입 (SOCIAL, GENERAL)

    @Builder.Default
    private boolean isSubscribed = false; // 구독 여부 (기본값: false)

    private LocalDateTime subscriptionStartDate; // 구독 시작 날짜 (선택)

    private LocalDateTime subscriptionEndDate;   // 구독 종료 날짜 (선택)

    @Builder.Default
    private boolean isBlocked = false;
    public void setBlocked(boolean blocked) {
        this.isBlocked = blocked;
    }
}