package in.apnabazaar.offering;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OfferingScheduleRepository extends JpaRepository<OfferingSchedule, UUID> {

    Optional<OfferingSchedule> findByOfferingId(UUID offeringId);
}
