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
import parser.*;

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

        System.out.print("Does reaching this page require logging in first? (y/n): ");
        boolean requiresLogin = scanner.nextLine().trim().equalsIgnoreCase("y");

        ChromeOptions chromeOptions = new ChromeOptions();
        if (requiresLogin) {
            // Needs a real, visible window so the user can log in by hand below.
            chromeOptions.addArguments("--window-size=1400,1000");
        } else {
            chromeOptions.addArguments("--headless=new");
        }
        WebDriver driver = new ChromeDriver(chromeOptions);

        try {
            switch (choice) {
                case "1" -> runPipelineA(driver, url, className, scanner, requiresLogin);
                case "2" -> runPipelineAWithReview(driver, url, className, scanner, requiresLogin);
                case "3" -> runPipelineB(driver, url, className, scanner, requiresLogin);
                default -> System.out.println("Unrecognized option, exiting.");
            }
        } finally {
            driver.quit();
        }
    }

    // Fetches the target page's HTML. If it requires login, this pauses so the user can log
    // in (and navigate anywhere else needed) by hand in the visible browser window, then
    // captures whatever is currently loaded once they confirm — rather than assuming any
    // particular login form, which would only work for one specific site.
    private static String fetchHtml(WebDriver driver, String url, Scanner scanner, boolean requiresLogin) {
        PageFetcher pageFetcher = new PageFetcher(driver);

        if (!requiresLogin) {
            return pageFetcher.getHtml(url);
        }

        driver.navigate().to(url);
        System.out.println();
        System.out.println("A browser window is open at: " + url);
        System.out.println("Log in (and navigate to the exact page you want captured, if it's not this one),");
        System.out.print("then press Enter here to continue: ");
        scanner.nextLine();
        return pageFetcher.getCurrentHtml();
    }

    private static void runPipelineA(WebDriver driver, String url, String className, Scanner scanner, boolean requiresLogin) throws IOException {
        String html = fetchHtml(driver, url, scanner, requiresLogin);

        java.nio.file.Files.writeString(java.nio.file.Paths.get("page_dump.html"), html);
        System.out.println("HTML dumped to page_dump.html");

        HtmlParser htmlParser = new HtmlParser();
        // Repeated-component detection now happens inside extractSections, scoped to the
        // Body region only (header/footer are removed first) and stripped from the DOM
        // before the Body's own flat elements are extracted — so the two never overlap.
        List<PageSection> sections = htmlParser.extractSections(html);
        PageSection bodySection = sections.stream()
                .filter(s -> s.getName().equals("Body"))
                .findFirst()
                .orElseThrow();
        List<RepeatedComponentGroup> groups = bodySection.getComponentGroups();

        SelectorPriorityFinder finder = new SelectorPriorityFinder();
        PageObjectGenerator generator = new PageObjectGenerator();

        System.out.println("Detected " + groups.size() + " repeated component pattern(s) "
                + "(each becomes a nested class inside the Body page, not a separate file):");
        for (RepeatedComponentGroup group : groups) {
            System.out.println("  Pattern: " + group.getContainerPath() + " -> " + group.getRepeatCount() + " instances");
        }

        System.out.println();
        System.out.println("Which sections should be generated?");
        System.out.println("  b - Body only (default; use this for most pages)");
        System.out.println("  a - All sections (body, header, footer)");
        System.out.println("  h - Header only");
        System.out.println("  f - Footer only");
        System.out.print("Choice: ");
        String sectionChoice = scanner.nextLine().trim().toLowerCase();

        int totalSelectors = 0;
        int totalLowConfidence = 0;

        for (PageSection section : sections) {
            if (!shouldGenerate(section.getName(), sectionChoice)) {
                continue;
            }

            if (section.isEmpty()) {
                System.out.println("Section " + section.getName() + ": no interactive elements, skipped.");
                continue;
            }

            List<WebElementSelector> selectors = finder.getOrder(section.getElements());
            long lowConfidence = selectors.stream().filter(WebElementSelector::isLowConfidence).count();

            String sectionClassName = className + section.getName();
            String classSource = generator.generateClassSource(sectionClassName, selectors, section.getComponentGroups());
            writeGeneratedFile("src/main/java/pages/generated/", sectionClassName, classSource);

            System.out.println("Section " + section.getName() + ": "
                    + selectors.size() + " selectors, "
                    + lowConfidence + " low-confidence");

            totalSelectors += selectors.size();
            totalLowConfidence += lowConfidence;
        }

        System.out.println("---");
        System.out.println("Generated " + totalSelectors + " selectors, "
                + totalLowConfidence + " low-confidence");
    }

    private static boolean shouldGenerate(String sectionName, String choice) {
        return switch (choice) {
            case "a" -> true;
            case "h" -> sectionName.equals("Header");
            case "f" -> sectionName.equals("Footer");
            default -> sectionName.equals("Body");
        };
    }

    private static void runPipelineAWithReview(WebDriver driver, String url, String className, Scanner scanner, boolean requiresLogin) throws IOException {
        List<WebElementSelector> selectors = extractSelectors(driver, url, scanner, requiresLogin);
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

    private static void runPipelineB(WebDriver driver, String url, String className, Scanner scanner, boolean requiresLogin) throws IOException {
        String html = fetchHtml(driver, url, scanner, requiresLogin);

        NaivePageObjectGenerator naiveGenerator = new NaivePageObjectGenerator();
        String result = naiveGenerator.generateFromRawHtml(className, html);
        writeGeneratedFile("src/main/java/pages/naive/", className, result);
    }

    private static List<WebElementSelector> extractSelectors(WebDriver driver, String url, Scanner scanner, boolean requiresLogin) throws IOException {
        String html = fetchHtml(driver, url, scanner, requiresLogin);

        java.nio.file.Files.writeString(java.nio.file.Paths.get("page_dump.html"), html);
        System.out.println("HTML dumped to page_dump.html");
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