package greensnail_backend.GreenSnail.service;

import greensnail_backend.GreenSnail.dto.SupportOrganizationDto;
import greensnail_backend.GreenSnail.entity.SupportOrganization;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupportOrganizationService {

    // 공공기관 단체 목록 조회
    public List<SupportOrganizationDto> getOrganizations() {
        return SupportOrganization.getOrganizations();
    }
}