package cli;

import ai.SelectorReviewClient;
import ai.SelectorReviewResult;
import fetch.PageFetcher;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import parser.ExtractedElement;
import parser.HtmlParser;
import parser.SelectorPriorityFinder;
import parser.WebElementSelector;

import java.util.List;

public class Main5 {
    public static void main(String[] args) {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless=new");
        WebDriver driver = new ChromeDriver(chromeOptions);

        PageFetcher pageFetcher = new PageFetcher(driver);
        String html = pageFetcher.getHtml("https://automationexercise.com/login");

        HtmlParser parser = new HtmlParser();
        List<ExtractedElement> elements = parser.extractInteractiveElements(html);

        SelectorPriorityFinder inputOrder = new SelectorPriorityFinder();
        List<WebElementSelector> allSelectors = inputOrder.getOrder(elements);

        List<WebElementSelector> lowConfidenceSelectors = allSelectors.stream()
                .filter(WebElementSelector::isLowConfidence)
                .toList();

        System.out.println("Total selectors: " + allSelectors.size());
        System.out.println("Low-confidence selectors to review: " + lowConfidenceSelectors.size());
        System.out.println("---");

        SelectorReviewClient reviewClient = new SelectorReviewClient();

        for (WebElementSelector selector : lowConfidenceSelectors) {
            System.out.println("Reviewing: " + selector.getSelectorName() + " (" + selector.getSelectorValue() + ")");

            SelectorReviewResult result = reviewClient.review(selector);

            if (!result.succeeded()) {
                System.out.println("  -> AI review FAILED: " + result.getFailureReason());
                System.out.println("  -> Falling back to deterministic output (element kept as-is)");
            } else {
                System.out.println("  -> Keep element: " + result.shouldKeepElement());
                System.out.println("  -> Suggested methods: " + result.getSuggestedMethods());
                System.out.println("  -> Confidence: " + result.getConfidence());
                System.out.println("  -> Concern: " + (result.getConcern().isEmpty() ? "(none)" : result.getConcern()));
            }
            System.out.println("---");
        }

        driver.quit();
    }
}