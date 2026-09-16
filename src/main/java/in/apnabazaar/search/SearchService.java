package in.apnabazaar.search;

import in.apnabazaar.community.Community;
import in.apnabazaar.community.CommunityRepository;
import in.apnabazaar.offering.DailyLineItem;
import in.apnabazaar.offering.DailyLineItemRepository;
import in.apnabazaar.offering.Offering;
import in.apnabazaar.offering.OfferingRepository;
import in.apnabazaar.gemini.CatalogEntry;
import in.apnabazaar.gemini.GeminiSearchClient;
import in.apnabazaar.ordering.OrderingWindowService;
import in.apnabazaar.ordering.WindowStatus;
import in.apnabazaar.provider.Provider;
import in.apnabazaar.provider.ProviderRepository;
import in.apnabazaar.provider.ProviderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class SearchService {

    private final CommunityRepository communityRepository;
    private final ProviderRepository providerRepository;
    private final OfferingRepository offeringRepository;
    private final DailyLineItemRepository dailyLineItemRepository;
    private final GeminiSearchClient geminiSearchClient;
    private final OrderingWindowService orderingWindowService;
    private final SearchEventRepository searchEventRepository;
    private final ZeroResultLogRepository zeroResultLogRepository;
    private final OfferingImpressionRepository offeringImpressionRepository;
    private final OfferingClickRepository offeringClickRepository;

    public SearchService(CommunityRepository communityRepository, ProviderRepository providerRepository,
                          OfferingRepository offeringRepository, DailyLineItemRepository dailyLineItemRepository,
                          GeminiSearchClient geminiSearchClient, OrderingWindowService orderingWindowService,
                          SearchEventRepository searchEventRepository,
                          ZeroResultLogRepository zeroResultLogRepository,
                          OfferingImpressionRepository offeringImpressionRepository,
                          OfferingClickRepository offeringClickRepository) {
        this.communityRepository = communityRepository;
        this.providerRepository = providerRepository;
        this.offeringRepository = offeringRepository;
        this.dailyLineItemRepository = dailyLineItemRepository;
        this.geminiSearchClient = geminiSearchClient;
        this.orderingWindowService = orderingWindowService;
        this.searchEventRepository = searchEventRepository;
        this.zeroResultLogRepository = zeroResultLogRepository;
        this.offeringImpressionRepository = offeringImpressionRepository;
        this.offeringClickRepository = offeringClickRepository;
    }

    @Transactional
    public SearchResponse search(SearchRequest request) {
        Community community = communityRepository.findBySlug(request.communitySlug())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Unknown community: " + request.communitySlug()));

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        List<Provider> activeProviders = providerRepository.findByCommunityAndStatus(community, ProviderStatus.active);
        Map<UUID, Offering> offeringsById = activeProviders.stream()
                .flatMap(provider -> offeringRepository.findByProvider(provider).stream())
                .filter(Offering::isAvailable)
                .collect(Collectors.toMap(Offering::getId, o -> o, (a, b) -> a, LinkedHashMap::new));

        List<CatalogEntry> catalog = offeringsById.values().stream()
                .map(o -> new CatalogEntry(o.getId(), o.getProvider().getShopName(), o.getName(),
                        o.getDescription(), o.getOfferingType().name(),
                        o.getCategory() != null ? o.getCategory().getName() : null))
                .toList();

        List<UUID> matchedOfferingIds = geminiSearchClient.matchOfferings(request.query(), catalog);

        Map<UUID, List<DailyLineItem>> lineItemsByOffering = dailyLineItemRepository
                .findByOfferingIdInAndItemDate(matchedOfferingIds, today).stream()
                .collect(Collectors.groupingBy(li -> li.getOffering().getId()));
        dailyLineItemRepository.findByOfferingIdInAndItemDate(matchedOfferingIds, today.plusDays(1))
                .forEach(li -> lineItemsByOffering
                        .computeIfAbsent(li.getOffering().getId(), k -> new ArrayList<>())
                        .add(li));

        List<SearchResultCard> results = new ArrayList<>();
        for (UUID offeringId : matchedOfferingIds) {
            Offering offering = offeringsById.get(offeringId);
            List<DailyLineItem> candidates = lineItemsByOffering.getOrDefault(offeringId, List.of());
            pickBestLineItem(candidates, now).ifPresent(pick -> {
                results.add(toCard(offering, pick.item(), pick.status()));
                offeringImpressionRepository.save(
                        new OfferingImpression(community, offering.getProvider(), offering, today));
            });
        }

        logSearchEvent(community, request, now, results.size());

        return new SearchResponse(request.query(), results.size(), results); // returns here
    }

    /**
     * Every active seller's available listing for today -- no query, no Gemini call, just
     * "who's open right now." Shown the instant the chatbot loads, like a morning digest.
     * Deliberately doesn't log a SearchEvent or an impression -- this isn't a search.
     */
    @Transactional(readOnly = true)
    public DigestResponse getDigest(String communitySlug) {
        Community community = communityRepository.findBySlug(communitySlug)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Unknown community: " + communitySlug));

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        List<Provider> activeProviders = providerRepository.findByCommunityAndStatus(community, ProviderStatus.active);
        Map<UUID, Offering> offeringsById = activeProviders.stream()
                .flatMap(provider -> offeringRepository.findByProvider(provider).stream())
                .filter(Offering::isAvailable)
                .collect(Collectors.toMap(Offering::getId, o -> o, (a, b) -> a, LinkedHashMap::new));

        List<UUID> offeringIds = new ArrayList<>(offeringsById.keySet());

        Map<UUID, List<DailyLineItem>> lineItemsByOffering = dailyLineItemRepository
                .findByOfferingIdInAndItemDate(offeringIds, today).stream()
                .collect(Collectors.groupingBy(li -> li.getOffering().getId()));
        dailyLineItemRepository.findByOfferingIdInAndItemDate(offeringIds, today.plusDays(1))
                .forEach(li -> lineItemsByOffering
                        .computeIfAbsent(li.getOffering().getId(), k -> new ArrayList<>())
                        .add(li));

        List<SearchResultCard> results = new ArrayList<>();
        for (UUID offeringId : offeringIds) {
            Offering offering = offeringsById.get(offeringId);
            List<DailyLineItem> candidates = lineItemsByOffering.getOrDefault(offeringId, List.of());
            pickBestLineItem(candidates, now).ifPresent(pick -> results.add(toCard(offering, pick.item(), pick.status())));
        }

        return new DigestResponse(today, results.size(), results);
    }

    /** A buyer tapped "Order on WhatsApp" for this offering — the other half of click-through rate. */
    @Transactional
    public void recordClick(UUID offeringId) {
        Offering offering = offeringRepository.findById(offeringId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Unknown offering: " + offeringId));
        Provider provider = offering.getProvider();
        offeringClickRepository.save(
                new OfferingClick(provider.getCommunity(), provider, offering, LocalDate.now()));
    }

    private record Pick(DailyLineItem item, WindowStatus status) {
    }

    private Optional<Pick> pickBestLineItem(List<DailyLineItem> candidates, LocalDateTime now) {
        return candidates.stream()
                .map(item -> new Pick(item, orderingWindowService.resolve(item, now)))
                .filter(pick -> pick.status().actionable())
                .min(Comparator.comparing(pick -> pick.item().getItemDate()));
    }

    private SearchResultCard toCard(Offering offering, DailyLineItem lineItem, WindowStatus status) {
        Provider provider = offering.getProvider();
        return new SearchResultCard(
                provider.getId(), provider.getShopName(), provider.getFlatNumber(), provider.getWhatsappNumber(),
                offering.getId(), offering.getName(), offering.getDescription(),
                lineItem.getPrice(), lineItem.getDeliveryType().name(), status.label(),
                lineItem.getId(), provider.getRating(),
                offering.getCategory() != null ? offering.getCategory().getName() : null);
    }

    private void logSearchEvent(Community community, SearchRequest request, LocalDateTime now, int resultCount) {
        String normalised = QueryNormalizer.normalise(request.query());
        boolean hadResults = resultCount > 0;
        short dayOfWeek = (short) now.getDayOfWeek().getValue();

        searchEventRepository.save(new SearchEvent(
                community, request.query(), normalised, resultCount, hadResults, now.toLocalDate(),
                TimeBucket.forTime(now.toLocalTime()), dayOfWeek, request.sessionId()));

        if (!hadResults) {
            recordZeroResult(community, normalised, now.toLocalDate());
        }
    }

    private void recordZeroResult(Community community, String normalisedQuery, LocalDate today) {
        zeroResultLogRepository.findByCommunityAndNormalisedQuery(community, normalisedQuery)
                .ifPresentOrElse(
                        existing -> existing.recordAnotherOccurrence(today),
                        () -> zeroResultLogRepository.save(new ZeroResultLog(community, normalisedQuery, today)));
    }
}
