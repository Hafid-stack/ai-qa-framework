package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class EvalContactP5Naive extends BasePage {

    private final By nameField = By.cssSelector("input[data-qa='name']");
    private final By emailField = By.cssSelector("input[data-qa='email']");
    private final By subjectField = By.cssSelector("input[data-qa='subject']");
    private final By messageField = By.cssSelector("textarea[data-qa='message']");
    private final By uploadFileField = By.name("upload_file");
    private final By submitButton = By.cssSelector("input[data-qa='submit-button']");
    private final By subscribeEmailField = By.id("susbscribe_email");
    private final By subscribeButton = By.id("subscribe");
    private final By homeLink = By.cssSelector("a[href='/']");
    private final By productsLink = By.cssSelector("a[href='/products']");
    private final By cartLink = By.cssSelector("a[href='/view_cart']");
    private final By loginLink = By.cssSelector("a[href='/login']");
    private final By testCasesLink = By.cssSelector("a[href='/test_cases']");
    private final By apiTestingLink = By.cssSelector("a[href='/api_list']");
    private final By contactUsLink = By.cssSelector("a[href='/contact_us']");
    private final By successAlert = By.className("alert-success");

    public EvalContactP5Naive(WebDriver driver) {
        super(driver);
    }

    public void enterName(String name) {
        type(nameField, name);
    }

    public void enterEmail(String email) {
        type(emailField, email);
    }

    public void enterSubject(String subject) {
        type(subjectField, subject);
    }

    public void enterMessage(String message) {
        type(messageField, message);
    }

    public void uploadFile(String filePath) {
        type(uploadFileField, filePath);
    }

    public void clickSubmit() {
        click(submitButton);
    }

    public void subscribe(String email) {
        type(subscribeEmailField, email);
        click(subscribeButton);
    }

    public void clickHome() {
        click(homeLink);
    }

    public void clickProducts() {
        click(productsLink);
    }

    public void clickCart() {
        click(cartLink);
    }

    public void clickLogin() {
        click(loginLink);
    }

    public void clickTestCases() {
        click(testCasesLink);
    }

    public void clickApiTesting() {
        click(apiTestingLink);
    }

    public void clickContactUs() {
        click(contactUsLink);
    }

    public String getSuccessMessage() {
        return getText(successAlert);
    }

    public boolean isSuccessMessageDisplayed() {
        return isDisplayed(successAlert);
    }
}