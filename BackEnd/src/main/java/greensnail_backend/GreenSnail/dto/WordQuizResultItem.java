package greensnail_backend.GreenSnail.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WordQuizResultItem {

    private Long quizId;
    private String word;
    private String selectedAnswer;
    private String correctAnswer;
    private boolean isCorrect;
    private String explanation;

    public static WordQuizResultItem of(Long quizId, String word, String selectedAnswer, String correctAnswer, String explanation) {
        boolean correct = selectedAnswer.equals(correctAnswer);
        return WordQuizResultItem.builder()
                .quizId(quizId)
                .word(word)
                .selectedAnswer(selectedAnswer)
                .correctAnswer(correctAnswer)
                .isCorrect(correct)
                .explanation(correct ? explanation : null)
                .build();
    }
}
