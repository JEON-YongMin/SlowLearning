package greensnail_backend.GreenSnail.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WordQuizResult {

    private LocalDate quizDate;
    private boolean isSolved;
    private List<WordQuizItem> quizList;
}
