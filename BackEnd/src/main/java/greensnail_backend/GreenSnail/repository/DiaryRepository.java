package greensnail_backend.GreenSnail.repository;

import greensnail_backend.GreenSnail.entity.Diary;
import greensnail_backend.GreenSnail.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DiaryRepository extends JpaRepository<Diary, Long> {

    Optional<Diary> findByUserAndDate(User user, LocalDate date);
    
}
