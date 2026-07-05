package greensnail_backend.GreenSnail.service;

import greensnail_backend.GreenSnail.entity.Scenario;
import greensnail_backend.GreenSnail.repository.ScenarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScenarioService {
    private final ScenarioRepository scenarioRepository;

    // 시나리오 저장
    public Scenario saveScenario(Scenario scenario) {
        return scenarioRepository.save(scenario);
    }

    // 모든 시나리오 조회
    public List<Scenario> getAllScenarios() {
        return scenarioRepository.findAll();
    }

    // 특정 시나리오 조회
    public Scenario getScenarioById(Long id) {
        return scenarioRepository.findById(id).orElse(null);
    }
}
