package cli;

import fetch.PageFetcher;
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

import java.util.List;

public class MainTwo {
    public static void main(String[] args) {
        Generator generator = new Generator();
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless=new");
        WebDriver driver = new ChromeDriver(chromeOptions);

        // Log in first, since checkout requires authentication
        driver.get(ConfigReader.get("loginUrl"));
        driver.findElement(By.cssSelector("[data-test='username']")).sendKeys(ConfigReader.get("valid.username"));
        driver.findElement(By.cssSelector("[data-test='password']")).sendKeys(ConfigReader.get("valid.password"));
        driver.findElement(By.cssSelector("[data-test='login-button']")).click();

        // Add an item to cart, then go to checkout step one — this page has firstName/lastName/postalCode, all type=text
        driver.get("https://www.saucedemo.com/checkout-step-one.html");

        PageFetcher pageFetcher = new PageFetcher(driver);
        String html = pageFetcher.getHtml("https://www.saucedemo.com/checkout-step-one.html");

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

        driver.quit();
    }
}