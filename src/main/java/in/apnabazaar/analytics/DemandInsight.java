package in.apnabazaar.analytics;

import in.apnabazaar.community.Community;
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

/** One AI-written summary of a community's search activity for one week — generated automatically, never hand-edited. */
@Entity
@Table(name = "demand_insight")
public class DemandInsight {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    @Column(name = "insight_text", nullable = false, columnDefinition = "TEXT")
    private String insightText;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected DemandInsight() {
    }

    public DemandInsight(Community community, LocalDate weekStart, String insightText) {
        this.community = community;
        this.weekStart = weekStart;
        this.insightText = insightText;
    }

    /** Overwrites the text when a weekly insight is regenerated for a week that already has one. */
    public void updateText(String insightText) {
        this.insightText = insightText;
    }

    public UUID getId() {
        return id;
    }

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public String getInsightText() {
        return insightText;
    }
}
