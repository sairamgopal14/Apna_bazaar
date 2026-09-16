package in.apnabazaar.broadcast;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * One request shape covers all three broadcast kinds — {@code providerId}/{@code promoAmount}
 * only apply to {@link BroadcastType#sponsored}; {@code scheduledAt}/{@code recurrencePattern}
 * are both optional (leave both blank to send immediately).
 */
public record CreateBroadcastRequest(@NotBlank String communitySlug, @NotBlank String message,
                                      @NotNull BroadcastType broadcastType, UUID providerId, Integer promoAmount,
                                      OffsetDateTime scheduledAt, RecurrencePattern recurrencePattern) {
}
