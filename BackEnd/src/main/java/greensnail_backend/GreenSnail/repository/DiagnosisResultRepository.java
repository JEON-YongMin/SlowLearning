package greensnail_backend.GreenSnail.repository;

import greensnail_backend.GreenSnail.entity.DiagnosisResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiagnosisResultRepository extends JpaRepository<DiagnosisResult, Long> {
    // DB에 진단 결과 저장/조회하는 역할
}
