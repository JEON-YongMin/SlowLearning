package greensnail_backend.GreenSnail.controller;

import greensnail_backend.GreenSnail.dto.SupportOrganizationDto;
import greensnail_backend.GreenSnail.global.api.ApiResponse;
import greensnail_backend.GreenSnail.global.api.SuccessCode;
import greensnail_backend.GreenSnail.service.SupportOrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "지원 단체", description = "공공기관 지원 단체 관련 API입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/support-organizations")
public class SupportOrganizationController {

    private final SupportOrganizationService supportOrganizationService;

    @Operation(summary = "공공기관 지원 단체 조회", description = "공공기관 지원 단체 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
    })
    @GetMapping
    public ApiResponse<List<SupportOrganizationDto>> getOrganizations() {
        List<SupportOrganizationDto> organizations = supportOrganizationService.getOrganizations();
        return ApiResponse.success(organizations, SuccessCode.OK);
    }
}