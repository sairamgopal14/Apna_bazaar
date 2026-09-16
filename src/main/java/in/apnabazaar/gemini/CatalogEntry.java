package in.apnabazaar.gemini;

import java.util.UUID;

/** One offering, flattened for Gemini to match against a buyer's query. */
public record CatalogEntry(UUID offeringId, String shopName, String offeringName, String description,
                            String offeringType, String categoryName) {
}
