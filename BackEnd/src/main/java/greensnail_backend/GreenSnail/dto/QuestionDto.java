package greensnail_backend.GreenSnail.dto;

public class QuestionDto {
    private int number;
    private String content;

    public QuestionDto(int number, String content) {
        this.number = number;
        this.content = content;
    }

    public int getNumber() {
        return number;
    }

    public String getContent() {
        return content;
    }
}
