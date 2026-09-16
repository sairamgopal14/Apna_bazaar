package in.apnabazaar.broadcast;

import java.util.List;

/** Gemini's 4 drafted options — nothing here is saved yet; the admin picks one to actually create it. */
public record NudgeSuggestionsResponse(String normalisedQuery, List<String> options) {
}
