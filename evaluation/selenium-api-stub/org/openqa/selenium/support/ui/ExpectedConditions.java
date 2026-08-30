package org.openqa.selenium.support.ui;
import org.openqa.selenium.*;
import java.util.function.Function;
public class ExpectedConditions {
    public static Function<WebDriver, WebElement> visibilityOfElementLocated(By locator) { return d -> null; }
    public static Function<WebDriver, WebElement> elementToBeClickable(By locator) { return d -> null; }
}
