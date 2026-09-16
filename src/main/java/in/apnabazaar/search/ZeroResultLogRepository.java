package in.apnabazaar.search;

import in.apnabazaar.community.Community;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ZeroResultLogRepository extends JpaRepository<ZeroResultLog, UUID> {

    Optional<ZeroResultLog> findByCommunityAndNormalisedQuery(Community community, String normalisedQuery);

    List<ZeroResultLog> findByCommunityOrderByOccurrenceCountDesc(Community community);
}
