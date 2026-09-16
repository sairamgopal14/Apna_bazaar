package in.apnabazaar.ordering;

/**
 * @param label      plain-English status shown on the buyer's seller card, e.g.
 *                   "Pre-order open — closes 9:00 AM today"
 * @param actionable whether this listing is still worth showing to a buyer right now
 *                   (mid-serving, pre-order window open, or always-available) —
 *                   false once both the serving and pre-order windows have lapsed
 */
public record WindowStatus(String label, boolean actionable) {
}
