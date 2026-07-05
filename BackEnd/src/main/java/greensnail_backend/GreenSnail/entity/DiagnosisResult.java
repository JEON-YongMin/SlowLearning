package greensnail_backend.GreenSnail.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "user"})
public class DiagnosisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AgeGroup ageGroup;

    private int score;
    private String resultType;

    private LocalDateTime createdAt;

    // 사용자 연동 (지금 로그인한 사용자와 연결)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    protected DiagnosisResult() {}

    public DiagnosisResult(User user, AgeGroup ageGroup, int score, String resultType) {
        this.user = user;
        this.ageGroup = ageGroup;
        this.score = score;
        this.resultType = resultType;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public AgeGroup getAgeGroup() {
        return ageGroup;
    }

    public int getScore() {
        return score;
    }

    public String getResultType() {
        return resultType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public User getUser() {
        return user;
    }
}
