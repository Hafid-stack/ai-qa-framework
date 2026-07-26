package cli;

import ai.NaivePageObjectGenerator;
import fetch.PageFetcher;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main7 {
    public static void main(String[] args) throws IOException {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless=new");
        WebDriver driver = new ChromeDriver(chromeOptions);

        PageFetcher pageFetcher = new PageFetcher(driver);
        NaivePageObjectGenerator naiveGenerator = new NaivePageObjectGenerator();
        Files.createDirectories(Paths.get("src/main/java/pages/naive"));

        // --- SauceDemo ---
        String sauceHtml = pageFetcher.getHtml("https://www.saucedemo.com/");
        String sauceResult = naiveGenerator.generateFromRawHtml("LoginPageNaiveB", sauceHtml);
        Files.writeString(Paths.get("src/main/java/pages/naive/LoginPageNaiveB.java"), sauceResult);
        System.out.println("=== SauceDemo naive result written ===");

        // --- automationexercise.com ---
        String exerciseHtml = pageFetcher.getHtml("https://automationexercise.com/login");
        String exerciseResult = naiveGenerator.generateFromRawHtml("LoginPageNaiveB2", exerciseHtml);
        Files.writeString(Paths.get("src/main/java/pages/naive/LoginPageNaiveB2.java"), exerciseResult);
        System.out.println("=== automationexercise.com naive result written ===");

        driver.quit();
    }
}