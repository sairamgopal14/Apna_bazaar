package in.apnabazaar.ingestion;

import java.util.List;

public record IngestionResult(int sellersCreated, int sellersReused, int offeringsCreated,
                               int listingsCreated, int listingsUpdated, int sellersDeactivated,
                               List<String> rowErrors) {
}
