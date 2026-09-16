package in.apnabazaar.ingestion;

import java.util.List;

/** Rows that parsed cleanly, plus a human-readable note for every row that didn't. */
record ParsedSheet(List<ExcelRowData> rows, List<String> rowErrors) {
}
