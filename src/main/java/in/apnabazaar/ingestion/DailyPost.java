package in.apnabazaar.ingestion;

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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/** One seller's header row for one day's upload — which day, and where it came from. */
@Entity
@Table(name = "daily_post")
public class DailyPost {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @Column(name = "post_date", nullable = false)
    private LocalDate postDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostSource source;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected DailyPost() {
    }

    public DailyPost(Provider provider, LocalDate postDate, PostSource source) {
        this.provider = provider;
        this.postDate = postDate;
        this.source = source;
    }

    public UUID getId() {
        return id;
    }

    public Provider getProvider() {
        return provider;
    }

    public LocalDate getPostDate() {
        return postDate;
    }

    public PostSource getSource() {
        return source;
    }
}
