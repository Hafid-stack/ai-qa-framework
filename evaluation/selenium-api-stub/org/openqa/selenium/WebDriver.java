package org.openqa.selenium;
import java.util.List;
public interface WebDriver extends SearchContext {
    void get(String url);
    String getTitle();
    String getPageSource();
    void quit();
    WebElement findElement(By by);
    List<WebElement> findElements(By by);
}
