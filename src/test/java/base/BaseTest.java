package base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import utils.ConfigReader;

import java.util.Map;

public class BaseTest {

    protected WebDriver driver;

    @BeforeMethod
    public void setUp() {
        String browser = System.getenv("BROWSER") != null ? System.getenv("BROWSER") : "chrome";
        boolean runHeadless = !"false".equalsIgnoreCase(System.getenv("HEADLESS"));

        if (browser.equalsIgnoreCase("firefox")) {
            driver = setUpFirefox(runHeadless);
        } else {
            driver = setUpChrome(runHeadless);
        }
    }

    private WebDriver setUpChrome(boolean runHeadless) {
        String chromedriverBin = System.getenv("CHROMEDRIVER_BIN");
        if (chromedriverBin != null && !chromedriverBin.isEmpty()) {
            System.setProperty("webdriver.chrome.driver", chromedriverBin);
        } else {
            WebDriverManager.chromedriver().setup();
        }

        ChromeOptions options = new ChromeOptions();
        // Headless is the default everywhere (local, CI). Set HEADLESS=false to watch it run.
        String chromeBin = System.getenv("CHROME_BIN");
        if (chromeBin != null && !chromeBin.isEmpty()) {
            options.setBinary(chromeBin);
        }
        if (runHeadless) {
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
        }
        //Maybe it is better to just maximize in the future
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-features=PasswordLeakDetection");
        options.setExperimentalOption("prefs", Map.of(
                "credentials_enable_service", false,
                "profile.password_manager_enabled", false
        ));

        return new ChromeDriver(options);
    }

    private WebDriver setUpFirefox(boolean runHeadless) {
        WebDriverManager.firefoxdriver().setup();

        FirefoxOptions options = new FirefoxOptions();
        if (runHeadless) {
            options.addArguments("--headless");
        }
        options.addArguments("--width=1920");
        options.addArguments("--height=1080");

        return new FirefoxDriver(options);
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected void log(String message) {
        System.out.println("[TEST LOG] "+message);
    }
}