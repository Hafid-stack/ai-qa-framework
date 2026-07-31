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

    // Captures whatever page is currently loaded, without navigating anywhere first.
    // Used after a manual login pause, where navigating again would throw away the
    // state the user just reached by hand (post-login redirect, a page they clicked into, etc.).
    public String getCurrentHtml() {
        return driver.getPageSource();
    }
}