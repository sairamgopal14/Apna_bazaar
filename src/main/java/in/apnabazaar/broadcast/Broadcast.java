package in.apnabazaar.broadcast;

import in.apnabazaar.community.Community;
import in.apnabazaar.provider.Provider;
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

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * One outgoing nudge — written by the admin directly, chosen from Gemini's
 * suggestions, or paid for by a seller. {@code searchesTriggered}/{@code whatsappTaps}
 * are never computed automatically (no real WhatsApp delivery exists to track) —
 * they're filled in by {@link #logPerformance} whenever the admin reports what
 * they personally observed.
 */
@Entity
@Table(name = "broadcast")
public class Broadcast {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    /** Only set for {@link BroadcastType#sponsored} — which seller paid for this. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private Provider provider;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "broadcast_type", nullable = false, length = 20)
    private BroadcastType broadcastType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BroadcastStatus status;

    @Column(name = "scheduled_at")
    private OffsetDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_pattern", length = 20)
    private RecurrencePattern recurrencePattern;

    @Column(name = "last_sent_at")
    private OffsetDateTime lastSentAt;

    @Column(name = "promo_amount")
    private Integer promoAmount;

    @Column(name = "searches_triggered", nullable = false)
    private int searchesTriggered;

    @Column(name = "whatsapp_taps", nullable = false)
    private int whatsappTaps;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected Broadcast() {
    }

    public Broadcast(Community community, Provider provider, String message, BroadcastType broadcastType,
                      BroadcastStatus status, OffsetDateTime scheduledAt, RecurrencePattern recurrencePattern,
                      Integer promoAmount) {
        this.community = community;
        this.provider = provider;
        this.message = message;
        this.broadcastType = broadcastType;
        this.status = status;
        this.scheduledAt = scheduledAt;
        this.recurrencePattern = recurrencePattern;
        this.promoAmount = promoAmount;
        this.searchesTriggered = 0;
        this.whatsappTaps = 0;
    }

    /** A one-time (scheduled or immediate) broadcast has now gone out. */
    public void markSent() {
        this.status = BroadcastStatus.sent;
        this.lastSentAt = OffsetDateTime.now();
    }

    /** A recurring broadcast fired for today — status stays {@code recurring_active}. */
    public void recordRecurringFire() {
        this.lastSentAt = OffsetDateTime.now();
    }

    /** The admin's manual report of what they observed after the fact. */
    public void logPerformance(int searchesTriggered, int whatsappTaps) {
        this.searchesTriggered = searchesTriggered;
        this.whatsappTaps = whatsappTaps;
    }

    public UUID getId() {
        return id;
    }

    public Community getCommunity() {
        return community;
    }

    public Provider getProvider() {
        return provider;
    }

    public String getMessage() {
        return message;
    }

    public BroadcastType getBroadcastType() {
        return broadcastType;
    }

    public BroadcastStatus getStatus() {
        return status;
    }

    public OffsetDateTime getScheduledAt() {
        return scheduledAt;
    }

    public RecurrencePattern getRecurrencePattern() {
        return recurrencePattern;
    }

    public OffsetDateTime getLastSentAt() {
        return lastSentAt;
    }

    public Integer getPromoAmount() {
        return promoAmount;
    }

    public int getSearchesTriggered() {
        return searchesTriggered;
    }

    public int getWhatsappTaps() {
        return whatsappTaps;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
