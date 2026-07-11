package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.ConfigReader;

public class LoginPage extends BasePage {

    private final By usernameInput = By.cssSelector("[data-test='username']");
    private final By passwordInput = By.cssSelector("[data-test='password']");
    private final By loginButton = By.cssSelector("[data-test='login-button']");
    private final By errorMessage = By.cssSelector("[data-test='error']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        navigateTo(ConfigReader.get("loginUrl"));
    }

    public void typeUsername(String username) {
        type(usernameInput, username);
    }

    public void typePassword(String password) {
        type(passwordInput, password);
    }

    public void clickLogin() {
        click(loginButton);
    }

    public void loginAs(String username, String password) {
        typeUsername(username);
        typePassword(password);
        clickLogin();
    }

    public boolean isErrorDisplayed() {
        return isDisplayed(errorMessage);
    }

    public String getErrorMessage() {
        return getText(errorMessage);
    }
}