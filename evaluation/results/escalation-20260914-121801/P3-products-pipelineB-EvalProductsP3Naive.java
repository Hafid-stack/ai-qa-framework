package pageobjects;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class EvalProductsP3Naive extends BasePage {

    private final By homeLink = By.xpath("//a[@href='/']");
    private final By productsLink = By.xpath("//a[@href='/products']");
    private final By cartLink = By.xpath("//a[@href='/view_cart']");
    private final By signupLoginLink = By.xpath("//a[@href='/login']");
    private final By testCasesLink = By.xpath("//a[@href='/test_cases']");
    private final By apiTestingLink = By.xpath("//a[@href='/api_list']");
    private final By videoTutorialsLink = By.xpath("//a[@href='https://www.youtube.com/c/AutomationExercise']");
    private final By contactUsLink = By.xpath("//a[@href='/contact_us']");
    private final By searchProductInput = By.id("search_product");
    private final By submitSearchButton = By.id("submit_search");
    private final By womenCategory = By.xpath("//a[@href='#Women']");
    private final By menCategory = By.xpath("//a[@href='#Men']");
    private final By kidsCategory = By.xpath("//a[@href='#Kids']");
    private final By subscribeEmailInput = By.id("susbscribe_email");
    private final By subscribeButton = By.id("subscribe");
    private final By continueShoppingButton = By.xpath("//button[@data-dismiss='modal']");

    public EvalProductsP3Naive(WebDriver driver) {
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

    public void clickSignupLogin() {
        click(signupLoginLink);
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

    public void typeSearchProduct(String value) {
        type(searchProductInput, value);
    }

    public void clickSubmitSearch() {
        click(submitSearchButton);
    }

    public void clickWomenCategory() {
        click(womenCategory);
    }

    public void clickMenCategory() {
        click(menCategory);
    }

    public void clickKidsCategory() {
        click(kidsCategory);
    }

    public void typeSubscribeEmail(String email) {
        type(subscribeEmailInput, email);
    }

    public void clickSubscribe() {
        click(subscribeButton);
    }

    public void clickContinueShopping() {
        click(continueShoppingButton);
    }

    public String getPageTitle() {
        return getText(By.cssSelector("h2.title"));
    }
}