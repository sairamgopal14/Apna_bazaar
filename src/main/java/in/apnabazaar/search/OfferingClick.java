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

/** One buyer tap on "Order on WhatsApp" — the "was acted on" half of click-through rate. */
@Entity
@Table(name = "offering_click")
public class OfferingClick {

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

    @Column(name = "clicked_on", nullable = false)
    private LocalDate clickedOn;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected OfferingClick() {
    }

    public OfferingClick(Community community, Provider provider, Offering offering, LocalDate clickedOn) {
        this.community = community;
        this.provider = provider;
        this.offering = offering;
        this.clickedOn = clickedOn;
    }

    public UUID getId() {
        return id;
    }
}
