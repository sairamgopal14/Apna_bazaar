package in.apnabazaar.gemini;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Turns one week of aggregated search activity into 2-3 plain-English sentences an admin
 * can act on — the only Gemini client in this project that writes an observation, rather
 * than matching a catalog ({@link GeminiSearchClient}) or drafting outreach text
 * ({@link GeminiNudgeClient}).
 */
@Component
public class GeminiInsightClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiInsightClient.class);

    private static final String SYSTEM_PROMPT = """
            You are a business analyst for Apna Bazaar, a hyperlocal marketplace inside a
            gated residential community. You are given one week of aggregated buyer search
            activity for one community.

            Write 2 to 3 short, plain-English sentences highlighting the single most useful
            pattern the community admin should act on this week — for example a recurring
            unmet demand, an unusually busy or quiet period, or a query that suddenly grew.
            Be specific and reference the actual numbers given. No greeting, no bullet
            points, no markdown — just the sentences.
            """;

    private final RestClient restClient;
    private final GeminiProperties properties;

    public GeminiInsightClient(GeminiProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("x-goog-api-key", properties.apiKey())
                .defaultHeader("content-type", "application/json")
                .build();
    }

    /** Returns Gemini's written insight, or null on any failure. */
    public String generateInsight(String weeklySummary) {
        try {
            GeminiGenerateContentRequest request = new GeminiGenerateContentRequest(
                    new GeminiGenerateContentRequest.SystemInstruction(
                            List.of(new GeminiGenerateContentRequest.Part(SYSTEM_PROMPT))),
                    List.of(new GeminiGenerateContentRequest.Content("user",
                            List.of(new GeminiGenerateContentRequest.Part(weeklySummary)))),
                    new GeminiGenerateContentRequest.GenerationConfig(0.3, 256));

            GeminiGenerateContentResponse response = restClient.post()
                    .uri("/v1beta/models/{model}:generateContent", properties.model())
                    .body(request)
                    .retrieve()
                    .body(GeminiGenerateContentResponse.class);

            return extractText(response);
        } catch (Exception e) {
            log.warn("Gemini insight generation failed: {}", e.getMessage());
            return null;
        }
    }

    private String extractText(GeminiGenerateContentResponse response) {
        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            return null;
        }
        var parts = response.candidates().get(0).content().parts();
        if (parts == null || parts.isEmpty()) {
            return null;
        }
        return parts.get(0).text().trim();
    }
}
