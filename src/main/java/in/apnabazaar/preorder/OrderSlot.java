package in.apnabazaar.preorder;

import in.apnabazaar.offering.DailyLineItem;
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

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * One real reservation -- the first genuinely real "this buyer intends to order" signal
 * in the whole app. Everything before this phase only ever displayed information;
 * this is the first thing that gets permanently remembered because a buyer acted on it.
 */
@Entity
@Table(name = "order_slot")
public class OrderSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offering_id", nullable = false)
    private Offering offering;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_line_item_id", nullable = false)
    private DailyLineItem dailyLineItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @CreationTimestamp
    @Column(name = "reserved_at", nullable = false, updatable = false)
    private OffsetDateTime reservedAt;

    @Column
    private Short rating;

    @Column(name = "rated_at")
    private OffsetDateTime ratedAt;

    protected OrderSlot() {
    }

    public OrderSlot(Offering offering, DailyLineItem dailyLineItem, Provider provider, UUID sessionId) {
        this.offering = offering;
        this.dailyLineItem = dailyLineItem;
        this.provider = provider;
        this.sessionId = sessionId;
    }

    /** Records the buyer's real answer to "how was it?" -- the only thing that ever updates a rating. */
    public void submitRating(int rating) {
        this.rating = (short) rating;
        this.ratedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public Offering getOffering() {
        return offering;
    }

    public DailyLineItem getDailyLineItem() {
        return dailyLineItem;
    }

    public Provider getProvider() {
        return provider;
    }

    public OffsetDateTime getReservedAt() {
        return reservedAt;
    }

    public Short getRating() {
        return rating;
    }
}
