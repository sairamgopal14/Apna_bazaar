package in.apnabazaar.search;

import in.apnabazaar.community.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface OfferingImpressionRepository extends JpaRepository<OfferingImpression, UUID> {

    @Query("""
            select new in.apnabazaar.search.ProviderCount(oi.provider.id, count(oi))
            from OfferingImpression oi
            where oi.community = :community and oi.shownOn = :date
            group by oi.provider.id
            """)
    List<ProviderCount> countByProviderForDate(Community community, LocalDate date);
}
