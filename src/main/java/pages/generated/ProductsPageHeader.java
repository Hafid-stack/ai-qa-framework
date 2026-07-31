package pages.generated;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ProductsPageHeader extends BasePage {

    private final By byHome = By.xpath("//a[contains(normalize-space(.), 'Home')]");
    private final By byProducts = By.xpath("//a[contains(normalize-space(.), 'Products')]");
    private final By byCart = By.xpath("//a[contains(normalize-space(.), 'Cart')]");
    private final By bySignupLogin = By.xpath("//a[contains(normalize-space(.), 'Signup / Login')]");
    private final By byTestCases = By.xpath("//a[contains(normalize-space(.), 'Test Cases')]");
    private final By byAPITesting = By.xpath("//a[contains(normalize-space(.), 'API Testing')]");
    private final By byVideoTutorials = By.xpath("//a[contains(normalize-space(.), 'Video Tutorials')]");
    private final By byContactUs = By.xpath("//a[contains(normalize-space(.), 'Contact us')]");

    public ProductsPageHeader(WebDriver driver) {
        super(driver);
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

}
