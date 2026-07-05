package greensnail_backend.GreenSnail.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SummaryRequestDto {
    private Long articleId;
    private String userSummary;
}
