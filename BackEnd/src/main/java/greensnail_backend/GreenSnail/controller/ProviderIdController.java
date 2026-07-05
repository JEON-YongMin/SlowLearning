package greensnail_backend.GreenSnail.controller;

import greensnail_backend.GreenSnail.entity.User;
import greensnail_backend.GreenSnail.global.api.ApiResponse;
import greensnail_backend.GreenSnail.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
@Tag(name = "Provider", description = "프로바이더 ID 관리 API")
public class ProviderIdController {

    private final UserRepository userRepository;

    @Operation(
            summary = "모든 프로바이더 ID 조회",
            description = "시스템에 등록된 모든 사용자의 프로바이더 ID를 조회합니다. 스웨거 테스트용으로 활용할 수 있습니다."
    )
    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getAllProviderIds() {
        List<User> users = userRepository.findAll();

        List<Map<String, Object>> providerInfoList = users.stream()
                .map(user -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("userId", user.getId());
                    info.put("providerId", user.getProviderId());
                    info.put("nickname", user.getNickname());
                    return info;
                })
                .collect(Collectors.toList());

        return ApiResponse.success(providerInfoList);
    }
}