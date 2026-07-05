package greensnail_backend.GreenSnail.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Setter
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WordLearningLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String providerId;

    private LocalDate quizDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    private boolean isSolved;

    private LocalDate solvedAt;
}
