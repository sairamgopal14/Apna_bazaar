package in.apnabazaar.search;

/** Lowercases, trims, and collapses whitespace so the same intent dedupes to one row. */
final class QueryNormalizer {

    private QueryNormalizer() {
    }

    static String normalise(String rawQuery) {
        return rawQuery.trim().toLowerCase().replaceAll("\\s+", " ");
    }
}
