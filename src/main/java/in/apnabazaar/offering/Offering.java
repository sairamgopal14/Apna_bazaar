package in.apnabazaar.offering;

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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Permanent catalog entry registered once by the seller.
 * {@link #description} is the field Claude reads for semantic matching —
 * today's price/availability lives separately on {@link in.apnabazaar.offering.DailyLineItem}.
 */
@Entity
@Table(name = "offering")
public class Offering {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_price", precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "offering_type", nullable = false, length = 20)
    private OfferingType offeringType;

    @Column(name = "is_available", nullable = false)
    private boolean available;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected Offering() {
    }

    public Offering(Provider provider, String name, String description, BigDecimal basePrice,
                     OfferingType offeringType) {
        this.provider = provider;
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.offeringType = offeringType;
        this.available = true;
    }

    public UUID getId() {
        return id;
    }

    public Provider getProvider() {
        return provider;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public OfferingType getOfferingType() {
        return offeringType;
    }

    public boolean isAvailable() {
        return available;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
