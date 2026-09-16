package in.apnabazaar.broadcast;

import in.apnabazaar.community.Community;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BroadcastRepository extends JpaRepository<Broadcast, UUID> {

    List<Broadcast> findByStatus(BroadcastStatus status);

    List<Broadcast> findByCommunityOrderByCreatedAtDesc(Community community, Pageable pageable);
}
