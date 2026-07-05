package greensnail_backend.GreenSnail.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportOrganizationDto {

    private Long id;
    private String organizationName;  // 단체명
    private String homepageUrl;       // 홈페이지 링크
}