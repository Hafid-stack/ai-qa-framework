package pages.generated;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class EvalLoginP4 extends BasePage {

    private final By byLoginEmail = By.cssSelector("[data-qa='login-email']");
    private final By byLoginPassword = By.cssSelector("[data-qa='login-password']");
    private final By bySignupName = By.cssSelector("[data-qa='signup-name']");
    private final By bySignupEmail = By.cssSelector("[data-qa='signup-email']");
    private final By byLoginButton = By.cssSelector("[data-qa='login-button']");
    private final By bySignupButton = By.cssSelector("[data-qa='signup-button']");
    private final By byScrollUp = By.cssSelector("[id='scrollUp']");

    public EvalLoginP4(WebDriver driver) {
        super(driver);
    }

    public void typeLoginEmail(String value) {
        type(byLoginEmail, value);
    }

    public void typeLoginPassword(String value) {
        type(byLoginPassword, value);
    }

    public void typeSignupName(String value) {
        type(bySignupName, value);
    }

    public void typeSignupEmail(String value) {
        type(bySignupEmail, value);
    }

    public void clickLoginButton() {
        click(byLoginButton);
    }

    public void clickSignupButton() {
        click(bySignupButton);
    }

    public void clickScrollUp() {
        click(byScrollUp);
    }

}
