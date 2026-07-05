package greensnail_backend.GreenSnail.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
        name = "diary",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "date"})
        }
)
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 일기 작성 날짜 (한 유저당 하루에 한 번만 작성 가능)
    @Column(nullable = false)
    private LocalDate date;

    // 일기 본문
    @Lob
    @Column(nullable = false)
    private String content;

    // AI 피드백 (null 가능)
    @Lob
    private String aiFeedback;

    // 작성 시각
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // AI 피드백 업데이트용 메서드
    public void updateAiFeedback(String aiFeedback) {
        this.aiFeedback = aiFeedback;
    }
}
