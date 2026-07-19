package ai;

import parser.WebElementSelector;

public class SelectorReviewPromptBuilder {

    public String buildPrompt(WebElementSelector selector) {
        return """
            You are reviewing one HTML element from an automated Page Object generator, \
            to judge whether the generated code makes sense.

            Element data:
            - Variable name: %s
            - Selector: %s
            - Selector type: %s
            - Element category (input/button/link): %s
            - Has visible text: %s
            - Matched via: %s (this means no data-test/data-qa/id/name was found; \
              this is a low-confidence, fallback match)

            Answer with ONLY a JSON object, no other text, no markdown formatting, \
            in exactly this shape:
            {
              "keepElement": true or false,
              "suggestedMethods": array of strings, choose from ["click", "type", "getText", "isDisplayed"],
              "concern": "one short sentence describing any risk or issue, or empty string if none",
              "confidence": "low", "medium", or "high"
            }
            """.formatted(
                selector.getSelectorName(),
                selector.getSelectorValue(),
                selector.getSelectorType(),
                selector.getElementCategory(),
                selector.hasVisibleText(),
                selector.getMatchedVia()
        );
    }
}