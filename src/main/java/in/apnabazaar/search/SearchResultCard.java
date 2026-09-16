package in.apnabazaar.search;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A matched seller, as shown on a buyer's result card.
 * {@link #dailyLineItemId} lets the buyer reserve exactly the listing they were shown,
 * instead of the server re-resolving "today's best match" a second time at reserve
 * time. {@link #rating} is null for any seller with no real order_slot ratings yet
 * Phase 5  it is never guessed or defaulted to zero.
 */
public record SearchResultCard(UUID providerId, String shopName, String flatNumber, String whatsappNumber,
                                UUID offeringId, String offeringName, String description,
                                BigDecimal price, String deliveryType, String statusLabel,
                                UUID dailyLineItemId, BigDecimal rating) {
}
