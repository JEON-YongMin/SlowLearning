package greensnail_backend.GreenSnail.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WordQuizItem {

    private Long wordId;
    private Long quizId;
    private String word;
    private String easyDefinition;
    private String example;
    private String dictionaryDefinition;
    private String question;
    private List<String> choices;
    private String answer;
    private String explanation;
}
