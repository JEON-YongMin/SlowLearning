package greensnail_backend.GreenSnail.dto;

import greensnail_backend.GreenSnail.entity.AgeCategory;
import greensnail_backend.GreenSnail.entity.JobCategory;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostUpdateRequestDto {
    private String retitle;
    private String recontent;
    private JobCategory jobCategory;
    private AgeCategory ageCategory;
}
