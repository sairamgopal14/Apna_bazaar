package in.apnabazaar.analytics;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/admin/dashboard")
    public DashboardResponse getDashboard(@RequestParam String communitySlug) {
        return analyticsService.getDashboard(communitySlug);
    }

    @PostMapping("/admin/dashboard/insights/generate")
    public InsightResponse generateInsightNow(@RequestParam String communitySlug) {
        return analyticsService.generateInsightNow(communitySlug);
    }
}
