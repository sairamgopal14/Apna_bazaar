package in.apnabazaar.gemini;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Asks Gemini to draft nudge message options for a real demand gap from
 * {@code zero_result_log} — a genuinely different job than {@link GeminiSearchClient}:
 * that one picks from an existing catalog, this one writes original text. Gemini only
 * ever proposes here — nothing it returns gets sent to anyone without an admin
 * choosing one first.
 */
@Component
public class GeminiNudgeClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiNudgeClient.class);
    private static final Pattern NUMBERED_LINE = Pattern.compile("^\\s*\\d+[.)]\\s*(.+)$", Pattern.MULTILINE);

    private static final String SYSTEM_PROMPT = """
            You are writing short nudge messages for Apna Bazaar, a hyperlocal marketplace
            inside a gated residential community. Residents have been searching for something
            that no seller currently offers — you're drafting a message the community admin
            could send out, either to find a seller who can fill the gap or to ask residents
            for recommendations.

            Write exactly 4 different short message options (each under 200 characters, friendly,
            suitable for a WhatsApp-style community message). Number them 1 to 4, one per line,
            nothing else — no introduction, no explanation.
            """;

    private final RestClient restClient;
    private final GeminiProperties properties;

    public GeminiNudgeClient(GeminiProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader("x-goog-api-key", properties.apiKey())
                .defaultHeader("content-type", "application/json")
                .build();
    }

    /** Returns up to 4 drafted nudge options; empty list on any failure. */
    public List<String> generateNudgeOptions(String normalisedQuery, int occurrenceCount) {
        try {
            String userMessage = "Buyers have searched for \"" + normalisedQuery + "\" " + occurrenceCount
                    + " time(s) with no matching seller. Draft the 4 message options.";

            GeminiGenerateContentRequest request = new GeminiGenerateContentRequest(
                    new GeminiGenerateContentRequest.SystemInstruction(
                            List.of(new GeminiGenerateContentRequest.Part(SYSTEM_PROMPT))),
                    List.of(new GeminiGenerateContentRequest.Content("user",
                            List.of(new GeminiGenerateContentRequest.Part(userMessage)))),
                    new GeminiGenerateContentRequest.GenerationConfig(0.7, 512));

            GeminiGenerateContentResponse response = restClient.post()
                    .uri("/v1beta/models/{model}:generateContent", properties.model())
                    .body(request)
                    .retrieve()
                    .body(GeminiGenerateContentResponse.class);

            return parseOptions(response);
        } catch (Exception e) {
            log.warn("Gemini nudge generation failed for '{}': {}", normalisedQuery, e.getMessage());
            return List.of();
        }
    }

    private List<String> parseOptions(GeminiGenerateContentResponse response) {
        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            return List.of();
        }
        var parts = response.candidates().get(0).content().parts();
        if (parts == null || parts.isEmpty()) {
            return List.of();
        }
        String text = parts.get(0).text();

        List<String> options = new ArrayList<>();
        Matcher matcher = NUMBERED_LINE.matcher(text);
        while (matcher.find()) {
            options.add(matcher.group(1).trim());
        }
        return options;
    }
}
