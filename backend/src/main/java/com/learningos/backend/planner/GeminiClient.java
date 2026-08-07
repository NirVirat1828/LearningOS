package com.learningos.backend.planner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Concrete {@link LlmClient} strategy that calls the Google Gemini REST API.
 *
 * <p>This class is the <em>only</em> place in the codebase that knows about
 * Gemini.  All other code (callers, tests) programs to the {@link LlmClient}
 * interface, so replacing Gemini with another provider requires adding a new
 * class like this one and pointing Spring at it — zero edits elsewhere.</p>
 *
 * <p>The API key is <strong>never</strong> hard-coded here.  It is read at
 * runtime from the {@code GEMINI_API_KEY} environment variable (or any other
 * source Spring's {@link Value} understands), so it never ends up in version
 * control and can be rotated without touching source code.</p>
 */
public class GeminiClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);

    // Gemini 2.0 Flash — fast, cost-efficient model for structured generation.
    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    private final String apiKey;
    private final RestClient restClient;

    public GeminiClient(@Value("${gemini.api.key}") String apiKey) {
        this.apiKey  = apiKey;
        this.restClient = RestClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        log.info("GeminiClient initialised (key masked: {}****)", apiKey.substring(0, 6));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Sends a {@code generateContent} request to Gemini and returns the
     * first candidate's text.  Throws a {@link GeminiException} if the API
     * returns an error or an unexpected response shape.</p>
     */
    @Override
    @SuppressWarnings("unchecked")
    public String chat(String prompt) {
        log.debug("Sending prompt to Gemini: {}", prompt);

        // Gemini request body: { "contents": [{ "parts": [{ "text": "..." }] }] }
        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                )
        );

        String url = GEMINI_API_URL + "?key=" + apiKey;

        Map<String, Object> response = restClient.post()
                .uri(url)
                .body(body)
                .retrieve()
                .body(Map.class);

        try {
            List<Map<String, Object>> candidates =
                    (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content  = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            String text = (String) parts.get(0).get("text");
            log.debug("Gemini responded: {}", text);
            return text;
        } catch (Exception e) {
            throw new GeminiException("Unexpected Gemini response shape: " + response, e);
        }
    }
}
