package greensnail_backend.GreenSnail.dto;

import greensnail_backend.GreenSnail.entity.AgeCategory;
import greensnail_backend.GreenSnail.entity.JobCategory;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostRequestDto {
    private String title;
    private String content;
    private JobCategory jobCategory;
    private AgeCategory ageCategory;
}
