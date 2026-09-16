package in.apnabazaar.preorder;

import java.time.LocalDate;
import java.util.UUID;

/** One past reservation that hasn't been rated yet -- shown to a returning buyer as a soft prompt. */
public record PendingRatingView(UUID orderSlotId, String shopName, String itemName, LocalDate itemDate) {
}
