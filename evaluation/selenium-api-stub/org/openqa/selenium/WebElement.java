package org.openqa.selenium;
import java.util.List;
public interface WebElement extends SearchContext {
    void click();
    void clear();
    void sendKeys(CharSequence... keysToSend);
    String getText();
    boolean isDisplayed();
    String getAttribute(String name);
    WebElement findElement(By by);
    List<WebElement> findElements(By by);
}
