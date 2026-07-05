package greensnail_backend.GreenSnail.dto;

public class DiagnosisResultDto {
    private int score;
    private String resultType;

    public DiagnosisResultDto(int score, String resultType) {
        this.score = score;
        this.resultType = resultType;
    }

    public int getScore() {
        return score;
    }

    public String getResultType() {
        return resultType;
    }
}
