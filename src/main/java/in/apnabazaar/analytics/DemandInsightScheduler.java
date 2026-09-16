package in.apnabazaar.analytics;

import in.apnabazaar.community.Community;
import in.apnabazaar.community.CommunityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Runs weekly (see {@link in.apnabazaar.broadcast.BroadcastScheduler} for the per-minute
 * equivalent), turning the past 7 days of {@code SearchEvent} data into a saved
 * {@link DemandInsight} per community.
 */
@Component
public class DemandInsightScheduler {

    private static final Logger log = LoggerFactory.getLogger(DemandInsightScheduler.class);

    private final CommunityRepository communityRepository;
    private final DemandInsightRepository demandInsightRepository;
    private final AnalyticsService analyticsService;

    public DemandInsightScheduler(CommunityRepository communityRepository,
                                   DemandInsightRepository demandInsightRepository,
                                   AnalyticsService analyticsService) {
        this.communityRepository = communityRepository;
        this.demandInsightRepository = demandInsightRepository;
        this.analyticsService = analyticsService;
    }

    @Scheduled(cron = "0 0 2 * * MON")
    @Transactional
    public void generateWeeklyInsights() {
        LocalDate weekStart = LocalDate.now().minusDays(7);
        LocalDate weekEnd = LocalDate.now().minusDays(1);

        for (Community community : communityRepository.findAll()) {
            if (demandInsightRepository.existsByCommunityAndWeekStart(community, weekStart)) {
                continue;
            }
            try {
                analyticsService.generateWeeklyInsight(community, weekStart, weekEnd);
                log.info("DemandInsightScheduler: generated insight for {} ({} to {})",
                        community.getSlug(), weekStart, weekEnd);
            } catch (Exception e) {
                log.warn("DemandInsightScheduler: failed for {}: {}", community.getSlug(), e.getMessage());
            }
        }
    }
}
