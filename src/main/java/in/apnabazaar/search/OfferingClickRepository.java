package in.apnabazaar.search;

import in.apnabazaar.community.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface OfferingClickRepository extends JpaRepository<OfferingClick, UUID> {

    @Query("""
            select new in.apnabazaar.search.ProviderCount(oc.provider.id, count(oc))
            from OfferingClick oc
            where oc.community = :community and oc.clickedOn = :date
            group by oc.provider.id
            """)
    List<ProviderCount> countByProviderForDate(Community community, LocalDate date);
}
