package greensnail_backend.GreenSnail.repository;

import greensnail_backend.GreenSnail.entity.Scenario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScenarioRepository extends JpaRepository<Scenario, Long> {
}
