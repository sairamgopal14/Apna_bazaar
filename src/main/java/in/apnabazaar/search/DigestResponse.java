package in.apnabazaar.search;

import java.time.LocalDate;
import java.util.List;

/** Every active seller's listing for today. */
public record DigestResponse(LocalDate date, int resultCount, List<SearchResultCard> results) {
}
