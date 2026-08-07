package com.learningos.backend.planner;

import java.util.List;

/**
 * Strategy interface for communicating with an LLM provider.
 *
 * <p><b>Why the Strategy Pattern?</b><br>
 * The planner must be able to switch LLM providers (Gemini today, OpenAI
 * tomorrow, a local Ollama model the day after) without touching any of the
 * caller code ({@link LlmPlannerService}).  The Strategy pattern achieves this
 * by hiding every provider-specific detail behind this single interface.  The
 * concrete implementation ({@link GeminiClient}, or any future class) is
 * selected at startup via Spring's dependency-injection container, so the
 * rest of the application never needs to know <em>which</em> LLM it's talking
 * to.</p>
 *
 * <p>Concretely, this means:
 * <ul>
 *   <li>Adding an OpenAI integration = add one new class, zero existing edits.</li>
 *   <li>Switching providers in production = change one property, restart.</li>
 *   <li>Unit-testing = inject a trivial mock, no real network call needed.</li>
 * </ul>
 */
public interface LlmClient {

    /**
     * Send a prompt to the underlying LLM and return the text response.
     *
     * @param prompt the natural-language instruction.
     * @return the LLM's text reply.
     */
    String chat(String prompt);
}
