package in.apnabazaar.offering;

import in.apnabazaar.community.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DailyLineItemRepository extends JpaRepository<DailyLineItem, UUID> {

    Optional<DailyLineItem> findByOfferingIdAndItemDate(UUID offeringId, LocalDate itemDate);

    @Query("""
            select d from DailyLineItem d
            where d.offering.id in :offeringIds and d.itemDate = :itemDate
            """)
    List<DailyLineItem> findByOfferingIdInAndItemDate(List<UUID> offeringIds, LocalDate itemDate);

    @Query("""
            select count(distinct dli.offering.provider)
            from DailyLineItem dli
            where dli.offering.provider.community = :community and dli.itemDate = :date and dli.available = true
            """) // answeres the active selllers
    long countDistinctActiveSellers(Community community, LocalDate date);
}
