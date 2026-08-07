package com.learningos.backend.planner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Wires the concrete {@link LlmClient} strategy for the {@code llm} profile.
 *
 * <p>Only active when {@code spring.profiles.active=llm}.  All LLM-related
 * infrastructure beans live here, keeping the rest of the application clean.</p>
 *
 * <p>To add a new provider (e.g. OpenAI), create a new {@code @Configuration}
 * class annotated with the new profile and define an {@code LlmClient} bean
 * there.  No other files need to change.</p>
 */
@Configuration
@Profile("llm")
public class LlmPlannerConfig {

    /**
     * Registers {@link GeminiClient} as the active {@link LlmClient} strategy.
     *
     * <p>The api key is supplied via the {@code GEMINI_API_KEY} environment
     * variable (mapped to {@code gemini.api.key} in {@code application.yml}).
     * It is never written to source files or logs.</p>
     *
     * @param apiKey the Gemini API key from the environment.
     * @return the Gemini-backed LLM client.
     */
    @Bean
    public LlmClient llmClient(@Value("${gemini.api.key}") String apiKey) {
        return new GeminiClient(apiKey);
    }
}
