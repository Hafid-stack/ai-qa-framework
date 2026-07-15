package cli;

import fetch.PageFetcher;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import parser.ExtractedElement;
import parser.HtmlParser;
import utils.ConfigReader;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        WebDriver driver = new ChromeDriver();

        PageFetcher pageFetcher = new PageFetcher(driver);
        String html = pageFetcher.getHtml(ConfigReader.get("loginUrl"));

        HtmlParser parser = new HtmlParser();
        List<ExtractedElement> elements = parser.extractInteractiveElements(html);

        for (ExtractedElement element : elements) {
            System.out.println(element);
        }

        driver.quit();
    }
}