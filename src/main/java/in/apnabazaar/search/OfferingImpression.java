package in.apnabazaar.search;

import in.apnabazaar.community.Community;
import in.apnabazaar.offering.Offering;
import in.apnabazaar.provider.Provider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/** One offering shown on one search result card — the "was seen" half of click-through rate. */
@Entity
@Table(name = "offering_impression")
public class OfferingImpression {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offering_id", nullable = false)
    private Offering offering;

    @Column(name = "shown_on", nullable = false)
    private LocalDate shownOn;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected OfferingImpression() {
    }

    public OfferingImpression(Community community, Provider provider, Offering offering, LocalDate shownOn) {
        this.community = community;
        this.provider = provider;
        this.offering = offering;
        this.shownOn = shownOn;
    }

    public UUID getId() {
        return id;
    }
}
