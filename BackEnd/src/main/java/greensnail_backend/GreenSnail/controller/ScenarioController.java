package greensnail_backend.GreenSnail.controller;

import greensnail_backend.GreenSnail.entity.Scenario;
import greensnail_backend.GreenSnail.service.ScenarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scenario")
@RequiredArgsConstructor
public class ScenarioController {

    private final ScenarioService scenarioService;

    /**
     * 시나리오 저장 API
     * @param scenario 저장할 시나리오 객체
     * @return 저장된 시나리오
     */
    @Operation(summary = "시나리오 저장", description = "새로운 시나리오를 저장합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "시나리오 저장 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 시나리오 데이터")
    })
    @PostMapping
    public Scenario saveScenario(@RequestBody Scenario scenario) {
        return scenarioService.saveScenario(scenario);
    }

    /**
     * 모든 시나리오 조회 API
     * @return 모든 시나리오 목록
     */
    @Operation(summary = "모든 시나리오 조회", description = "저장된 모든 시나리오를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "시나리오 목록 조회 성공")
    })
    @GetMapping
    public List<Scenario> getAllScenarios() {
        return scenarioService.getAllScenarios();
    }

    /**
     * 특정 시나리오 조회 API
     * @param id 조회할 시나리오의 ID
     * @return 해당 ID의 시나리오
     */
    @Operation(summary = "특정 시나리오 조회", description = "특정 시나리오를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "시나리오 조회 성공"),
            @ApiResponse(responseCode = "404", description = "시나리오 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public Scenario getScenarioById(@PathVariable Long id) {
        return scenarioService.getScenarioById(id);
    }
}
