package pages.naive;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import base.BasePage;

public class LoginPageNaiveB extends BasePage {

    private final By usernameField = By.cssSelector("[data-test='username']");
    private final By passwordField = By.cssSelector("[data-test='password']");
    private final By loginButton = By.cssSelector("[data-test='login-button']");
    private final By credentialsInfo = By.cssSelector("[data-test='login-credentials']");
    private final By passwordInfo = By.cssSelector("[data-test='login-password']");

    public LoginPageNaiveB(WebDriver driver) {
        super(driver);
    }

    public void enterUsername(String username) {
        type(usernameField, username);
    }

    public void enterPassword(String password) {
        type(passwordField, password);
    }

    public void clickLogin() {
        click(loginButton);
    }

    public void login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLogin();
    }

    public String getCredentialsText() {
        return getText(credentialsInfo);
    }

    public String getPasswordText() {
        return getText(passwordInfo);
    }
}