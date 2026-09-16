package in.apnabazaar.preorder;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReserveResponse(UUID orderSlotId, OffsetDateTime reservedAt) {
}
