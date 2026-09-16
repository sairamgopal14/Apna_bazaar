package in.apnabazaar.ingestion;

import in.apnabazaar.offering.DeliveryType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * One parsed row of the admin's daily sheet. Timing fields are nullable on purpose —
 * a blank cell means "unchanged from usual," resolved later against {@link in.apnabazaar.offering.OfferingSchedule}.
 * {@code forDate} is nullable too — blank means "today," but a day-before seller's
 * batter for tomorrow morning genuinely needs to be posted with tomorrow's date.
 */
record ExcelRowData(String sellerName, String flatNumber, String whatsappNumber, String itemName,
                     String description, BigDecimal price, DeliveryType deliveryType,
                     LocalTime servesFrom, LocalTime servesTo, LocalTime preorderClosesAt,
                     Short preorderDayOffset, LocalDate forDate) {
}
