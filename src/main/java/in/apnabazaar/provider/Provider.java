package in.apnabazaar.provider;

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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "provider")
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "shop_name", nullable = false, length = 200)
    private String shopName;

    @Column(name = "flat_number", length = 50)
    private String flatNumber;

    @Column(name = "whatsapp_number", nullable = false, length = 20)
    private String whatsappNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false, length = 20)
    private ProviderType providerType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProviderStatus status;

    // see ReservationService.recalculateProviderRating.
    @Column(precision = 3, scale = 2)
    private BigDecimal rating;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected Provider() {
    }

    public Provider(Community community, String name, String shopName, String flatNumber, String whatsappNumber,
                     ProviderType providerType) {
        this.community = community;
        this.name = name;
        this.shopName = shopName;
        this.flatNumber = flatNumber;
        this.whatsappNumber = whatsappNumber;
        this.providerType = providerType;
        this.status = ProviderStatus.active;
    }

    /** A seller who didn't appear in today's upload — stops showing up in search until they upload again. */
    public void deactivate() {
        this.status = ProviderStatus.inactive;
    }

    /** A previously-deactivated seller who has reappeared in a new upload — findable again. */
    public void reactivate() {
        this.status = ProviderStatus.active;
    }

    /** Recomputed from real order_slot ratings only — never set any other way. */
    public void updateRating(BigDecimal rating) {
        this.rating = rating;
    }

    public UUID getId() {
        return id;
    }

    public Community getCommunity() {
        return community;
    }

    public String getName() {
        return name;
    }

    public String getShopName() {
        return shopName;
    }

    public String getFlatNumber() {
        return flatNumber;
    }

    public String getWhatsappNumber() {
        return whatsappNumber;
    }

    public ProviderType getProviderType() {
        return providerType;
    }

    public ProviderStatus getStatus() {
        return status;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
