package greensnail_backend.GreenSnail.repository;

import greensnail_backend.GreenSnail.entity.LearningProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LearningProgressRepository extends JpaRepository<LearningProgress, Long> {
    Optional<LearningProgress> findByProviderIdAndMonthInfo(String providerId, String yearMonth);
}