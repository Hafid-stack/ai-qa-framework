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

        return geminiClient.sendPrompt(prompt);
    }
}