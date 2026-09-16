package in.apnabazaar.search;

import java.util.List;

public record SearchResponse(String query, int resultCount, List<SearchResultCard> results) {
}
