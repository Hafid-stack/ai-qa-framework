package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class EvalHomeP2Naive extends BasePage {

    private final By homeLink = By.cssSelector("a[href='/']");
    private final By productsLink = By.cssSelector("a[href='/products']");
    private final By cartLink = By.cssSelector("a[href='/view_cart']");
    private final By loginLink = By.cssSelector("a[href='/login']");
    private final By testCasesLink = By.cssSelector("a[href='/test_cases']");
    private final By apiTestingLink = By.cssSelector("a[href='/api_list']");
    private final By videoTutorialsLink = By.cssSelector("a[href='https://www.youtube.com/c/AutomationExercise']");
    private final By contactUsLink = By.cssSelector("a[href='/contact_us']");
    private final By subscribeEmailInput = By.id("susbscribe_email");
    private final By subscribeButton = By.id("subscribe");
    private final By closeModalButton = By.className("close-modal");

    public EvalHomeP2Naive(WebDriver driver) {
        super(driver);
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

    public void clickVideoTutorials() {
        click(videoTutorialsLink);
    }

    public void clickContactUs() {
        click(contactUsLink);
    }

    public void typeSubscribeEmail(String email) {
        type(subscribeEmailInput, email);
    }

    public void clickSubscribe() {
        click(subscribeButton);
    }

    public void clickCloseModal() {
        click(closeModalButton);
    }

    public String getSubscribeEmailValue() {
        return getText(subscribeEmailInput);
    }
}