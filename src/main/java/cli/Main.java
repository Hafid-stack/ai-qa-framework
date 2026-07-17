package cli;

import fetch.PageFetcher;
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

public class Main {
    public static void main(String[] args) {
        Generator generator = new Generator();
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless=new");
        WebDriver driver = new ChromeDriver(chromeOptions);


        PageFetcher pageFetcher = new PageFetcher(driver);
        //we get the raw html
        String html = pageFetcher.getHtml(ConfigReader.get("loginUrl"));

        HtmlParser parser = new HtmlParser();

        List<ExtractedElement> elements = parser.extractInteractiveElements(html);
        for (ExtractedElement element : elements) {
            System.out.println(element);


        }
        SelectorPriorityFinder inputOrder = new SelectorPriorityFinder();
        List<WebElementSelector> cssSelectors=inputOrder.getOrder(elements);
        for (WebElementSelector cssSelector : cssSelectors) {
            System.out.println(generator.buildJavaFieldDeclaration(cssSelector));
        }


        driver.quit();
    }
}