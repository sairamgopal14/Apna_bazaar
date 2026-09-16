package in.apnabazaar.preorder;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/search/{offeringId}/reserve")
    public ReserveResponse reserve(@PathVariable UUID offeringId, @Valid @RequestBody ReserveRequest request) {
        return reservationService.reserve(offeringId, request);
    }

    @GetMapping("/search/pending-ratings")
    public List<PendingRatingView> getPendingRatings(@RequestParam UUID sessionId) {
        return reservationService.getPendingRatings(sessionId);
    }

    @PostMapping("/order-slots/{orderSlotId}/rating")
    public void submitRating(@PathVariable UUID orderSlotId, @Valid @RequestBody RatingRequest request) {
        reservationService.submitRating(orderSlotId, request);
    }
}
