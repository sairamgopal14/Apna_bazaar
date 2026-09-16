package in.apnabazaar.analytics;

import java.time.LocalDate;

public record InsightResponse(String communitySlug, LocalDate weekStart, LocalDate weekEnd, String insightText) {
}
