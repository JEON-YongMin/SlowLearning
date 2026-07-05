package greensnail_backend.GreenSnail.entity;

import greensnail_backend.GreenSnail.dto.SupportOrganizationDto;
import java.util.Arrays;
import java.util.List;

public class SupportOrganization {

    // 하드코딩된 공공기관 단체 목록 (4개, id 추가)
    private static final List<SupportOrganizationDto> ORGANIZATIONS = Arrays.asList(
            SupportOrganizationDto.builder()
                    .id(1L)
                    .organizationName("밈센터 이용 안내")
                    .homepageUrl("https://sbifc.org/bbs/board.php?bo_table=B10")
                    .build(),
            SupportOrganizationDto.builder()
                    .id(2L)
                    .organizationName("프로그램 안내 및 신청")
                    .homepageUrl("https://sbifc.org/bbs/board.php?bo_table=B12")
                    .build(),
            SupportOrganizationDto.builder()
                    .id(3L)
                    .organizationName("함께하랑 사회협동조합")
                    .homepageUrl("https://withharang2023.modoo.at/")
                    .build(),
            SupportOrganizationDto.builder()
                    .id(4L)
                    .organizationName("고양시 경계성 지능인 평생교육")
                    .homepageUrl("https://www.goyang.go.kr/edu/M000046/S001/conts.do")
                    .build()
    );

    // 단체 목록 조회
    public static List<SupportOrganizationDto> getOrganizations() {
        return ORGANIZATIONS;
    }
}