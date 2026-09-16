package in.apnabazaar.broadcast;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BroadcastResponse(UUID id, String message, BroadcastType broadcastType, BroadcastStatus status,
                                 OffsetDateTime scheduledAt, RecurrencePattern recurrencePattern,
                                 OffsetDateTime lastSentAt, Integer promoAmount, int searchesTriggered,
                                 int whatsappTaps) {

    static BroadcastResponse from(Broadcast broadcast) {
        return new BroadcastResponse(broadcast.getId(), broadcast.getMessage(), broadcast.getBroadcastType(),
                broadcast.getStatus(), broadcast.getScheduledAt(), broadcast.getRecurrencePattern(),
                broadcast.getLastSentAt(), broadcast.getPromoAmount(), broadcast.getSearchesTriggered(),
                broadcast.getWhatsappTaps());
    }
}
