package in.apnabazaar.ordering;

import in.apnabazaar.offering.DailyLineItem;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Resolves a {@link DailyLineItem}'s three independent time windows (serving,
 * pre-order, realtime) into the single plain-English status line shown on each
 * seller card, e.g. "Pre-order open — closes 9:00 AM today" or
 * "Serving now · Realtime orders open · 22 mins left!".
 */
@Service
public class OrderingWindowService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a");
    private static final int URGENT_MINUTES_THRESHOLD = 30;

    public WindowStatus resolve(DailyLineItem item, LocalDateTime now) {
        if (!item.isAvailable()) {
            return new WindowStatus("Not available today", false);
        }

        boolean alwaysAvailable = item.getServesFrom() == null && item.getServesTo() == null;
        if (alwaysAvailable) {
            return new WindowStatus("Available now", true);
        }

        LocalDateTime servingStart = LocalDateTime.of(item.getItemDate(), item.getServesFrom());
        LocalDateTime servingEnd = LocalDateTime.of(item.getItemDate(), item.getServesTo());

        if (!now.isBefore(servingStart) && now.isBefore(servingEnd)) {
            return currentlyServing(item, now, servingEnd);
        }
        if (now.isBefore(servingStart)) { // not yet started
            return beforeServing(item, now, servingStart);
        }
        return new WindowStatus("Closed for today", false);
    }

    private WindowStatus currentlyServing(DailyLineItem item, LocalDateTime now, LocalDateTime servingEnd) {
        if (!item.isAcceptsRealtime()) {
            return new WindowStatus("Serving now", true);
        }

        LocalDateTime realtimeCutoff = item.getRealtimeCutoffMinutes() == null
                ? servingEnd
                : servingEnd.minusMinutes(item.getRealtimeCutoffMinutes());

        if (!now.isBefore(realtimeCutoff)) {
            return new  WindowStatus("Serving now · Realtime orders closed", true);
        }

        long minutesLeft = Duration.between(now, realtimeCutoff).toMinutes();
        if (minutesLeft <= URGENT_MINUTES_THRESHOLD) {
            return new WindowStatus("Serving now · Realtime orders open · " + minutesLeft + " mins left!", true);
        }
        return new WindowStatus("Serving now · Realtime orders open", true);
    }

    private WindowStatus beforeServing(DailyLineItem item, LocalDateTime now, LocalDateTime servingStart) {
        if (!item.isPreorderRequired() || item.getPreorderClosesAt() == null) {
            return new WindowStatus("Opens " + TIME_FORMAT.format(item.getServesFrom()) + " today", true);
        }

        LocalDate cutoffDate = item.getItemDate().plusDays(item.getPreorderDayOffset());
        LocalDateTime preorderCutoff = LocalDateTime.of(cutoffDate, item.getPreorderClosesAt());

        if (now.isBefore(preorderCutoff)) {
            String dayLabel = dayLabelFor(cutoffDate, now.toLocalDate(), item.getPreorderClosesAt());
            return new WindowStatus(
                    "Pre-order open — closes " + TIME_FORMAT.format(item.getPreorderClosesAt()) + " " + dayLabel,
                    true);
        }

        if (item.isAcceptsRealtime()) {
            return new WindowStatus(
                    "Pre-order closed — opens " + TIME_FORMAT.format(servingStart.toLocalTime()) + " (walk-in only)",
                    true);
        }
        return new WindowStatus("Pre-order closed for today", false);
    }

    private String dayLabelFor(LocalDate targetDate, LocalDate today, java.time.LocalTime closesAt) {
        if (!targetDate.isEqual(today)) {
            return "today";
        }
        return closesAt.isAfter(java.time.LocalTime.of(17, 0)) ? "tonight" : "today";
    }
}
