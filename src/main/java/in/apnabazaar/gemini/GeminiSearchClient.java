package in.apnabazaar.gemini;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Sends the buyer's query plus the community's active catalog to Gemini and asks it to
 * pick which offerings genuinely match by meaning, not keyword overlap — "idli" should
 * surface "South Indian breakfast tiffin" even though the words never overlap.
 */
@Component
public class GeminiSearchClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiSearchClient.class);
    private static final Pattern JSON_ARRAY = Pattern.compile("\\[[^\\[\\]]*]", Pattern.DOTALL);

    private static final String SYSTEM_PROMPT = """
            You are the search matcher for Apna Bazaar, a hyperlocal marketplace inside a gated
            residential community. You are given a buyer's query and today's catalog of active
            sellers as a JSON array, each entry with an offeringId, shopName, offeringName,
            description, and offeringType.

            Return ONLY a JSON array of the offeringId values that genuinely satisfy the buyer's
            query, ordered from most to least relevant. Match by meaning and intent, not just
            keyword overlap — e.g. a query for "idli" should match a listing described as
            "South Indian breakfast tiffin" even though the words differ, and a query for
            "something sweet and cold" should match fresh fruit juice. Only include offerings
            that a reasonable buyer would consider a real match. If nothing genuinely matches,
            return an empty array: []

            Respond with nothing but the JSON array. No explanation, no markdown fencing.
            """;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final GeminiProperties properties;

    public GeminiSearchClient(GeminiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("x-goog-api-key", properties.apiKey())
                .defaultHeader("content-type", "application/json")
                .build();
    }

    /** Returns matched offering IDs ranked by relevance; empty list on no match or any failure. */
    public List<UUID> matchOfferings(String buyerQuery, List<CatalogEntry> catalog) {
        if (catalog.isEmpty()) {
            return List.of();
        }

        try {
            String catalogJson = objectMapper.writeValueAsString(catalog);
            String userMessage = "Buyer query: " + buyerQuery + "\n\nCatalog:\n" + catalogJson;

            GeminiGenerateContentRequest request = new GeminiGenerateContentRequest(
                    new GeminiGenerateContentRequest.SystemInstruction(
                            List.of(new GeminiGenerateContentRequest.Part(SYSTEM_PROMPT))),
                    List.of(new GeminiGenerateContentRequest.Content("user",
                            List.of(new GeminiGenerateContentRequest.Part(userMessage)))),
                    new GeminiGenerateContentRequest.GenerationConfig(0.0, 1024));

            GeminiGenerateContentResponse response = restClient.post()
                    .uri("/v1beta/models/{model}:generateContent", properties.model())
                    .body(request)
                    .retrieve()
                    .body(GeminiGenerateContentResponse.class);

            return parseMatchedIds(response, catalog);
        } catch (Exception e) {
            log.warn("Gemini search call failed for query '{}': {}", buyerQuery, e.getMessage());
            return List.of();
        }
    }

    private List<UUID> parseMatchedIds(GeminiGenerateContentResponse response, List<CatalogEntry> catalog) {
        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            return List.of();
        }
        var parts = response.candidates().get(0).content().parts();
        if (parts == null || parts.isEmpty()) {
            return List.of();
        }
        String text = parts.get(0).text();

        Matcher matcher = JSON_ARRAY.matcher(text);
        if (!matcher.find()) {
            return List.of();
        }

        try {
            Set<String> validIds = catalog.stream()
                    .map(c -> c.offeringId().toString())
                    .collect(Collectors.toSet());

            String[] rawIds = objectMapper.readValue(matcher.group(), String[].class);
            // Filter out any ID Gemini returns that isn't actually in the catalog.
            List<UUID> matched = new ArrayList<>();
            for (String rawId : rawIds) {
                if (validIds.contains(rawId)) {
                    matched.add(UUID.fromString(rawId));
                }
            }
            return matched;
        } catch (Exception e) {
            log.warn("Failed to parse Gemini's match response: {}", e.getMessage());
            return List.of();
        }
    }
}
