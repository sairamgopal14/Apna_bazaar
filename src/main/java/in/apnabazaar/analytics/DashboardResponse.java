package in.apnabazaar.analytics;

import in.apnabazaar.search.TopQueryView;

import java.time.LocalDate;
import java.util.List;

public record DashboardResponse(LocalDate date, long totalSearchesToday, long zeroResultQueriesToday,
                                 String peakSearchTime, long activeSellersToday, List<TopQueryView> topQueries,
                                 List<SellerClickThroughView> sellerClickThroughRates, String latestInsight,
                                 LocalDate insightWeekStart) {
}
