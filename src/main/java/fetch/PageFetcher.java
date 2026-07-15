package fetch;

import org.openqa.selenium.WebDriver;

public class PageFetcher {

    private WebDriver driver;

    public PageFetcher(WebDriver driver) {
        this.driver = driver;
    }

    public String getHtml(String url) {
        driver.navigate().to(url);
        return driver.getPageSource();
    }
}