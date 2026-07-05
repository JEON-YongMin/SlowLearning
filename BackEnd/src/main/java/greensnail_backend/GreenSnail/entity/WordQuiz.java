package greensnail_backend.GreenSnail.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WordQuiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false, unique = true)
    private Word word;

    @Column(nullable = false)
    private String question;

    @ElementCollection
    @CollectionTable(name = "word_quiz_choices", joinColumns = @JoinColumn(name = "quiz_id"))
    @Column(name = "choice")
    private List<String> choices;

    @Column(nullable = false)
    private String answer;

    @Column(length = 500)
    private String explanation;
}
