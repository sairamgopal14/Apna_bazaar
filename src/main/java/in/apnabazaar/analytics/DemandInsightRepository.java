package in.apnabazaar.analytics;

import in.apnabazaar.community.Community;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface DemandInsightRepository extends JpaRepository<DemandInsight, UUID> {

    Optional<DemandInsight> findTopByCommunityOrderByWeekStartDesc(Community community);

    Optional<DemandInsight> findByCommunityAndWeekStart(Community community, LocalDate weekStart);

    boolean existsByCommunityAndWeekStart(Community community, LocalDate weekStart);
}
