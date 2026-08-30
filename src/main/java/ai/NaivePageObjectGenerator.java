package ai;

import java.io.IOException;

public class NaivePageObjectGenerator {

    private final GeminiClient geminiClient;

    public NaivePageObjectGenerator() {
        this.geminiClient = new GeminiClient();
    }

    public String generateFromRawHtml(String className, String rawHtml) throws IOException {
        String prompt = """
            You are given the raw HTML of a web page. Generate a complete Java Page Object class \
            for Selenium WebDriver automation.

            Requirements:
            - The class must be named %s
            - It must extend a class called BasePage (package: base) which already provides \
              these protected methods you should use: click(By locator), type(By locator, String value), \
              getText(By locator), isDisplayed(By locator)
            - Include a constructor that takes a WebDriver and calls super(driver)
            - Identify all interactive elements (inputs, buttons, links) in the HTML
            - For each element, create a private final By field with the best available locator \
              (prefer data-test, data-qa, id, or name attributes over relying on visible text)
            - Generate appropriate methods per element (type methods for inputs, click methods for \
              buttons/links, getText methods where the element has meaningful visible text)
            - Skip hidden inputs (type="hidden") - they are not real interactive elements
            - Respond with ONLY the raw Java source code, no explanation, no markdown code fences

            HTML:
            %s
            """.formatted(className, rawHtml);

        return stripMarkdownFences(geminiClient.sendPrompt(prompt));
    }

    // The model sometimes wraps its answer in ```java ... ``` despite being told not to.
    // That is a response-formatting artefact, not a code-quality defect, so it is removed
    // before the output is evaluated - otherwise Pipeline B would fail the "does it compile?"
    // metric for a purely cosmetic reason and the A/B comparison would be unfair.
    private String stripMarkdownFences(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(java)?", "").trim();
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3).trim();
            }
        }
        return trimmed;
    }
}