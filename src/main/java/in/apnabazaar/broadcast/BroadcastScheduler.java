package in.apnabazaar.broadcast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

/** Checks once a minute for scheduled or recurring broadcasts that are due, and delivers them. */
@Component
public class BroadcastScheduler {

    private static final Logger log = LoggerFactory.getLogger(BroadcastScheduler.class);

    private final BroadcastRepository broadcastRepository;

    public BroadcastScheduler(BroadcastRepository broadcastRepository) {
        this.broadcastRepository = broadcastRepository;
    }

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void deliverDueBroadcasts() {
        OffsetDateTime now = OffsetDateTime.now();
        int sent = deliverOneTimeBroadcasts(now);
        int fired = deliverRecurringBroadcasts(now);
        if (sent > 0 || fired > 0) {
            log.info("BroadcastScheduler: sent {} one-time, fired {} recurring broadcast(s)", sent, fired);
        }
    }

    private int deliverOneTimeBroadcasts(OffsetDateTime now) {
        List<Broadcast> due = broadcastRepository.findByStatus(BroadcastStatus.scheduled).stream()
                .filter(b -> !b.getScheduledAt().isAfter(now))
                .toList();
        due.forEach(Broadcast::markSent);
        return due.size();
    }

    private int deliverRecurringBroadcasts(OffsetDateTime now) {
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();

        List<Broadcast> due = broadcastRepository.findByStatus(BroadcastStatus.recurring_active).stream()
                .filter(b -> isDueToday(b, today, currentTime))
                .toList();
        due.forEach(Broadcast::recordRecurringFire);
        return due.size();
    }

    private boolean isDueToday(Broadcast broadcast, LocalDate today, LocalTime currentTime) {
        boolean alreadyFiredToday = broadcast.getLastSentAt() != null
                && broadcast.getLastSentAt().toLocalDate().isEqual(today);
        if (alreadyFiredToday) {
            return false;
        }

        LocalTime fireTime = broadcast.getScheduledAt().toLocalTime();
        if (currentTime.isBefore(fireTime)) {
            return false;
        }

        return switch (broadcast.getRecurrencePattern()) {
            case daily -> true;
            case weekdays -> today.getDayOfWeek() != DayOfWeek.SATURDAY && today.getDayOfWeek() != DayOfWeek.SUNDAY;
            case weekly -> today.getDayOfWeek() == broadcast.getScheduledAt().getDayOfWeek();
        };
    }
}
