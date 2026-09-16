package in.apnabazaar.preorder;

import in.apnabazaar.offering.DailyLineItem;
import in.apnabazaar.offering.DailyLineItemRepository;
import in.apnabazaar.offering.Offering;
import in.apnabazaar.ordering.OrderingWindowService;
import in.apnabazaar.ordering.WindowStatus;
import in.apnabazaar.provider.Provider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/** Creates reservations with a live ordering-window check, and recalculates provider ratings. */
@Service
public class ReservationService {

    private final DailyLineItemRepository dailyLineItemRepository;
    private final OrderingWindowService orderingWindowService;
    private final OrderSlotRepository orderSlotRepository;

    public ReservationService(DailyLineItemRepository dailyLineItemRepository,
                               OrderingWindowService orderingWindowService,
                               OrderSlotRepository orderSlotRepository) {
        this.dailyLineItemRepository = dailyLineItemRepository;
        this.orderingWindowService = orderingWindowService;
        this.orderSlotRepository = orderSlotRepository;
    }

    @Transactional
    public ReserveResponse reserve(UUID offeringId, ReserveRequest request) {
        DailyLineItem item = dailyLineItemRepository.findById(request.dailyLineItemId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Unknown listing"));

        if (!item.getOffering().getId().equals(offeringId)) {
            throw new ResponseStatusException(BAD_REQUEST, "That listing does not belong to this offering");
        }

        WindowStatus status = orderingWindowService.resolve(item, LocalDateTime.now());
        if (!status.actionable()) {
            throw new ResponseStatusException(CONFLICT,
                    "This offering is no longer accepting orders (" + status.label() + ")");
        }

        Offering offering = item.getOffering();
        Provider provider = offering.getProvider();
        OrderSlot slot = orderSlotRepository.saveAndFlush(new OrderSlot(offering, item, provider, request.sessionId()));
        return new ReserveResponse(slot.getId(), slot.getReservedAt());
    }

    @Transactional(readOnly = true)
    public List<PendingRatingView> getPendingRatings(UUID sessionId) {
        return orderSlotRepository.findPendingRatings(sessionId, LocalDate.now()).stream()
                .map(slot -> new PendingRatingView(slot.getId(), slot.getProvider().getShopName(),
                        slot.getDailyLineItem().getItemName(), slot.getDailyLineItem().getItemDate()))
                .toList();
    }

    @Transactional
    public void submitRating(UUID orderSlotId, RatingRequest request) {
        OrderSlot slot = orderSlotRepository.findById(orderSlotId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Unknown reservation"));
        if (slot.getRating() != null) {
            throw new ResponseStatusException(CONFLICT, "This reservation has already been rated");
        }

        slot.submitRating(request.rating(), request.reviewText());
        recalculateProviderRating(slot.getProvider());
    }

    private void recalculateProviderRating(Provider provider) {
        Double average = orderSlotRepository.averageRatingForProvider(provider);
        provider.updateRating(BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP));
    }
}
