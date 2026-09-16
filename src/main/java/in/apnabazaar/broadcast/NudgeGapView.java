package in.apnabazaar.broadcast;

import java.util.UUID;

/** One row from {@code zero_result_log}, shown to the admin so they can pick a gap to nudge about. */
public record NudgeGapView(UUID zeroResultLogId, String normalisedQuery, int occurrenceCount) {
}
