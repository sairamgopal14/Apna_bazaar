package in.apnabazaar.broadcast;

import in.apnabazaar.community.Community;
import in.apnabazaar.community.CommunityRepository;
import in.apnabazaar.gemini.GeminiNudgeClient;
import in.apnabazaar.provider.Provider;
import in.apnabazaar.provider.ProviderRepository;
import in.apnabazaar.search.ZeroResultLog;
import in.apnabazaar.search.ZeroResultLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/** Creates and reads {@link Broadcast} records. Delivery happens separately in {@link BroadcastScheduler}. */
@Service
public class BroadcastService {

    private final CommunityRepository communityRepository;
    private final ProviderRepository providerRepository;
    private final ZeroResultLogRepository zeroResultLogRepository;
    private final BroadcastRepository broadcastRepository;
    private final GeminiNudgeClient geminiNudgeClient;

    public BroadcastService(CommunityRepository communityRepository, ProviderRepository providerRepository,
                             ZeroResultLogRepository zeroResultLogRepository, BroadcastRepository broadcastRepository,
                             GeminiNudgeClient geminiNudgeClient) {
        this.communityRepository = communityRepository;
        this.providerRepository = providerRepository;
        this.zeroResultLogRepository = zeroResultLogRepository;
        this.broadcastRepository = broadcastRepository;
        this.geminiNudgeClient = geminiNudgeClient;
    }

    /** Lists real, existing demand gaps so the admin can pick one to base a nudge on. */
    public List<NudgeGapView> getNudgeGaps(String communitySlug) {
        Community community = findCommunity(communitySlug);
        return zeroResultLogRepository.findByCommunityOrderByOccurrenceCountDesc(community).stream()
                .map(gap -> new NudgeGapView(gap.getId(), gap.getNormalisedQuery(), gap.getOccurrenceCount()))
                .toList();
    }

    /** Asks Gemini to draft 4 options for one real gap. Nothing gets saved here — purely a proposal. */
    public NudgeSuggestionsResponse generateNudgeSuggestions(String communitySlug, UUID zeroResultLogId) {
        Community community = findCommunity(communitySlug);
        ZeroResultLog gap = zeroResultLogRepository.findById(zeroResultLogId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Unknown gap: " + zeroResultLogId));
        if (!gap.getCommunity().getId().equals(community.getId())) {
            throw new ResponseStatusException(BAD_REQUEST, "That gap doesn't belong to " + communitySlug);
        }

        List<String> options = geminiNudgeClient.generateNudgeOptions(gap.getNormalisedQuery(), gap.getOccurrenceCount());
        return new NudgeSuggestionsResponse(gap.getNormalisedQuery(), options);
    }

    /** Creates a real broadcast — organic, the admin's chosen AI-drafted text, or a paid sponsorship. */
    @Transactional
    public BroadcastResponse createBroadcast(CreateBroadcastRequest request) {
        Community community = findCommunity(request.communitySlug());

        Provider provider = null;
        if (request.broadcastType() == BroadcastType.sponsored) {
            if (request.providerId() == null || request.promoAmount() == null) {
                throw new ResponseStatusException(BAD_REQUEST,
                        "Sponsored broadcasts require both providerId and promoAmount");
            }
            provider = providerRepository.findById(request.providerId())
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Unknown provider: " + request.providerId()));
        }

        if (request.recurrencePattern() != null && request.scheduledAt() == null) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "Recurring broadcasts need scheduledAt — its time-of-day is used as the daily fire time");
        }

        BroadcastStatus status = resolveInitialStatus(request);

        Broadcast broadcast = new Broadcast(community, provider, request.message(), request.broadcastType(),
                status, request.scheduledAt(), request.recurrencePattern(), request.promoAmount());
        if (status == BroadcastStatus.sent) {
            broadcast.markSent();
        }

        return BroadcastResponse.from(broadcastRepository.save(broadcast));
    }

    private BroadcastStatus resolveInitialStatus(CreateBroadcastRequest request) {
        if (request.recurrencePattern() != null) {
            return BroadcastStatus.recurring_active;
        }
        if (request.scheduledAt() != null && request.scheduledAt().isAfter(OffsetDateTime.now())) {
            return BroadcastStatus.scheduled;
        }
        return BroadcastStatus.sent;
    }

    /** The admin's own manual report of what they observed after the fact. */
    @Transactional
    public BroadcastResponse logPerformance(UUID broadcastId, UpdatePerformanceRequest request) {
        Broadcast broadcast = broadcastRepository.findById(broadcastId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Unknown broadcast: " + broadcastId));
        broadcast.logPerformance(request.searchesTriggered(), request.whatsappTaps());
        return BroadcastResponse.from(broadcast);
    }

    /** Past broadcasts, newest first, with whatever performance the admin has logged for them. */
    public List<BroadcastResponse> getHistory(String communitySlug, int limit) {
        Community community = findCommunity(communitySlug);
        return broadcastRepository.findByCommunityOrderByCreatedAtDesc(community, PageRequest.of(0, limit)).stream()
                .map(BroadcastResponse::from)
                .toList();
    }

    private Community findCommunity(String communitySlug) {
        return communityRepository.findBySlug(communitySlug)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Unknown community: " + communitySlug));
    }
}
