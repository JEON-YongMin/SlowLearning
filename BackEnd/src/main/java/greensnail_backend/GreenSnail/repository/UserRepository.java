package greensnail_backend.GreenSnail.repository;

import greensnail_backend.GreenSnail.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderId(String providerId);
    boolean existsByProviderId(String providerId);

    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}