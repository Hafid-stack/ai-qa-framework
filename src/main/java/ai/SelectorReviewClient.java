package ai;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import parser.WebElementSelector;

import java.io.IOException;
import java.util.List;

public class SelectorReviewClient {

    private final AIClient aiClient;
    private final SelectorReviewPromptBuilder promptBuilder;
    private final Gson gson = new Gson();

    public SelectorReviewClient() {
        this.aiClient = new AIClient();
        this.promptBuilder = new SelectorReviewPromptBuilder();
    }

    public SelectorReviewResult review(WebElementSelector selector) {
        String prompt = promptBuilder.buildPrompt(selector);

        try {
            String rawResponse = aiClient.sendPrompt(prompt);
            String cleanedResponse = stripMarkdownFences(rawResponse);
            JsonObject json = gson.fromJson(cleanedResponse, JsonObject.class);

            boolean keepElement = json.has("keepElement") && json.get("keepElement").getAsBoolean();
            String concern = json.has("concern") ? json.get("concern").getAsString() : "";
            String confidence = json.has("confidence") ? json.get("confidence").getAsString() : "unknown";

            List<String> suggestedMethods = new java.util.ArrayList<>();
            if (json.has("suggestedMethods")) {
                json.getAsJsonArray("suggestedMethods").forEach(el -> suggestedMethods.add(el.getAsString()));
            }

            return new SelectorReviewResult(true, keepElement, suggestedMethods, concern, confidence, null);

        } catch (IOException e) {
            // Network/API failure — don't crash the whole pipeline, just flag this one element as unreviewed
            return SelectorReviewResult.failed("API call failed: " + e.getMessage());
        } catch (JsonSyntaxException | NullPointerException | IllegalStateException e) {
            // Gemini didn't return valid, well-formed JSON despite instructions — also shouldn't crash the pipeline
            return SelectorReviewResult.failed("Could not parse AI response: " + e.getMessage());
        }
    }

    // Gemini sometimes wraps JSON in ```json ... ``` even when told not to — strip that defensively
    private String stripMarkdownFences(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(json)?", "").trim();
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3).trim();
            }
        }
        return trimmed;
    }
}