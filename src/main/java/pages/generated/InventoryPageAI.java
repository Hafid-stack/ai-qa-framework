package pages.generated;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class InventoryPageAI extends BasePage {

    private final By byLoginEmail = By.cssSelector("[data-test='login-email']");
    private final By byLoginPassword = By.cssSelector("[data-test='login-password']");
    private final By bySignupName = By.cssSelector("[data-test='signup-name']");
    private final By bySignupEmail = By.cssSelector("[data-test='signup-email']");
    private final By bySusbscribeEmail = By.cssSelector("#susbscribe_email");
    private final By byLoginButton = By.cssSelector("[data-test='login-button']");
    private final By bySignupButton = By.cssSelector("[data-test='signup-button']");
    private final By bySubscribe = By.cssSelector("#subscribe");
    private final By byHome = By.linkText("Home");
    private final By byProducts = By.linkText(" Products");
    private final By byCart = By.linkText("Cart");
    private final By bySignupLogin = By.linkText("Signup / Login");
    private final By byTestCases = By.linkText("Test Cases");
    private final By byAPITesting = By.linkText("API Testing");
    private final By byVideoTutorials = By.linkText("Video Tutorials");
    private final By byContactUs = By.linkText("Contact us");
    private final By byScrollUp = By.cssSelector("#scrollUp");

    public InventoryPageAI(WebDriver driver) {
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

    public void typeSusbscribeEmail(String value) {
        type(bySusbscribeEmail, value);
    }

    public void clickLoginButton() {
        click(byLoginButton);
    }

    public void clickSignupButton() {
        click(bySignupButton);
    }

    public void clickSubscribe() {
        click(bySubscribe);
    }

    public void clickHome() {
        click(byHome);
    }

    public String getTextHome() {
        return getText(byHome);
    }

    public void clickProducts() {
        click(byProducts);
    }

    public String getTextProducts() {
        return getText(byProducts);
    }

    public void clickCart() {
        click(byCart);
    }

    public String getTextCart() {
        return getText(byCart);
    }

    public void clickSignupLogin() {
        click(bySignupLogin);
    }

    public String getTextSignupLogin() {
        return getText(bySignupLogin);
    }

    public void clickTestCases() {
        click(byTestCases);
    }

    public String getTextTestCases() {
        return getText(byTestCases);
    }

    public void clickAPITesting() {
        click(byAPITesting);
    }

    public String getTextAPITesting() {
        return getText(byAPITesting);
    }

    public void clickVideoTutorials() {
        click(byVideoTutorials);
    }

    public String getTextVideoTutorials() {
        return getText(byVideoTutorials);
    }

    public void clickContactUs() {
        click(byContactUs);
    }

    public String getTextContactUs() {
        return getText(byContactUs);
    }

    public void clickScrollUp() {
        click(byScrollUp);
    }

}
