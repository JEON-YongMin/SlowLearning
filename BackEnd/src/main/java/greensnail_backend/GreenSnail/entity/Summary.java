package greensnail_backend.GreenSnail.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Slf4j
public class Summary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    @JsonIgnore
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @Column(name = "provider_id")
    private String providerId;

    @Lob
    private String userSummary;

    @Lob
    private String aiSummary;

    @Lob
    private String feedback;

    private LocalDateTime createdAt;

    private boolean isCleared = false;

    @Column(name = "cleared_at")
    private LocalDate clearedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        log.info("Summary entity PrePersist - User: {}, Article: {}, ProviderId: {}",
                user != null ? user.getId() : "null",
                article != null ? article.getId() : "null",
                providerId);
    }

    public static Summary create(User user, Article article, String userSummary, String aiSummary) {
        if (user == null) {
            log.error("User cannot be null when creating Summary");
            throw new IllegalArgumentException("User cannot be null");
        }
        if (article == null) {
            log.error("Article cannot be null when creating Summary");
            throw new IllegalArgumentException("Article cannot be null");
        }
        if (userSummary == null || userSummary.trim().isEmpty()) {
            log.error("UserSummary cannot be null or empty when creating Summary");
            throw new IllegalArgumentException("UserSummary cannot be null or empty");
        }

        Summary summary = new Summary();
        summary.setArticle(article);
        summary.setUser(user);
        summary.setProviderId(user.getProviderId());
        summary.setUserSummary(userSummary);
        summary.setAiSummary(aiSummary);

        log.info("Created Summary entity - User ID: {}, Article ID: {}, Provider ID: {}",
                user.getId(), article.getId(), user.getProviderId());

        return summary;
    }

    public void updateAiFeedback(String feedback) {

        this.feedback = feedback;
        this.setClearedAt(LocalDate.now());
        this.setCleared(true);
    }
}