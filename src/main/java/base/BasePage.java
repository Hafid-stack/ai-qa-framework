package base;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;

/**
 * BasePage holds reusable Selenium actions shared by every page object.
 * Page objects extend this class instead of talking to WebDriver directly.
 */
public class BasePage {

    protected WebDriver driver;
    protected WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    protected WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }
    protected int countElements(By locator) {
        return driver.findElements(locator).size();
    }
    protected void click(By locator) {
        try {
            waitForClickable(locator).click();
        } catch (Exception e) {
            // Fallback: some elements (overlays, popups) block normal clicks.
            // JS click bypasses the visual overlay check.
            WebElement el = driver.findElement(locator);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }

    protected void type(By locator, String text) {
        WebElement el = waitForVisible(locator);
        el.clear();
        el.sendKeys(text);
    }

//protected void type(By locator, String text) {
//
//    WebElement element =
//            wait.until(ExpectedConditions.elementToBeClickable(locator));
//
//    int attempts = 0;
//
//    while (attempts < 3) {
//
//        element.clear();
//
//        if (text.isEmpty()) {
//            element.sendKeys(" ");
//            element.sendKeys(Keys.BACK_SPACE);
//        } else {
//            element.sendKeys(text);
//        }
//
//        String enteredText = element.getAttribute("value");
//
//        if (enteredText.equals(text)) {
//            return;
//        }
//
//        attempts++;
//    }

//    throw new RuntimeException("Failed to type text into field: " + locator);
//}

    protected String getText(By locator) {
        return waitForVisible(locator).getText();
    }

    protected boolean isDisplayed(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    protected void navigateTo(String url) {
        driver.get(url);
    }

    protected void log(String message) {
        System.out.println("[PAGE LOG] "+message);
    }
    protected void dismissChromePasswordPopupIfPresent() {
        try {
            // Chrome's leak-detection popup shows a "No thanks" or close button
            // This checks briefly without waiting the full timeout, since most tests won't hit it
            java.util.List<WebElement> possiblePopupButtons = driver.findElements(
                    By.xpath("//button[contains(text(),'No thanks') or contains(text(),'Not now')]")
            );
            if (!possiblePopupButtons.isEmpty()) {
                possiblePopupButtons.get(0).click();
            }
        } catch (Exception e) {
            // Popup wasn't there, or didn't match — safe to ignore, this is best-effort only
        }
    }
}