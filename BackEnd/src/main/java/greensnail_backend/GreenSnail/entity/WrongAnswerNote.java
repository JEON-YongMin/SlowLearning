package greensnail_backend.GreenSnail.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WrongAnswerNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String providerId;

    @ManyToOne(fetch = FetchType.LAZY)
    private Word word;

    private LocalDate quizDate;

    private boolean isSolved;
}