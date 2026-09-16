package in.apnabazaar.analytics;

import java.util.UUID;

public record SellerClickThroughView(UUID providerId, String shopName, long impressions, long clicks,
                                      double clickThroughRate) {
}
