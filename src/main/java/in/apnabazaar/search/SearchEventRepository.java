package in.apnabazaar.search;

import in.apnabazaar.community.Community;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SearchEventRepository extends JpaRepository<SearchEvent, UUID> {

    long countByCommunityAndQueryDate(Community community, LocalDate queryDate);

    long countByCommunityAndQueryDateAndHadResultsFalse(Community community, LocalDate queryDate);

    long countByCommunityAndQueryDateBetween(Community community, LocalDate start, LocalDate end);

    long countByCommunityAndQueryDateBetweenAndHadResultsFalse(Community community, LocalDate start, LocalDate end);

    @Query("""
            select new in.apnabazaar.search.TimeBucketCountView(se.timeBucket, count(se))
            from SearchEvent se
            where se.community = :community and se.queryDate = :queryDate
            group by se.timeBucket
            order by count(se) desc
            """)
    List<TimeBucketCountView> countByTimeBucketForDate(Community community, LocalDate queryDate);

    @Query("""
            select new in.apnabazaar.search.TopQueryView(se.normalisedQuery, count(se))
            from SearchEvent se
            where se.community = :community and se.queryDate = :queryDate
            group by se.normalisedQuery
            order by count(se) desc
            """)
    List<TopQueryView> topQueriesForDate(Community community, LocalDate queryDate, Pageable pageable);

    @Query("""
            select new in.apnabazaar.search.TopQueryView(se.normalisedQuery, count(se))
            from SearchEvent se
            where se.community = :community and se.queryDate between :start and :end
            group by se.normalisedQuery
            order by count(se) desc
            """)
    List<TopQueryView> topQueriesForDateRange(Community community, LocalDate start, LocalDate end, Pageable pageable);
}
