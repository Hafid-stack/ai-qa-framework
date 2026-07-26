package pages.naive;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPageNaiveB2 extends BasePage {

    // Locators
    private final By homeLink = By.cssSelector(".shop-menu a[href='/']");
    private final By productsLink = By.cssSelector("a[href='/products']");
    private final By cartLink = By.cssSelector("a[href='/view_cart']");
    private final By loginSignupLink = By.cssSelector("a[href='/login']");
    private final By testCasesLink = By.cssSelector("a[href='/test_cases']");
    private final By apiTestingLink = By.cssSelector("a[href='/api_list']");
    private final By videoTutorialsLink = By.cssSelector("a[href*='youtube.com/c/AutomationExercise']");
    private final By contactUsLink = By.cssSelector("a[href='/contact_us']");

    private final By loginTitle = By.cssSelector(".login-form h2");
    private final By loginEmailInput = By.cssSelector("[data-qa='login-email']");
    private final By loginPasswordInput = By.cssSelector("[data-qa='login-password']");
    private final By loginButton = By.cssSelector("[data-qa='login-button']");

    private final By signupTitle = By.cssSelector(".signup-form h2");
    private final By signupNameInput = By.cssSelector("[data-qa='signup-name']");
    private final By signupEmailInput = By.cssSelector("[data-qa='signup-email']");
    private final By signupButton = By.cssSelector("[data-qa='signup-button']");

    private final By subscribeEmailInput = By.id("susbscribe_email");
    private final By subscribeButton = By.id("subscribe");

    // Constructor
    public LoginPageNaiveB2(WebDriver driver) {
        super(driver);
    }

    // Navigation Methods
    public void clickHome() {
        click(homeLink);
    }

    public void clickProducts() {
        click(productsLink);
    }

    public void clickCart() {
        click(cartLink);
    }

    public void clickLoginSignup() {
        click(loginSignupLink);
    }

    public void clickTestCases() {
        click(testCasesLink);
    }

    public void clickApiTesting() {
        click(apiTestingLink);
    }

    public void clickVideoTutorials() {
        click(videoTutorialsLink);
    }

    public void clickContactUs() {
        click(contactUsLink);
    }

    // Login Form Methods
    public String getLoginTitleText() {
        return getText(loginTitle);
    }

    public void enterLoginEmail(String email) {
        type(loginEmailInput, email);
    }

    public void enterLoginPassword(String password) {
        type(loginPasswordInput, password);
    }

    public void clickLogin() {
        click(loginButton);
    }

    // Signup Form Methods
    public String getSignupTitleText() {
        return getText(signupTitle);
    }

    public void enterSignupName(String name) {
        type(signupNameInput, name);
    }

    public void enterSignupEmail(String email) {
        type(signupEmailInput, email);
    }

    public void clickSignup() {
        click(signupButton);
    }

    // Subscription Methods
    public void enterSubscribeEmail(String email) {
        type(subscribeEmailInput, email);
    }

    public void clickSubscribe() {
        click(subscribeButton);
    }
}