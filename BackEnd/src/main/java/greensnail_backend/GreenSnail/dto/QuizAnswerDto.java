package greensnail_backend.GreenSnail.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizAnswerDto {
    private Long quizId;
    private String selectedAnswer;
}
