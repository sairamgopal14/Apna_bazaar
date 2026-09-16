package in.apnabazaar.search;

import in.apnabazaar.community.Community;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Every buyer query, logged for Phase 4 analytics regardless of whether it matched. */
@Entity
@Table(name = "search_event")
public class SearchEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    @Column(name = "raw_query", nullable = false, columnDefinition = "TEXT")
    private String rawQuery;

    @Column(name = "normalised_query", nullable = false, columnDefinition = "TEXT")
    private String normalisedQuery;

    @Column(name = "result_count", nullable = false)
    private int resultCount;

    @Column(name = "had_results", nullable = false)
    private boolean hadResults;

    @Column(name = "query_date", nullable = false)
    private LocalDate queryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "time_bucket", nullable = false, length = 20)
    private TimeBucket timeBucket;

    @Column(name = "day_of_week", nullable = false)
    private short dayOfWeek;

    @Column(name = "session_id")
    private UUID sessionId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected SearchEvent() {
    }

    public SearchEvent(Community community, String rawQuery, String normalisedQuery, int resultCount,
                        boolean hadResults, LocalDate queryDate, TimeBucket timeBucket, short dayOfWeek,
                        UUID sessionId) {
        this.community = community;
        this.rawQuery = rawQuery;
        this.normalisedQuery = normalisedQuery;
        this.resultCount = resultCount;
        this.hadResults = hadResults;
        this.queryDate = queryDate;
        this.timeBucket = timeBucket;
        this.dayOfWeek = dayOfWeek;
        this.sessionId = sessionId;
    }

    public UUID getId() {
        return id;
    }
}
