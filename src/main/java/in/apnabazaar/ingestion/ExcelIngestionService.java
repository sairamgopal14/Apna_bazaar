package in.apnabazaar.ingestion;

import in.apnabazaar.community.Community;
import in.apnabazaar.community.CommunityRepository;
import in.apnabazaar.offering.Category;
import in.apnabazaar.offering.CategoryRepository;
import in.apnabazaar.offering.DailyLineItem;
import in.apnabazaar.offering.DailyLineItemRepository;
import in.apnabazaar.offering.DeliveryType;
import in.apnabazaar.offering.Offering;
import in.apnabazaar.offering.OfferingRepository;
import in.apnabazaar.offering.OfferingSchedule;
import in.apnabazaar.offering.OfferingScheduleRepository;
import in.apnabazaar.offering.OfferingType;
import in.apnabazaar.provider.Provider;
import in.apnabazaar.provider.ProviderRepository;
import in.apnabazaar.provider.ProviderStatus;
import in.apnabazaar.provider.ProviderType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Turns the admin's daily sheet into providers, offerings, and today's listings.
 * New sellers get {@code name} and {@code shopName} both set from the sheet's single
 * "Seller Name" column, and default to {@link ProviderType#food_seller}, since the
 * sheet has no separate display-name or provider-type column.
 */
@Service
public class ExcelIngestionService {

    private final CommunityRepository communityRepository;
    private final ProviderRepository providerRepository;
    private final OfferingRepository offeringRepository;
    private final OfferingScheduleRepository offeringScheduleRepository;
    private final CategoryRepository categoryRepository;
    private final DailyPostRepository dailyPostRepository;
    private final DailyLineItemRepository dailyLineItemRepository;
    private final ExcelSheetParser excelSheetParser;

    public ExcelIngestionService(CommunityRepository communityRepository, ProviderRepository providerRepository,
                                  OfferingRepository offeringRepository,
                                  OfferingScheduleRepository offeringScheduleRepository,
                                  CategoryRepository categoryRepository,
                                  DailyPostRepository dailyPostRepository,
                                  DailyLineItemRepository dailyLineItemRepository,
                                  ExcelSheetParser excelSheetParser) {
        this.communityRepository = communityRepository;
        this.providerRepository = providerRepository;
        this.offeringRepository = offeringRepository;
        this.offeringScheduleRepository = offeringScheduleRepository;
        this.categoryRepository = categoryRepository;
        this.dailyPostRepository = dailyPostRepository;
        this.dailyLineItemRepository = dailyLineItemRepository;
        this.excelSheetParser = excelSheetParser;
    }

    @Transactional
    public IngestionResult ingest(MultipartFile file, String communitySlug) throws IOException {
        Community community = communityRepository.findBySlug(communitySlug)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Unknown community: " + communitySlug));

        ParsedSheet sheet = excelSheetParser.parse(file);
        LocalDate uploadDate = LocalDate.now();

        int sellersCreated = 0;
        int sellersReused = 0;
        int offeringsCreated = 0;
        int listingsCreated = 0;
        int listingsUpdated = 0;
        Set<UUID> touchedProviderIds = new HashSet<>();

        for (ExcelRowData row : sheet.rows()) {
            Optional<Provider> existingProvider =
                    providerRepository.findByCommunityAndWhatsappNumber(community, row.whatsappNumber());

            Provider provider;
            if (existingProvider.isPresent()) {
                provider = existingProvider.get();
                if (provider.getStatus() != ProviderStatus.active) {
                    provider.reactivate();
                }
                sellersReused++;
            } else {
                provider = providerRepository.save(new Provider(community, row.sellerName(), row.sellerName(),
                        row.flatNumber(), row.whatsappNumber(), ProviderType.food_seller));
                sellersCreated++;
            }
            touchedProviderIds.add(provider.getId());

            Optional<Offering> existingOffering = offeringRepository.findByProviderAndName(provider, row.itemName());
            Offering offering;
            if (existingOffering.isPresent()) {
                offering = existingOffering.get();
            } else {
                String description = row.description() == null || row.description().isBlank()
                        ? row.itemName()
                        : row.description();
                offering = offeringRepository.save(
                        new Offering(provider, row.itemName(), description, row.price(), OfferingType.food_item));
                offeringsCreated++;
            }

            if (row.categoryName() != null) {
                Category category = categoryRepository.findByNameIgnoreCase(row.categoryName())
                        .orElseGet(() -> categoryRepository.save(new Category(row.categoryName())));
                offering.assignCategory(category);
            }

            LocalDate itemDate = row.forDate() != null ? row.forDate() : uploadDate;

            DailyPost dailyPost = dailyPostRepository.findByProviderAndPostDate(provider, itemDate)
                    .orElseGet(() -> dailyPostRepository.save(new DailyPost(provider, itemDate, PostSource.excel_upload)));

            ResolvedWindow window = resolveWindow(offering, row);

            Optional<DailyLineItem> existingLineItem =
                    dailyLineItemRepository.findByOfferingIdAndItemDate(offering.getId(), itemDate);
            if (existingLineItem.isPresent()) {
                existingLineItem.get().applyDailyUpdate(dailyPost, row.itemName(), row.price(), window.deliveryType(),
                        window.servesFrom(), window.servesTo(), window.acceptsRealtime(),
                        window.realtimeCutoffMinutes(), window.preorderRequired(), window.preorderClosesAt(),
                        window.preorderDayOffset());
                listingsUpdated++;
            } else {
                dailyLineItemRepository.save(new DailyLineItem(offering, dailyPost, itemDate, row.itemName(),
                        row.price(), window.deliveryType(), window.servesFrom(), window.servesTo(),
                        window.acceptsRealtime(), window.realtimeCutoffMinutes(), window.preorderRequired(),
                        window.preorderClosesAt(), window.preorderDayOffset()));
                listingsCreated++;
            }
        }

        int sellersDeactivated = deactivateAbsentSellers(community, touchedProviderIds);

        return new IngestionResult(sellersCreated, sellersReused, offeringsCreated, listingsCreated,
                listingsUpdated, sellersDeactivated, sheet.rowErrors());
    }

    private record ResolvedWindow(DeliveryType deliveryType, LocalTime servesFrom, LocalTime servesTo,
                                   boolean acceptsRealtime, Integer realtimeCutoffMinutes, boolean preorderRequired,
                                   LocalTime preorderClosesAt, short preorderDayOffset) {
    }

    /**
     * Row value wins if given; otherwise falls back to the offering's recurring schedule;
     * otherwise a hardcoded default. Whatever the row explicitly states becomes the new
     * schedule going forward, so tomorrow's blank cells fall back to today's stated values.
     */
    private ResolvedWindow resolveWindow(Offering offering, ExcelRowData row) {
        Optional<OfferingSchedule> existingSchedule = offeringScheduleRepository.findByOfferingId(offering.getId());

        DeliveryType deliveryType = row.deliveryType() != null ? row.deliveryType()
                : existingSchedule.map(OfferingSchedule::getDeliveryType).orElse(DeliveryType.pickup);
        LocalTime servesFrom = row.servesFrom() != null ? row.servesFrom()
                : existingSchedule.map(OfferingSchedule::getServesFrom).orElse(null);
        LocalTime servesTo = row.servesTo() != null ? row.servesTo()
                : existingSchedule.map(OfferingSchedule::getServesTo).orElse(null);
        LocalTime preorderClosesAt = row.preorderClosesAt() != null ? row.preorderClosesAt()
                : existingSchedule.map(OfferingSchedule::getPreorderClosesAt).orElse(null);
        short preorderDayOffset = row.preorderDayOffset() != null ? row.preorderDayOffset()
                : existingSchedule.map(OfferingSchedule::getPreorderDayOffset).orElse((short) 0);
        boolean preorderRequired = preorderClosesAt != null;

        // Not exposed as sheet columns yet — always inherited from the schedule, or defaulted.
        boolean acceptsRealtime = existingSchedule.map(OfferingSchedule::isAcceptsRealtime).orElse(true);
        Integer realtimeCutoffMinutes = existingSchedule.map(OfferingSchedule::getRealtimeCutoffMinutes).orElse(null);

        boolean rowStatedSomething = row.deliveryType() != null || row.servesFrom() != null
                || row.servesTo() != null || row.preorderClosesAt() != null || row.preorderDayOffset() != null;
        if (rowStatedSomething) {
            if (existingSchedule.isPresent()) {
                existingSchedule.get().applyUpdate(deliveryType, servesFrom, servesTo, preorderRequired,
                        preorderClosesAt, preorderDayOffset);
            } else {
                offeringScheduleRepository.save(new OfferingSchedule(offering, deliveryType, servesFrom, servesTo,
                        acceptsRealtime, realtimeCutoffMinutes, preorderRequired, preorderClosesAt,
                        preorderDayOffset));
            }
        }

        return new ResolvedWindow(deliveryType, servesFrom, servesTo, acceptsRealtime, realtimeCutoffMinutes,
                preorderRequired, preorderClosesAt, preorderDayOffset);
    }

    /** A seller who was active but didn't appear anywhere in today's sheet stops counting as active. */
    private int deactivateAbsentSellers(Community community, Set<UUID> touchedProviderIds) {
        List<Provider> activeProviders = providerRepository.findByCommunityAndStatus(community, ProviderStatus.active);
        int deactivated = 0;
        for (Provider provider : activeProviders) {
            if (!touchedProviderIds.contains(provider.getId())) {
                provider.deactivate();
                deactivated++;
            }
        }
        return deactivated;
    }
}
