package greensnail_backend.GreenSnail.dto;

import greensnail_backend.GreenSnail.entity.AgeGroup;
import java.util.List;

public class DiagnosisRequestDto {
    private AgeGroup ageGroup;
    private List<Integer> answers;  // 예=1, 아니오=0

    public AgeGroup getAgeGroup() {
        return ageGroup;
    }

    public List<Integer> getAnswers() {
        return answers;
    }
}
