package in.apnabazaar.broadcast;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BroadcastRepository extends JpaRepository<Broadcast, UUID> {

    List<Broadcast> findByStatus(BroadcastStatus status);
}
