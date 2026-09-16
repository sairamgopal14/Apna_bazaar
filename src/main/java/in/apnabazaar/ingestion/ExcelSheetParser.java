package in.apnabazaar.ingestion;

import in.apnabazaar.offering.DeliveryType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the admin's daily sheet into plain Java data. Expected column order, one header
 * row then one row per item: Seller Name | Flat Number | WhatsApp Number | Item Name |
 * Description | Price | Delivery Type | Serves From | Serves To | Preorder Closes At |
 * Preorder Day Offset | For Date | Category. Every column after Price may be left blank —
 * timing falls back to {@link in.apnabazaar.offering.OfferingSchedule} then a hardcoded
 * default; a blank For Date means "today." For Date also accepts the plain words
 * "today"/"tomorrow" instead of a literal date, since a day-before seller's batter is
 * always for "tomorrow" relative to whenever the admin happens to upload. Category is
 * free text — a brand-new word here becomes a brand-new category, no code change needed.
 */
@Component
class ExcelSheetParser {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm");

    ParsedSheet parse(MultipartFile file) throws IOException {
        List<ExcelRowData> rows = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (InputStream in = file.getInputStream(); XSSFWorkbook workbook = new XSSFWorkbook(in)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isBlank(cell(row, 0, formatter))) {
                    continue;
                }

                int spreadsheetRowNumber = rowIndex + 1;
                try {
                    rows.add(parseRow(row, formatter));
                } catch (Exception e) {
                    errors.add("Row " + spreadsheetRowNumber + ": " + e.getMessage());
                }
            }
        }

        return new ParsedSheet(rows, errors);
    }

    private ExcelRowData parseRow(Row row, DataFormatter formatter) {
        return new ExcelRowData(
                require(cell(row, 0, formatter), "Seller Name"),
                require(cell(row, 1, formatter), "Flat Number"),
                require(cell(row, 2, formatter), "WhatsApp Number"),
                require(cell(row, 3, formatter), "Item Name"),
                cell(row, 4, formatter),
                parsePrice(require(cell(row, 5, formatter), "Price")),
                parseDeliveryType(cell(row, 6, formatter)),
                parseTime(cell(row, 7, formatter)),
                parseTime(cell(row, 8, formatter)),
                parseTime(cell(row, 9, formatter)),
                parseDayOffset(cell(row, 10, formatter)),
                parseForDate(cell(row, 11, formatter)),
                blankToNull(cell(row, 12, formatter)));
    }

    private String blankToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private String cell(Row row, int index, DataFormatter formatter) {
        var c = row.getCell(index);
        return c == null ? "" : formatter.formatCellValue(c).trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String require(String value, String columnName) {
        if (isBlank(value)) {
            throw new IllegalArgumentException(columnName + " is required");
        }
        return value;
    }

    private BigDecimal parsePrice(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Price '" + value + "' is not a number");
        }
    }

    private DeliveryType parseDeliveryType(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return DeliveryType.valueOf(value.trim().toLowerCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Delivery Type '" + value + "' must be pickup, home_delivery, or both");
        }
    }

    private LocalTime parseTime(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return LocalTime.parse(value.trim(), TIME_FORMAT);
        } catch (Exception e) {
            throw new IllegalArgumentException("Time '" + value + "' must look like 5:00 or 17:30");
        }
    }

    private Short parseDayOffset(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return Short.valueOf(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Preorder Day Offset '" + value + "' must be 0 or -1");
        }
    }

    private LocalDate parseForDate(String value) {
        if (isBlank(value)) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.equalsIgnoreCase("today")) {
            return null;
        }
        if (trimmed.equalsIgnoreCase("tomorrow")) {
            return LocalDate.now().plusDays(1);
        }
        try {
            return LocalDate.parse(trimmed);
        } catch (Exception e) {
            throw new IllegalArgumentException("For Date '" + value + "' must be 'today', 'tomorrow', or yyyy-MM-dd");
        }
    }
}
