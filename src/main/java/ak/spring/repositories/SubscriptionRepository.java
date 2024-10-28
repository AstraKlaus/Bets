package ak.spring.repositories;

import ak.spring.models.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByPersonId(Long personId);
}
