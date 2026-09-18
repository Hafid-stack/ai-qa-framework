package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class EvalLoginP4Naive extends BasePage {

    private final By loginEmailField = By.cssSelector("[data-qa='login-email']");
    private final By loginPasswordField = By.cssSelector("[data-qa='login-password']");
    private final By loginButton = By.cssSelector("[data-qa='login-button']");
    
    private final By signupNameField = By.cssSelector("[data-qa='signup-name']");
    private final By signupEmailField = By.cssSelector("[data-qa='signup-email']");
    private final By signupButton = By.cssSelector("[data-qa='signup-button']");
    
    private final By subscribeEmailField = By.id("susbscribe_email");
    private final By subscribeButton = By.id("subscribe");

    public EvalLoginP4Naive(WebDriver driver) {
        super(driver);
    }

    public void typeLoginEmail(String email) {
        type(loginEmailField, email);
    }

    public void typeLoginPassword(String password) {
        type(loginPasswordField, password);
    }

    public void clickLoginButton() {
        click(loginButton);
    }

    public void typeSignupName(String name) {
        type(signupNameField, name);
    }

    public void typeSignupEmail(String email) {
        type(signupEmailField, email);
    }

    public void clickSignupButton() {
        click(signupButton);
    }

    public void typeSubscribeEmail(String email) {
        type(subscribeEmailField, email);
    }

    public void clickSubscribeButton() {
        click(subscribeButton);
    }

    public boolean isLoginButtonDisplayed() {
        return isDisplayed(loginButton);
    }

    public boolean isSignupButtonDisplayed() {
        return isDisplayed(signupButton);
    }
}