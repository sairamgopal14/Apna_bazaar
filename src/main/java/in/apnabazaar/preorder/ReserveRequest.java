package in.apnabazaar.preorder;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReserveRequest(@NotNull UUID dailyLineItemId, @NotNull UUID sessionId) {
}
