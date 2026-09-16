package in.apnabazaar.analytics;

import in.apnabazaar.community.Community;
import in.apnabazaar.community.CommunityRepository;
import in.apnabazaar.gemini.GeminiInsightClient;
import in.apnabazaar.offering.DailyLineItemRepository;
import in.apnabazaar.provider.Provider;
import in.apnabazaar.provider.ProviderRepository;
import in.apnabazaar.search.OfferingClickRepository;
import in.apnabazaar.search.OfferingImpressionRepository;
import in.apnabazaar.search.ProviderCount;
import in.apnabazaar.search.SearchEventRepository;
import in.apnabazaar.search.TimeBucketCountView;
import in.apnabazaar.search.TopQueryView;
import in.apnabazaar.search.ZeroResultLog;
import in.apnabazaar.search.ZeroResultLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/** Builds the admin dashboard and generates the weekly AI insight from aggregated search data. */
@Service
public class AnalyticsService {

    private final CommunityRepository communityRepository;
    private final SearchEventRepository searchEventRepository;
    private final DailyLineItemRepository dailyLineItemRepository;
    private final OfferingImpressionRepository offeringImpressionRepository;
    private final OfferingClickRepository offeringClickRepository;
    private final ProviderRepository providerRepository;
    private final ZeroResultLogRepository zeroResultLogRepository;
    private final DemandInsightRepository demandInsightRepository;
    private final GeminiInsightClient geminiInsightClient;

    public AnalyticsService(CommunityRepository communityRepository, SearchEventRepository searchEventRepository,
                             DailyLineItemRepository dailyLineItemRepository,
                             OfferingImpressionRepository offeringImpressionRepository,
                             OfferingClickRepository offeringClickRepository, ProviderRepository providerRepository,
                             ZeroResultLogRepository zeroResultLogRepository,
                             DemandInsightRepository demandInsightRepository,
                             GeminiInsightClient geminiInsightClient) {
        this.communityRepository = communityRepository;
        this.searchEventRepository = searchEventRepository;
        this.dailyLineItemRepository = dailyLineItemRepository;
        this.offeringImpressionRepository = offeringImpressionRepository;
        this.offeringClickRepository = offeringClickRepository;
        this.providerRepository = providerRepository;
        this.zeroResultLogRepository = zeroResultLogRepository;
        this.demandInsightRepository = demandInsightRepository;
        this.geminiInsightClient = geminiInsightClient;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String communitySlug) {
        Community community = findCommunity(communitySlug);
        LocalDate today = LocalDate.now();

        long totalSearches = searchEventRepository.countByCommunityAndQueryDate(community, today);
        long zeroResultCount = searchEventRepository.countByCommunityAndQueryDateAndHadResultsFalse(community, today);
        long activeSellers = dailyLineItemRepository.countDistinctActiveSellers(community, today);
        List<TopQueryView> topQueries = searchEventRepository.topQueriesForDate(community, today, PageRequest.of(0, 5));
        String peakSearchTime = peakSearchTime(community, today);
        List<SellerClickThroughView> ctrViews = buildClickThroughViews(community, today);

        return demandInsightRepository.findTopByCommunityOrderByWeekStartDesc(community)
                .map(insight -> new DashboardResponse(today, totalSearches, zeroResultCount, peakSearchTime,
                        activeSellers, topQueries, ctrViews, insight.getInsightText(), insight.getWeekStart()))
                .orElseGet(() -> new DashboardResponse(today, totalSearches, zeroResultCount, peakSearchTime,
                        activeSellers, topQueries, ctrViews, null, null));
    }

    /** Lets an admin trigger the weekly insight on demand (rather than waiting for Monday 2 AM) using the last 7 days. */
    @Transactional
    public InsightResponse generateInsightNow(String communitySlug) {
        Community community = findCommunity(communitySlug);
        LocalDate weekEnd = LocalDate.now();
        LocalDate weekStart = weekEnd.minusDays(6);

        DemandInsight insight = generateWeeklyInsight(community, weekStart, weekEnd);
        return new InsightResponse(communitySlug, insight.getWeekStart(), weekEnd, insight.getInsightText());
    }

    @Transactional
    public DemandInsight generateWeeklyInsight(Community community, LocalDate weekStart, LocalDate weekEnd) {
        long totalSearches = searchEventRepository.countByCommunityAndQueryDateBetween(community, weekStart, weekEnd);
        long zeroResultSearches = searchEventRepository
                .countByCommunityAndQueryDateBetweenAndHadResultsFalse(community, weekStart, weekEnd);
        List<TopQueryView> topQueries = searchEventRepository
                .topQueriesForDateRange(community, weekStart, weekEnd, PageRequest.of(0, 5));
        List<ZeroResultLog> persistentGaps = zeroResultLogRepository
                .findByCommunityOrderByOccurrenceCountDesc(community).stream()
                .limit(3)
                .toList();

        String summary = buildWeeklySummary(weekStart, weekEnd, totalSearches, zeroResultSearches, topQueries,
                persistentGaps);
        String insightText = geminiInsightClient.generateInsight(summary);
        if (insightText == null || insightText.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Gemini did not return an insight");
        }

        return demandInsightRepository.findByCommunityAndWeekStart(community, weekStart)
                .map(existing -> {
                    existing.updateText(insightText);
                    return existing;
                })
                .orElseGet(() -> demandInsightRepository.save(new DemandInsight(community, weekStart, insightText)));
    }

    private Community findCommunity(String communitySlug) {
        return communityRepository.findBySlug(communitySlug)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Unknown community: " + communitySlug));
    }

    private String peakSearchTime(Community community, LocalDate date) {
        List<TimeBucketCountView> counts = searchEventRepository.countByTimeBucketForDate(community, date);
        return counts.isEmpty() ? null : counts.get(0).timeBucket().name();
    }

    private List<SellerClickThroughView> buildClickThroughViews(Community community, LocalDate date) {
        Map<UUID, Long> impressions = offeringImpressionRepository.countByProviderForDate(community, date).stream()
                .collect(Collectors.toMap(ProviderCount::providerId, ProviderCount::count));
        Map<UUID, Long> clicks = offeringClickRepository.countByProviderForDate(community, date).stream()
                .collect(Collectors.toMap(ProviderCount::providerId, ProviderCount::count));

        return impressions.entrySet().stream()
                .map(entry -> toClickThroughView(entry.getKey(), entry.getValue(),
                        clicks.getOrDefault(entry.getKey(), 0L)))
                .sorted(Comparator.comparingDouble(SellerClickThroughView::clickThroughRate).reversed())
                .toList();
    }

    private SellerClickThroughView toClickThroughView(UUID providerId, long shown, long clicked) {
        String shopName = providerRepository.findById(providerId).map(Provider::getShopName).orElse("Unknown seller");
        double ctr = shown == 0 ? 0.0 : (clicked * 100.0) / shown;
        return new SellerClickThroughView(providerId, shopName, shown, clicked, ctr);
    }

    private String buildWeeklySummary(LocalDate weekStart, LocalDate weekEnd, long totalSearches,
                                       long zeroResultSearches, List<TopQueryView> topQueries,
                                       List<ZeroResultLog> persistentGaps) {
        String topQueriesText = topQueries.stream()
                .map(q -> q.normalisedQuery() + " (" + q.searchCount() + ")")
                .collect(Collectors.joining(", "));
        String gapsText = persistentGaps.stream()
                .map(g -> g.getNormalisedQuery() + " (" + g.getOccurrenceCount() + " total misses)")
                .collect(Collectors.joining(", "));

        return "Week: " + weekStart + " to " + weekEnd
                + "\nTotal searches: " + totalSearches
                + "\nZero-result searches: " + zeroResultSearches
                + "\nTop queries: " + (topQueriesText.isBlank() ? "none" : topQueriesText)
                + "\nPersistent unmet demand (all-time top gaps): " + (gapsText.isBlank() ? "none" : gapsText);
    }
}
