package in.apnabazaar.gemini;

import java.util.UUID;

/** One offering, flattened for the LLM to read as it matches a buyer's query against the catalog. */
//a passport-photo crop of a full Offering

public record CatalogEntry(UUID offeringId, String shopName, String offeringName, String description,
                            String offeringType, String categoryName) {
}
