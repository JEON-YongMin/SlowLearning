package greensnail_backend.GreenSnail.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "learning_progress")
@Getter
@Setter
public class LearningProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(name = "monthInfo", nullable = false)
    private String monthInfo; // 예: "2025-06"

    @Column(name = "progress", length = 32, nullable = false)
    private String progress = "00000000000000000000000000000000";
}
