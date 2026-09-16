package in.apnabazaar.search;

import java.math.BigDecimal;
import java.util.UUID;

/** A matched seller, as shown on a buyer's result card. {@link #rating} is null until the seller has a real rating. */
public record SearchResultCard(UUID providerId, String shopName, String flatNumber, String whatsappNumber,
                                UUID offeringId, String offeringName, String description,
                                BigDecimal price, String deliveryType, String statusLabel,
                                UUID dailyLineItemId, BigDecimal rating, String categoryName) {
}
