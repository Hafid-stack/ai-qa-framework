package base;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

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

    /**
     * Like isDisplayed, but first waits (up to the normal timeout) for the element.
     * Use it for "has the page loaded?" checks that run right after a click or a
     * navigation, where checking instantly is a race the test can lose.
     */
    protected boolean isDisplayedAfterWait(By locator) {
        try {
            waitForVisible(locator);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Waits until at least one element matches, then returns them all. */
    protected java.util.List<WebElement> waitForAtLeastOne(By locator) {
        try {
            wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(locator, 0));
        } catch (Exception e) {
            // fall through: the caller receives an empty list and fails with a clear message
        }
        return driver.findElements(locator);
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