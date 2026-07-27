package cli;

import ai.GeminiClient;
import ai.NaivePageObjectGenerator;
import ai.OllamaClient;
import ai.SelectorReviewClient;
import ai.SelectorReviewResult;
import fetch.PageFetcher;
import generator.PageObjectGenerator;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import parser.ExtractedElement;
import parser.HtmlParser;
import parser.SelectorPriorityFinder;
import parser.WebElementSelector;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== AI-QA Framework: Pipeline Runner ===");
        System.out.println();
        System.out.println("1 - Pipeline A: structured extraction + generate Page Object");
        System.out.println("2 - Pipeline A + AI judgment-call review of low-confidence selectors");
        System.out.println("3 - Pipeline B: naive raw-HTML AI baseline");
        System.out.print("Choose an option (1/2/3): ");
        String choice = scanner.nextLine().trim();

        System.out.print("Enter the URL to test: ");
        String url = scanner.nextLine().trim();

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
            System.out.println("(No protocol specified, assuming: " + url + ")");
        }

        System.out.print("Enter a class name for the generated Page Object (e.g. LoginPageAI): ");
        String className = scanner.nextLine().trim();

        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless=new");
        WebDriver driver = new ChromeDriver(chromeOptions);

        try {
            switch (choice) {
                case "1" -> runPipelineA(driver, url, className);
                case "2" -> runPipelineAWithReview(driver, url, className, scanner);
                case "3" -> runPipelineB(driver, url, className);
                default -> System.out.println("Unrecognized option, exiting.");
            }
        } finally {
            driver.quit();
        }
    }

    private static void runPipelineA(WebDriver driver, String url, String className) throws IOException {
        List<WebElementSelector> selectors = extractSelectors(driver, url);

        PageObjectGenerator generator = new PageObjectGenerator();
        String classSource = generator.generateClassSource(className, selectors);
        writeGeneratedFile("src/main/java/pages/generated/", className, classSource);

        long lowConfidenceCount = selectors.stream().filter(WebElementSelector::isLowConfidence).count();
        System.out.println("Total selectors: " + selectors.size());
        System.out.println("Low-confidence: " + lowConfidenceCount);
    }

    private static void runPipelineAWithReview(WebDriver driver, String url, String className, Scanner scanner) throws IOException {
        List<WebElementSelector> selectors = extractSelectors(driver, url);
        List<WebElementSelector> lowConfidence = selectors.stream().filter(WebElementSelector::isLowConfidence).toList();

        System.out.println("Total selectors: " + selectors.size());
        System.out.println("Low-confidence selectors to review: " + lowConfidence.size());

        System.out.print("Use Gemini (g) or local Ollama (o) for review? ");
        String backendChoice = scanner.nextLine().trim().toLowerCase();

        SelectorReviewClient reviewClient = backendChoice.equals("o")
                ? new SelectorReviewClient(new OllamaClient())
                : new SelectorReviewClient(new GeminiClient());

        for (WebElementSelector selector : lowConfidence) {
            System.out.println("Reviewing: " + selector.getSelectorName() + " (" + selector.getSelectorValue() + ")");
            SelectorReviewResult result = reviewClient.review(selector);

            if (!result.succeeded()) {
                System.out.println("  -> AI review FAILED: " + result.getFailureReason());
            } else {
                System.out.println("  -> Keep: " + result.shouldKeepElement()
                        + " | Methods: " + result.getSuggestedMethods()
                        + " | Confidence: " + result.getConfidence()
                        + " | Concern: " + (result.getConcern().isEmpty() ? "(none)" : result.getConcern()));
            }
        }

        PageObjectGenerator generator = new PageObjectGenerator();
        String classSource = generator.generateClassSource(className, selectors);
        writeGeneratedFile("src/main/java/pages/generated/", className, classSource);
    }

    private static void runPipelineB(WebDriver driver, String url, String className) throws IOException {
        PageFetcher pageFetcher = new PageFetcher(driver);
        String html = pageFetcher.getHtml(url);

        NaivePageObjectGenerator naiveGenerator = new NaivePageObjectGenerator();
        String result = naiveGenerator.generateFromRawHtml(className, html);
        writeGeneratedFile("src/main/java/pages/naive/", className, result);
    }

    private static List<WebElementSelector> extractSelectors(WebDriver driver, String url) {
        PageFetcher pageFetcher = new PageFetcher(driver);
        String html = pageFetcher.getHtml(url);

        HtmlParser htmlParser = new HtmlParser();
        List<ExtractedElement> elements = htmlParser.extractInteractiveElements(html);

        SelectorPriorityFinder finder = new SelectorPriorityFinder();
        return finder.getOrder(elements);
    }

    private static void writeGeneratedFile(String folder, String className, String content) throws IOException {
        String path = folder + className + ".java";
        java.nio.file.Path filePath = java.nio.file.Paths.get(path);

        if (java.nio.file.Files.exists(filePath)) {
            System.out.println("WARNING: " + path + " already exists and will be OVERWRITTEN.");
            System.out.println("(Rename the class, or back up the old file first, if you want to keep it.)");
        }

        java.nio.file.Files.createDirectories(java.nio.file.Paths.get(folder));
        java.nio.file.Files.writeString(filePath, content);
        System.out.println("Written: " + path);
    }
}