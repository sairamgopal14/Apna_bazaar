package in.apnabazaar.offering;

import in.apnabazaar.ingestion.DailyPost;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Today's specific price/availability/timing for an {@link Offering}.
 * Window fields (serves/preorder/realtime) are always fully resolved by the
 * time a row is saved — ingestion fills any blank left in the day's upload
 * from {@link OfferingSchedule} before this row is ever written, so nothing
 * downstream (search, ordering-window logic) needs to know a schedule exists.
 */
@Entity
@Table(name = "daily_line_item")
public class DailyLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offering_id", nullable = false)
    private Offering offering;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_post_id")
    private DailyPost dailyPost;

    @Column(name = "item_date", nullable = false)
    private LocalDate itemDate;

    @Column(name = "item_name", nullable = false, length = 200)
    private String itemName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

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

    /** 0 = pre-order closes same day as {@link #itemDate}; -1 = closes the day before. */
    @Column(name = "preorder_day_offset", nullable = false)
    private short preorderDayOffset;

    @Column(name = "is_available", nullable = false)
    private boolean available;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected DailyLineItem() {
    }

    public DailyLineItem(Offering offering, DailyPost dailyPost, LocalDate itemDate, String itemName,
                          BigDecimal price, DeliveryType deliveryType, LocalTime servesFrom, LocalTime servesTo,
                          boolean acceptsRealtime, Integer realtimeCutoffMinutes, boolean preorderRequired,
                          LocalTime preorderClosesAt, short preorderDayOffset) {
        this.offering = offering;
        this.dailyPost = dailyPost;
        this.itemDate = itemDate;
        this.itemName = itemName;
        this.price = price;
        this.deliveryType = deliveryType;
        this.servesFrom = servesFrom;
        this.servesTo = servesTo;
        this.acceptsRealtime = acceptsRealtime;
        this.realtimeCutoffMinutes = realtimeCutoffMinutes;
        this.preorderRequired = preorderRequired;
        this.preorderClosesAt = preorderClosesAt;
        this.preorderDayOffset = preorderDayOffset;
        this.available = true;
    }

    /**
     * Overwrites this row's daily-changing fields in place — used when the admin re-uploads
     * a corrected sheet for a day that's already been ingested. Identity ({@code id}), the
     * link to {@link #offering}, and {@link #itemDate} never change; only what a seller
     * could plausibly correct about the same day's listing does.
     */
    public void applyDailyUpdate(DailyPost dailyPost, String itemName, BigDecimal price, DeliveryType deliveryType,
                                  LocalTime servesFrom, LocalTime servesTo, boolean acceptsRealtime,
                                  Integer realtimeCutoffMinutes, boolean preorderRequired,
                                  LocalTime preorderClosesAt, short preorderDayOffset) {
        this.dailyPost = dailyPost;
        this.itemName = itemName;
        this.price = price;
        this.deliveryType = deliveryType;
        this.servesFrom = servesFrom;
        this.servesTo = servesTo;
        this.acceptsRealtime = acceptsRealtime;
        this.realtimeCutoffMinutes = realtimeCutoffMinutes;
        this.preorderRequired = preorderRequired;
        this.preorderClosesAt = preorderClosesAt;
        this.preorderDayOffset = preorderDayOffset;
        this.available = true;
    }

    public UUID getId() {
        return id;
    }

    public Offering getOffering() {
        return offering;
    }

    public DailyPost getDailyPost() {
        return dailyPost;
    }

    public LocalDate getItemDate() {
        return itemDate;
    }

    public String getItemName() {
        return itemName;
    }

    public BigDecimal getPrice() {
        return price;
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

    public boolean isAvailable() {
        return available;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
