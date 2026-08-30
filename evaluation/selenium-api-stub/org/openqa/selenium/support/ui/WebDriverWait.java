package org.openqa.selenium.support.ui;
import org.openqa.selenium.WebDriver;
import java.time.Duration;
public class WebDriverWait {
    public WebDriverWait(WebDriver driver, Duration timeout) {}
    public <V> V until(java.util.function.Function<WebDriver, V> condition) { return null; }
}
