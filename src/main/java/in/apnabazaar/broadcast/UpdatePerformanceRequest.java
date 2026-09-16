package in.apnabazaar.broadcast;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** The admin's own manual report of what they observed after a broadcast went out. */
public record UpdatePerformanceRequest(@NotNull @Min(0) Integer searchesTriggered,
                                        @NotNull @Min(0) Integer whatsappTaps) {
}
