package greensnail_backend.GreenSnail.dto;

import greensnail_backend.GreenSnail.entity.Diary;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiaryResponseDto {
    private Long id;
    private String content;
    private String aiFeedback;
    private String date;
    private String createdAt;

    public static DiaryResponseDto fromEntity(Diary diary) {
        return DiaryResponseDto.builder()
                .id(diary.getId())
                .content(diary.getContent())
                .aiFeedback(diary.getAiFeedback())
                .date(diary.getDate().toString())
                .createdAt(diary.getCreatedAt().toString())
                .build();
    }
}
