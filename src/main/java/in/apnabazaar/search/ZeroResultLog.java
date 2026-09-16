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

import java.time.LocalDate;
import java.util.UUID;

/** Every distinct failed query, deduplicated per community — the onboarding opportunity tracker. */
@Entity
@Table(name = "zero_result_log")
public class ZeroResultLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    @Column(name = "normalised_query", nullable = false, columnDefinition = "TEXT")
    private String normalisedQuery;

    @Column(name = "occurrence_count", nullable = false)
    private int occurrenceCount;

    @Column(name = "first_seen", nullable = false)
    private LocalDate firstSeen;

    @Column(name = "last_seen", nullable = false)
    private LocalDate lastSeen;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ZeroResultStatus status;

    protected ZeroResultLog() {
    }

    public ZeroResultLog(Community community, String normalisedQuery, LocalDate seenOn) {
        this.community = community;
        this.normalisedQuery = normalisedQuery;
        this.occurrenceCount = 1;
        this.firstSeen = seenOn;
        this.lastSeen = seenOn;
        this.status = ZeroResultStatus.new_;
    }

    public void recordAnotherOccurrence(LocalDate seenOn) {
        this.occurrenceCount++;
        this.lastSeen = seenOn;
    }

    public UUID getId() {
        return id;
    }

    public Community getCommunity() {
        return community;
    }

    public String getNormalisedQuery() {
        return normalisedQuery;
    }

    public int getOccurrenceCount() {
        return occurrenceCount;
    }

    public LocalDate getFirstSeen() {
        return firstSeen;
    }

    public LocalDate getLastSeen() {
        return lastSeen;
    }

    public ZeroResultStatus getStatus() {
        return status;
    }
}
