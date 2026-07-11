package base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import utils.ConfigReader;

public class BaseTest {

    protected WebDriver driver;

    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        // Headless is required for CI (GitHub Actions / Jenkins have no display).
        // Locally, it runs with a visible browser unless CI env var is set.
        if (System.getenv("CI") != null) {
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
        }
        //Maybe it is better to just maximize in the future
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
        //to be tested
        //driver.navigate().to();
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