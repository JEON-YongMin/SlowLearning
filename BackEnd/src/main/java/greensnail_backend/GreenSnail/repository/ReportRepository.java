package greensnail_backend.GreenSnail.repository;

import greensnail_backend.GreenSnail.entity.Report;
import greensnail_backend.GreenSnail.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

    int countByReportedUser(User user);
}