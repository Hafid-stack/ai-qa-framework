package cli;

import fetch.PageFetcher;
import generator.PageObjectGenerator;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import parser.ExtractedElement;
import parser.HtmlParser;
import parser.SelectorPriorityFinder;
import parser.WebElementSelector;
import utils.ConfigReader;
import utils.Generator;

import java.io.IOException;
import java.util.List;

public class Main3 {
    public static void main(String[] args) {
        Generator generator = new Generator();
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless=new");
        WebDriver driver = new ChromeDriver(chromeOptions);

        // Log in first, since checkout requires authentication
        //driver.get("https://automationexercise.com/login");
        //driver.findElement(By.cssSelector("[data-test='username']")).sendKeys(ConfigReader.get("valid.username"));
        //driver.findElement(By.cssSelector("[data-test='password']")).sendKeys(ConfigReader.get("valid.password"));
        //driver.findElement(By.cssSelector("[data-test='login-button']")).click();

        // Add an item to cart, then go to checkout step one — this page has firstName/lastName/postalCode, all type=text
        //driver.get("https://www.saucedemo.com/checkout-step-one.html");

        PageFetcher pageFetcher = new PageFetcher(driver);
        String html = pageFetcher.getHtml("https://automationexercise.com/login");

        HtmlParser parser = new HtmlParser();
        List<ExtractedElement> elements = parser.extractInteractiveElements(html);
        for (ExtractedElement element : elements) {
            System.out.println(element);
        }

        SelectorPriorityFinder inputOrder = new SelectorPriorityFinder();
        List<WebElementSelector> cssSelectors = inputOrder.getOrder(elements);
        for (WebElementSelector cssSelector : cssSelectors) {
            System.out.println(generator.buildJavaFieldDeclaration(cssSelector));
        }
        PageObjectGenerator generator2 = new PageObjectGenerator();
        String classSource = generator2.generateClassSource("InventoryPageAI", cssSelectors);
        try {
            generator2.writeToFile("InventoryPageAI", classSource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Generated: src/main/java/pages/generated/InventoryPageAI.java");
        List<WebElementSelector> lowConfidenceSelectors = cssSelectors.stream()
                .filter(WebElementSelector::isLowConfidence)
                .toList();

        System.out.println("Total selectors: " + cssSelectors.size());
        System.out.println("Low-confidence (need AI review): " + lowConfidenceSelectors.size());
        driver.quit();
    }
}