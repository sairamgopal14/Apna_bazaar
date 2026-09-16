package in.apnabazaar.offering;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * A seller's recurring weekly default timing for one offering — introduced in
 * Phase 2 so the admin's daily sheet can leave timing columns blank when
 * nothing changed, instead of retyping "5-9 AM" every morning. Ingestion reads
 * this as a fallback whenever a day's row doesn't specify a value; the values
 * actually saved onto that day's {@link DailyLineItem} are always fully
 * resolved by the time they're written, so nothing downstream of ingestion
 * (search, ordering-window logic) ever needs to know this table exists.
 */
@Entity
@Table(name = "offering_schedule")
public class OfferingSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offering_id", nullable = false, unique = true)
    private Offering offering;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_type", nullable = false, length = 20)
    private DeliveryType deliveryType;

    @Column(name = "serves_from")
    private LocalTime servesFrom;

    @Column(name = "serves_to")
    private LocalTime servesTo;

    @Column(name = "accepts_realtime", nullable = false)
    private boolean acceptsRealtime;

    @Column(name = "realtime_cutoff_minutes")
    private Integer realtimeCutoffMinutes;

    @Column(name = "preorder_required", nullable = false)
    private boolean preorderRequired;

    @Column(name = "preorder_closes_at")
    private LocalTime preorderClosesAt;

    @Column(name = "preorder_day_offset", nullable = false)
    private short preorderDayOffset;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected OfferingSchedule() {
    }

    public OfferingSchedule(Offering offering, DeliveryType deliveryType, LocalTime servesFrom, LocalTime servesTo,
                             boolean acceptsRealtime, Integer realtimeCutoffMinutes, boolean preorderRequired,
                             LocalTime preorderClosesAt, short preorderDayOffset) {
        this.offering = offering;
        this.deliveryType = deliveryType;
        this.servesFrom = servesFrom;
        this.servesTo = servesTo;
        this.acceptsRealtime = acceptsRealtime;
        this.realtimeCutoffMinutes = realtimeCutoffMinutes;
        this.preorderRequired = preorderRequired;
        this.preorderClosesAt = preorderClosesAt;
        this.preorderDayOffset = preorderDayOffset;
    }

    /** Updates whichever recurring defaults the admin just explicitly re-stated in a day's sheet. */
    public void applyUpdate(DeliveryType deliveryType, LocalTime servesFrom, LocalTime servesTo,
                             boolean preorderRequired, LocalTime preorderClosesAt, short preorderDayOffset) {
        this.deliveryType = deliveryType;
        this.servesFrom = servesFrom;
        this.servesTo = servesTo;
        this.preorderRequired = preorderRequired;
        this.preorderClosesAt = preorderClosesAt;
        this.preorderDayOffset = preorderDayOffset;
    }

    public UUID getId() {
        return id;
    }

    public Offering getOffering() {
        return offering;
    }

    public DeliveryType getDeliveryType() {
        return deliveryType;
    }

    public LocalTime getServesFrom() {
        return servesFrom;
    }

    public LocalTime getServesTo() {
        return servesTo;
    }

    public boolean isAcceptsRealtime() {
        return acceptsRealtime;
    }

    public Integer getRealtimeCutoffMinutes() {
        return realtimeCutoffMinutes;
    }

    public boolean isPreorderRequired() {
        return preorderRequired;
    }

    public LocalTime getPreorderClosesAt() {
        return preorderClosesAt;
    }

    public short getPreorderDayOffset() {
        return preorderDayOffset;
    }
}
