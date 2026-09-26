package pages.generated;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class SauceDemoLoginPageBody extends BasePage {

    private final By byUsername = By.cssSelector("[data-test='username']");
    private final By byPassword = By.cssSelector("[data-test='password']");
    private final By byLoginButton = By.cssSelector("[data-test='login-button']");

    public SauceDemoLoginPageBody(WebDriver driver) {
        super(driver);
    }

    public void typeUsername(String value) {
        type(byUsername, value);
    }

    public void typePassword(String value) {
        type(byPassword, value);
    }

    public void clickLoginButton() {
        click(byLoginButton);
    }

}
